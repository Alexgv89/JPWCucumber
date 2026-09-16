package hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Label;
import manager.PlaywrightManager;

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
    // Colección sincronizada para registrar de forma segura los navegadores usados en ejecución paralela
    private static final Set<String> executedBrowsers = Collections.synchronizedSet(new HashSet<>());
    // Contador para limitar los intentos de actualizar el testCaseId y evitar spam de errores en consola
    private static final ThreadLocal<Integer> updateAttempts = ThreadLocal.withInitial(() -> 0);

    private static Properties properties;
    private String envName;
    private String displayBrowserName;
    private String executorName;
    private boolean headless;

    static {
        properties = new Properties();
        try (var input = Hooks.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null)
                properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp(Scenario scenario) {
        Playwright playwright = Playwright.create();
        PlaywrightManager.setPlaywright(playwright);

        // 1. Obtención y normalización del ambiente real de ejecución
        this.envName = System.getProperty("environment", properties.getProperty("Environment", "QA")).toUpperCase();

        // RESOLUCIÓN DE URL DINÁMICA
        String urlKey = "url." + this.envName.toLowerCase();
        String baseUrl = properties.getProperty(urlKey);

        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = System.getProperty("baseUrl", properties.getProperty("url.qa", "https://es.wikipedia.org"));
        }
        PlaywrightManager.setBaseUrl(baseUrl);

        // 2. Determinación del navegador
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

        if (browserName == null) {
            browserName = System.getProperty("browser", properties.getProperty("browser", "chromium")).toLowerCase();
        }

        this.displayBrowserName = browserName.toUpperCase();
        this.executorName = this.displayBrowserName;

        if (browserName.equals("safari")) {
            browserName = "webkit";
            this.displayBrowserName = "SAFARI";
            this.executorName = "Safari";
        } else if (browserName.equals("chromium")) {
            this.executorName = "Chromium";
        } else {
            this.executorName = this.displayBrowserName.substring(0, 1).toUpperCase() + this.displayBrowserName.substring(1).toLowerCase();
        }

        executedBrowsers.add(this.displayBrowserName);

        this.headless = Boolean.parseBoolean(System.getProperty("headless", properties.getProperty("headless", "false")));
        String channel = System.getProperty("channel", properties.getProperty("channel"));

        String recordVideoProp = System.getProperty("record.video", properties.getProperty("record.video", "false"));
        boolean recordVideo = Boolean.parseBoolean(recordVideoProp);

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(this.headless);
        if (channel != null && !channel.isEmpty()) {
            options.setChannel(channel);
        }

        // 4. Selección y lanzamiento del motor de navegador
        Browser browser;
        switch (browserName) {
            case "firefox": browser = playwright.firefox().launch(options); break;
            case "webkit": browser = playwright.webkit().launch(options); break;
            case "edge":
                options.setChannel("msedge");
                browser = playwright.chromium().launch(options);
                break;
            case "chrome":
                options.setChannel("chrome");
                browser = playwright.chromium().launch(options);
                break;
            case "chromium":
            default: browser = playwright.chromium().launch(options); break;
        }
        PlaywrightManager.setBrowser(browser);

        // 5. Configuración del contexto de Playwright y video opcional
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setViewportSize(1920, 1080);
        if (recordVideo) {
            contextOptions.setRecordVideoDir(Paths.get("target/videos/"));
        }

        BrowserContext context = browser.newContext(contextOptions);
        PlaywrightManager.setContext(context);

        Page page = context.newPage();
        PlaywrightManager.setPage(page);
    }

    @io.cucumber.java.BeforeStep
    public void beforeStep() {
        int attempts = updateAttempts.get();
        if (attempts < 3) {
            try {
                AllureLifecycle lifecycle = Allure.getLifecycle();
                lifecycle.updateTestCase(test -> {
                    String originalId = test.getTestCaseId();
                    if (originalId != null && !originalId.contains("_")) {
                        test.setTestCaseId(originalId + "_" + this.displayBrowserName.toLowerCase());
                    }
                });

                // Inyección de metadatos
                Allure.label("environment", this.envName.toLowerCase());
                Allure.label("browser", this.displayBrowserName.toLowerCase());
                Allure.label("executor", this.executorName.toLowerCase());
                Allure.label("thread", String.format("%s (ID: %d)", Thread.currentThread().getName(), Thread.currentThread().getId()));

                Allure.parameter("Environment", this.envName);
                Allure.parameter("Base URL", PlaywrightManager.getBaseUrl());
                Allure.parameter("Browser", this.displayBrowserName);
                Allure.parameter("browser.executor", this.executorName);
                Allure.parameter("Headless", String.valueOf(this.headless));

                updateAttempts.set(Integer.MAX_VALUE);
            } catch (Exception e) {
                updateAttempts.set(attempts + 1);
            }
        }
    }


    @After
    public void tearDown(Scenario scenario) {
        Page page = PlaywrightManager.getPage();
        if (scenario.isFailed() && page != null) {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            String scenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
            scenario.attach(screenshot, "image/png", "screenshot-" + scenarioName);
        }
        PlaywrightManager.cleanUp();
    }

    @AfterAll
    public static synchronized void generateAllureEnvironment() {
        System.out.println(">>> ALLURE DEBUG: Executing generateAllureEnvironment()");
        try {
            String resultsDirStr = System.getProperty("allure.results.directory", "target/allure-results");
            Path allureResultsDir = Paths.get(resultsDirStr);
            Files.createDirectories(allureResultsDir);

            Properties envProps = new Properties();
            String activeEnv = System.getProperty("environment", properties.getProperty("Environment", "QA")).toUpperCase();
            envProps.setProperty("Environment", activeEnv);

            // Agregar la URL base al entorno global
            String urlKey = "url." + activeEnv.toLowerCase();
            String baseUrl = properties.getProperty(urlKey, "https://es.wikipedia.org");
            envProps.setProperty("Base URL", baseUrl);

            envProps.setProperty("Project", properties.getProperty("Project", "Wikipedia Automation Suite"));
            envProps.setProperty("Executed.By", properties.getProperty("Executed.By", "Señor"));
            envProps.setProperty("Release.Version", properties.getProperty("Release.Version", "1.0.0"));

            String activeBrowser = System.getProperty("browser", "CHROME").toUpperCase();
            envProps.setProperty("Browser", activeBrowser);
            envProps.setProperty("browser.executor", activeBrowser);
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

    public static Page getPage() {
        return PlaywrightManager.getPage();
    }
}
