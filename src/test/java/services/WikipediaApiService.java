package services;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class WikipediaApiService {

    private String baseUri;

    public WikipediaApiService() {
        this.baseUri = "https://es.wikipedia.org/api/rest_v1";
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    private RequestSpecification getBaseRequest() {
        return given()
                .filter(new AllureRestAssured()) // Adjunta HTTP Request & Response automáticamente a Allure
                .baseUri(this.baseUri)
                .header("User-Agent", "WikipediaAutomationSuite/1.0 (test@example.com)")
                .contentType(ContentType.JSON);
    }

    /**
     * Consulta el resumen de un artículo por su título en la API REST de Wikipedia
     * @param endpoint Ruta o título del artículo (ej. "/page/summary/Selenium")
     * @return Response de REST Assured con el status, headers y body JSON
     */
    public Response getArticleSummary(String endpoint) {
        return getBaseRequest()
                .when()
                .get(endpoint);
    }
}
