package manager;

import com.microsoft.playwright.*;
import java.util.Optional;

public class PlaywrightManager {
    private static final ThreadLocal<Playwright> playwrightThread = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThread = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThread = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThread = new ThreadLocal<>();
    private static final ThreadLocal<String> baseUrlThread = new ThreadLocal<>();

    public static void setPlaywright(Playwright playwright) {
        playwrightThread.set(playwright);
    }

    public static void setBrowser(Browser browser) {
        browserThread.set(browser);
    }

    public static void setContext(BrowserContext context) {
        contextThread.set(context);
    }

    public static void setPage(Page page) {
        pageThread.set(page);
    }

    public static void setBaseUrl(String baseUrl) {
        baseUrlThread.set(baseUrl);
    }

    public static String getBaseUrl() {
        return baseUrlThread.get();
    }

    public static Page getPage() {
        return pageThread.get();
    }

    public static void cleanUp() {
        try {
            if (pageThread.get() != null) {
                pageThread.get().close();
            }
            if (contextThread.get() != null) {
                contextThread.get().close();
            }
            if (browserThread.get() != null) {
                browserThread.get().close();
            }
            if (playwrightThread.get() != null) {
                playwrightThread.get().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pageThread.remove();
            contextThread.remove();
            browserThread.remove();
            playwrightThread.remove();
            baseUrlThread.remove();
        }
    }
}
