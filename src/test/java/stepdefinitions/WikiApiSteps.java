package stepdefinitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WikiApiSteps {

    private String baseUri;
    private RequestSpecification request;
    private Response response;

    @Dado("que la API REST de Wikipedia está disponible en {string}")
    public void queLaApiRestDeWikipediaEstaDisponibleEn(String uri) {
        this.baseUri = uri;
        this.request = given()
                .filter(new AllureRestAssured()) // Adjunta Request/Response automáticamente en Allure
                .baseUri(this.baseUri)
                .header("User-Agent", "WikipediaAutomationSuite/1.0 (test@example.com)")
                .contentType(ContentType.JSON);
    }

    @Cuando("realizo una petición GET al endpoint de resumen {string}")
    public void realizoUnaPeticionGetAlEndpointDeResumen(String endpoint) {
        this.response = this.request.when().get(endpoint);
    }

    @Entonces("la respuesta debe tener el código de estado HTTP {int}")
    public void laRespuestaDebeTenerElCodigoDeEstadoHttp(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, this.response.getStatusCode(),
                "El código de estado HTTP no coincide con el esperado.");
    }

    @Y("el cuerpo de la respuesta debe contener el título {string}")
    public void elCuerpoDeLaRespuestaDebeContenerElTitulo(String expectedTitle) {
        this.response.then().body("title", equalToIgnoringCase(expectedTitle));
    }

    @Y("el tipo de contenido debe ser {string}")
    public void elTipoDeContenidoDebeSer(String expectedContentType) {
        Assertions.assertTrue(this.response.getContentType().contains(expectedContentType),
                "El Content-Type no contiene " + expectedContentType);
    }
}
