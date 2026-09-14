package stepdefinitions;

import org.junit.jupiter.api.Assertions;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import pages.WikiHomePage;

public class WikiHomeSteps {
    private final WikiHomePage wikiHomePage = new WikiHomePage();
    @Dado("cuando navego a website de wikipedia {string}")
    public void cuando_navego_a_website_de_wikipedia(String url) {
        wikiHomePage.navegar(url);
    }

    @Entonces("se muestra el titulo {string}")
    public void se_muestra_el_titulo(String tituloEsperado) {
        Assertions.assertEquals(tituloEsperado,wikiHomePage.verificarTitulo());

    }
}
