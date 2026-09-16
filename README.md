# JAVA PLAYWRIGHT + CUCUMBER + ALLURE + JUNIT 5

## 🌐 Scripts de Ejecución y Reportes (Allure)

El proyecto cuenta con una serie de comandos automatizados diseñados para la ejecución de pruebas multiplataforma, garantizando la separación de resultados por navegador y su posterior consolidación en un reporte unificado de Allure con soporte de **Quality Gates**.

### 📋 Comandos Disponibles

Puede ejecutar las siguientes tareas directamente desde su terminal utilizando `npm`:

#### 🚀 Ejecuciones Rápidas (Con Quality Gates y Allure Run)
Estos comandos utilizan el wrapper de Allure para una generación de reportes más robusta y el soporte de Puertas de Calidad.

| Comando | Descripción | Ejemplo / Nota |
| :--- | :--- | :--- |
| `npm run test` | Ejecuta la suite completa de pruebas. | `npm run test` |
| `npm run test:smoke` | Ejecuta solo los escenarios marcados con `@smoke`. | `npm run test:smoke` |
| `npm run test:regression` | Ejecuta la suite de regresión completa. | `npm run test:regression` |
| `npm run test:video` | Ejecuta la suite completa grabando video de la sesión. | `npm run test:video` |
| `npm run test:headless` | Ejecuta la suite completa sin interfaz gráfica (Invisible). | `npm run test:headless` |

#### 🛠️ Ejecuciones Dinámicas (Parámetros Personalizados)
Estos comandos permiten definir el navegador o el tag directamente desde la consola.

| Comando | Uso | Descripción | Ejemplo de ejecución |
| :--- | :--- | :--- | :--- |
| `npm run test:browser` | `BROWSER=[nombre] npm run test:browser` | Ejecuta la suite en un navegador específico. | `BROWSER=firefox npm run test:browser` |
| `npm run test:browser:headless` | `BROWSER=[nombre] npm run test:browser:headless` | Ejecuta en un navegador específico modo headless. | `BROWSER=safari npm run test:browser:headless` |
| `npm run test:tag` | `TAG=[@tag] npm run test:tag` | Ejecuta solo los escenarios que tengan el tag indicado. | `TAG=@mi_feature npm run test:tag` |

#### 🌐 Ejecuciones Cross-Browser (Consolidación de Resultados)
Ejecuciones que lanzan múltiples navegadores y unifican los resultados en un solo reporte.

| Comando | Navegadores | Modo | Descripción |
| :--- | :--- | :--- | :--- |
| `npm run test:cross-browser` | Chrome, Firefox, Safari | UI | Ejecución estándar multiplataforma. |
| `npm run test:cross-browser:headless` | Chrome, Firefox, Safari | Headless | Ejecución multiplataforma invisible. |

---

### ⚙️ Arquitectura del Flujo de Ejecución

Para mantener los archivos de características (`.feature`) limpios, agnósticos y enfocados puramente en las reglas de negocio, la infraestructura de pruebas se sustenta en los siguientes pilares de diseño:

1. **Separación de Responsabilidades (Clean Architecture):** Los archivos Gherkin dictan las reglas del negocio, mientras que la infraestructura y los scripts de consola controlan la ejecución física y la infraestructura de los navegadores, evitando mezclar código de pruebas con configuraciones de entorno.
2. **Escalabilidad y Mantenimiento en CI/CD:** Gracias al aislamiento de resultados en directorios temporales independientes (`target/allure-results-[browser]`), se evitan condiciones de carrera (*race conditions*) y corrupción de datos durante ejecuciones automatizadas y desatendidas.
3. **Reportes de Calidad Consolidados (Allure):** Mediante la inyección dinámica de metadatos en los ganchos (*hooks*), cada navegador se registra como una entidad de prueba única y separada, permitiendo auditar métricas reales de éxito, tiempos de carga y comportamiento por motor de renderizado sin falsos agrupamientos por reintentos.
