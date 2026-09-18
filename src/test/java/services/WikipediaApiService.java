package services;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class WikipediaApiService {

    private static final Properties properties = new Properties();
    private String baseUri;

    static {
        try (var input = WikipediaApiService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WikipediaApiService() {
        this.baseUri = resolveDefaultBaseUri();
    }

    /**
     * Resuelve la URL base del ambiente activo (QA, DEV, STAGING, PROD)
     */
    private String getEnvironmentBaseUrl() {
        String activeEnv = System.getProperty("environment", properties.getProperty("Environment", "QA")).toLowerCase();
        String urlKey = "url." + activeEnv;
        String baseUrl = properties.getProperty(urlKey);
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = properties.getProperty("url.qa", "https://es.wikipedia.org");
        }
        return baseUrl;
    }

    /**
     * Resuelve la URL base por defecto agregando el path REST
     */
    private String resolveDefaultBaseUri() {
        String baseUrl = getEnvironmentBaseUrl();
        return baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/rest_v1";
    }

    /**
     * Configura la URI base soportando tanto URLs absolutas (http/https) como relativas (/api/rest_v1)
     * @param uri URL completa o path relativo
     */
    public void setBaseUri(String uri) {
        if (uri == null || uri.trim().isEmpty()) {
            this.baseUri = resolveDefaultBaseUri();
        } else if (uri.startsWith("http://") || uri.startsWith("https://")) {
            // URL Absoluta: se toma tal cual
            this.baseUri = uri;
        } else {
            // Path Relativo: se concatena con la baseUrl del ambiente activo
            String baseUrl = getEnvironmentBaseUrl();
            this.baseUri = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + (uri.startsWith("/") ? uri.substring(1) : uri);
        }
    }

    public String getBaseUri() {
        return this.baseUri;
    }

    private RequestSpecification getBaseRequest() {
        return given()
                .filter(new AllureRestAssured()) // Adjunta HTTP Request & Response automáticamente a Allure
                .baseUri(this.baseUri)
                .header("User-Agent", "WikipediaAutomationSuite/1.0 (test@example.com)")
                .contentType(ContentType.JSON);
    }

    /**
     * Consulta el resumen de un artículo por su endpoint en la API REST de Wikipedia
     * @param endpoint Ruta del endpoint (ej. "/page/summary/Selenium")
     * @return Response de REST Assured
     */
    public Response getArticleSummary(String endpoint) {
        return getBaseRequest()
                .when()
                .get(endpoint);
    }
}
