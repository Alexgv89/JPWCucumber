package pages;
import com.microsoft.playwright.Page;
import manager.PlaywrightManager;
import static hooks.Hooks.getPage;

public class BasePage {

    public void navegar(String url) {
        Page page = getPage();
        String finalUrl = url;

        // Si la URL proporcionada es relativa (no empieza con http), le concatenamos la baseUrl del ambiente
        if (!url.startsWith("http")) {
            String baseUrl = PlaywrightManager.getBaseUrl();
            if (baseUrl != null) {
                finalUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + (url.startsWith("/") ? url.substring(1) : url);
            }
        }

        page.navigate(finalUrl);
    }

    public void title(String titulo){
        Page page = getPage();
        page.title();
    }
}
