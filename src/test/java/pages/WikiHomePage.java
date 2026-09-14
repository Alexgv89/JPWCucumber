package pages;

import static hooks.Hooks.getPage;

public class WikiHomePage extends BasePage {
    private final String titulo = "h1[id='Bienvenidos_a_Wikipedia,']";

    public String verificarTitulo() {
        getPage().waitForSelector(titulo);
        return getPage().textContent(titulo);
    }
}
