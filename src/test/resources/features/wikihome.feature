      # language: es
      @allure.label.epic:Wikipedia
      @allure.label.feature:Navegación_y_Búsqueda
      @allure.label.layer:UI
      @allure.label.owner:QA_Team
      @allure.link.wiki:https://www.wikipedia.org/
      @cross-browser
      Característica: navegar a home de wikipedia
      @smoke
      @allure.label.story:Validación_de_Portal_Principal
      @allure.label.severity:critical
      @allure.label.owner:AlexGV
      @allure.issue:WIKI-001
      @allure.tms:TC-001
      Esquema del escenario: verificar home y titulo de wikipedia
      Dado cuando navego a website de wikipedia "https://es.wikipedia.org/wiki/Wikipedia:Portada"
      Entonces se muestra el titulo "Título Incorrecto para Error Rojo"

      Ejemplos:
      | nombre |
      | juan   |
      | pedro  |
      Escenario: verificar home

      |


