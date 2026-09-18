#language: es
@allure.label.epic:Wikipedia
@allure.label.feature:API_Wikipedia_REST
@allure.label.layer:API
@allure.label.owner:QA_Team
@api
Característica: Validación de Servicios REST de Wikipedia

  @smoke 
  @allure.label.story:Consulta_de_Resumen_de_Artículo
  @allure.label.severity:critical
  @allure.issue:API-001
  @allure.tms:TC-API-001
  Escenario: Consultar información y resumen de un artículo existente por API
    Dado que la API REST de Wikipedia está disponible en "/api/rest_v1"
    Cuando realizo una petición GET al endpoint de resumen "/page/summary/Selenium"
    Entonces la respuesta debe tener el código de estado HTTP 200
    Y el cuerpo de la respuesta debe contener el título "Selenium"
    Y el tipo de contenido debe ser "application/json"
