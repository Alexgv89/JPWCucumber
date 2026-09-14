package hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Hooks {
    // Instancias aisladas por hilo (Thread-Safe para ejecución paralela)
    private static final ThreadLocal<Playwright> playwrightThread = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThread = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThread = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThread = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> recordVideoThread = new ThreadLocal<>();

    private static Properties properties;
    
    // Colección sincronizada para registrar de forma segura los navegadores usados en ejecución paralela
    private static final Set<String> executedBrowsers = Collections.synchronizedSet(new HashSet<>());

    static {
        properties = new Properties();
        try (var input = Hooks.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp(Scenario scenario) {
        Playwright playwright = Playwright.create();
        playwrightThread.set(playwright);
        
        // 1. Prioridad 1: Lectura de etiquetas desde el archivo .feature
        String browserName = null;
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@browser=")) {
                browserName = tag.replace("@browser=", "").toLowerCase();
                break;
            } else if (tag.equals("@chrome") || tag.equals("@firefox") || tag.equals("@safari") || tag.equals("@webkit") || tag.equals("@edge")) {
                browserName = tag.replace("@", "").toLowerCase();
                break;
            }
        }

        // 2. Prioridad 2: Consola (-Dbrowser=...) > config.properties > Por defecto (chromium)
        if (browserName == null) {
            browserName = System.getProperty("browser", properties.getProperty("browser", "chromium")).toLowerCase();
        }
        
        // Normalización de motor Safari a WebKit
        if (browserName.equals("safari")) {
            browserName = "webkit";
        }
        
        // Registramos el navegador activo en la lista segura para Allure
        executedBrowsers.add(browserName);

        boolean headless = Boolean.parseBoolean(System.getProperty("headless", properties.getProperty("headless", "false")));
        String channel = System.getProperty("channel", properties.getProperty("channel"));
        
        String recordVideoProp = System.getProperty("record.video", properties.getProperty("record.video", "false"));
        boolean recordVideo = Boolean.parseBoolean(recordVideoProp);
        recordVideoThread.set(recordVideo);

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);
        if (channel != null && !channel.isEmpty()) {
            options.setChannel(channel);
        }

        // 3. Selección y lanzamiento del motor de navegador por hilo
        Browser browser;
        switch (browserName) {
            case "firefox":
                browser = playwright.firefox().launch(options);
                break;
            case "webkit":
                browser = playwright.webkit().launch(options);
                break;
            case "edge":
                options.setChannel("msedge");
                browser = playwright.chromium().launch(options);
                break;
            case "chrome":
                options.setChannel("chrome");
                browser = playwright.chromium().launch(options);
                break;
            case "chromium":
            default:
                browser = playwright.chromium().launch(options);
                break;
        }
        browserThread.set(browser);

        // 4. Configuración del contexto de Playwright y video opcional
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080);

        if (recordVideo) {
            contextOptions.setRecordVideoDir(Paths.get("target/videos/"));
        }

        BrowserContext context = browser.newContext(contextOptions);
        contextThread.set(context);
        
        Page page = context.newPage();
        pageThread.set(page);
    }

    @After
    public void tearDown(Scenario scenario) {
        Page page = pageThread.get();
        BrowserContext context = contextThread.get();
        Browser browser = browserThread.get();
        Playwright playwright = playwrightThread.get();

        // 5. Captura automática de pantalla en caso de fallo vinculada al escenario
        if (scenario.isFailed() && page != null) {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            String scenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
            scenario.attach(screenshot, "image/png", "screenshot-" + scenarioName);
        }

        // 6. Cierre ordenado de los recursos y limpieza de ThreadLocal para evitar fugas de memoria
        if (page != null) { page.close(); pageThread.remove(); }
        if (context != null) { context.close(); contextThread.remove(); }
        if (browser != null) { browser.close(); browserThread.remove(); }
        if (playwright != null) { playwright.close(); playwrightThread.remove(); }
        
        if (recordVideoThread.get() != null) {
            recordVideoThread.remove();
        }

        // 7. Generación segura del entorno para Allure
        generateAllureEnvironment();
    }

    // Método sincronizado para evitar condiciones de carrera en ejecuciones paralelas
    private synchronized void generateAllureEnvironment() {
        try {
            Path allureResultsDir = Paths.get("target/allure-results");
            Files.createDirectories(allureResultsDir);
            
            Properties envProps = new Properties();

            // Metadatos extraídos de config.properties (con valores por defecto si no existen)
            envProps.setProperty("Environment", properties.getProperty("Environment", "QA"));
            envProps.setProperty("Project", properties.getProperty("Project", "Automation"));
            envProps.setProperty("Executed.By", properties.getProperty("Executed.By", "Señor"));
            envProps.setProperty("Release.Version", properties.getProperty("Release.Version", "1.0"));

            // Datos dinámicos ejecución actual
            envProps.setProperty("Browsers.Used", String.join(", ", executedBrowsers));
            envProps.setProperty("Headless", System.getProperty("headless", properties.getProperty("headless", "false")));
            envProps.setProperty("OS.Name", System.getProperty("os.name"));
            envProps.setProperty("Java.Version", System.getProperty("java.version"));
            

            try (FileOutputStream fos = new FileOutputStream(allureResultsDir.resolve("environment.properties").toFile())) {
                envProps.store(fos, "Allure Environment Properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método seguro por hilo para acceder a la página actual desde las Pages
    public static Page getPage() {
        return pageThread.get();
    }
}