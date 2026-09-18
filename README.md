# 🚀 Wikipedia Automation Suite (Playwright + Cucumber + Allure)

Este framework de automatización representa una implementación avanzada de pruebas E2E, diseñada para ser escalable, multiplataforma y totalmente integrable en flujos de CI/CD. Su arquitectura prioriza el aislamiento de hilos, la consolidación de metadatos y la visibilidad de la calidad mediante **Quality Gates**.

---

## 🏗️ Arquitectura Técnica

El framework ha sido diseñado bajo principios de **Clean Architecture**, separando la definición de negocio (Gherkin) de la infraestructura técnica.

### 🔹 Pilares de Diseño:
- **Paralelismo Seguro**: Implementado mediante `ThreadLocal` en el `PlaywrightManager`, asegurando que cada hilo de ejecución tenga su propia instancia de navegador, contexto y página, eliminando cualquier fuga de estado (*state leakage*).
- **Unicidad en Allure**: Modificación programática del `testCaseId` en los Hooks, permitiendo que un mismo escenario ejecutado en diferentes navegadores se registre como una entrada única, evitando que Allure los interprete como "reintentos".
- **Consolidación de Metadata (The Fragment Strategy)**: Para evitar que los navegadores sobreescriban el archivo `environment.properties` en ejecuciones paralelas o cross-browser, cada proceso escribe un fragmento (`env-chrome.properties`). Un script de consolidación final (`calculate-quality-gate.js`) une estos fragmentos en un único archivo maestro.
- **Puertas de Calidad (Quality Gates)**: Implementación de reglas estrictas (Tasa de éxito 100%, 0 fallos) que se calculan dinámicamente y se visualizan en el Home del reporte, permitiendo una decisión rápida de "Go/No-Go".

---

## 🌐 Scripts de Ejecución

Todos los comandos utilizan el wrapper de Allure para garantizar la generación de reportes y la activación de Quality Gates.

### 🚀 Ejecuciones Rápidas (Local)
| Comando | Descripción | Nota |
| :--- | :--- | :--- |
| `npm run test` | Suite completa (Chrome por defecto). | Ideal para desarrollo rápido. |
| `npm run test:smoke` | Solo escenarios `@smoke`. | Validación rápida de funcionalidades críticas. |
| `npm run test:regression` | Suite de regresión completa. | Validación exhaustiva antes de despliegue. |
| `npm run test:headless` | Ejecución invisible. | Mayor velocidad y menor consumo de recursos. |

### 🛠️ Ejecuciones Dinámicas (Parámetros)
| Comando | Uso | Descripción |
| :--- | :--- | :--- |
| `npm run test:browser` | `BROWSER=firefox npm run test:browser` | Ejecuta en un navegador específico. |
| `npm run test:browser:headless` | `BROWSER=safari npm run test:browser:headless` | Ejecuta en un navegador específico modo invisible. |
| `npm run test:tag` | `TAG=@mi_tag npm run test:tag` | Ejecuta escenarios con el tag indicado. |

### 🌐 Ejecuciones Cross-Browser
| Comando | Navegadores | Modo | Descripción |
| :--- | :--- | :--- | :--- |
| `npm run test:cross-browser` | Chrome, Firefox, Safari | UI | Ejecución multiplataforma con interfaz. |
| `npm run test:cross-browser:headless` | Chrome, Firefox, Safari | Headless | Ejecución multiplataforma invisible. |

---

## 📊 Generación y Visualización de Reportes

El framework ofrece múltiples opciones para visualizar y compartir los reportes generados:

### 1️⃣ Reporte Local Interactivo (Servidor Web)
```bash
# Generar y abrir el reporte estándar de Allure en el puerto 8082
npm run report:open
```

### 2️⃣ Reporte Estático Autónomo en 1 Solo Archivo (`single-file`) 📎
Ideal para **enviar por correo, Slack o Teams**. Genera un único archivo HTML autocontenido con todos los assets, estilos, imágenes y datos incrustados, que se puede abrir directamente con **doble clic** en cualquier navegador sin necesidad de servidor:
```bash
# Genera el archivo: allure-report-single/index.html
npm run report:single
```

### 3️⃣ Reporte en Vivo en la Nube (GitHub Pages) 🌐
El flujo de CI despliega automáticamente el reporte en vivo tras cada ejecución:
👉 **URL Pública**: `https://alexgv89.github.io/JPWCucumber/`

### 4️⃣ Consulta de Ejecuciones Anteriores (Histórico y Artefactos) 🗄️
- **Desde la Web**: En el reporte de GitHub Pages, consulta la sección **History / Trends** para ver la evolución y reintentos de cada prueba.
- **Desde GitHub Actions**: En la pestaña **Actions**, selecciona cualquier ejecución pasada y descarga el artefacto **`github-pages`** (ZIP). Para visualizarlo localmente:
  ```bash
  # Descomprimir y servir:
  npx allure open ruta/a/la/carpeta/descomprimida
  ```

---

## ☁️ Integración Continua (CI/CD)

El proyecto está totalmente preparado para ejecutarse en servidores de CI (como GitHub Actions) mediante comandos optimizados que generan artefactos estáticos.

### 📦 Comandos de CI
- `npm run test:ci:cross`: Ejecuta la suite cross-browser en modo headless con `allure run`, consolidando metadatos y Quality Gates.
- `npm run test:ci:browser`: Ejecuta un navegador específico en CI con `allure run`.
- `npm run report:generate:ci`: Garantiza la disponibilidad del reporte para el despliegue en Pages.

### 🚀 Flujo de Despliegue en GitHub Pages
El pipeline de CI realiza las siguientes acciones:
1. **Aislamiento**: Ejecuta los tests en un contenedor Linux limpio con **Java 25** y **Node.js 22**.
2. **Consolidación**: Ejecuta `calculate-quality-gate.js` para unificar la metadata de todos los navegadores (`Browsers.Used`).
3. **Landing Page**: Crea un portal de entrada profesional (`index.html`) para acceder fácilmente al reporte de Allure.
4. **Historial Persistente**: Utiliza `actions/cache` para acumular el historial (`allure-history/history.jsonl`) entre ejecuciones.
5. **Publicación**: Despliega el resultado final en **GitHub Pages**.

---

## 📊 Metadatos del Reporte
El reporte generado incluye automáticamente la siguiente información consolidada en el Home:
- **Browsers.Used**: Listado de todos los navegadores que participaron en la ejecución (ej: `CHROME, FIREFOX, SAFARI`).
- **Environment**: Ambiente ejecutado (QA, Prod, etc.) resuelto dinámicamente desde `config.properties`.
- **Base URL**: URL del sistema bajo prueba.
- **OS / Java**: Información del sistema operativo y versión de Java del servidor de ejecución.
- **Quality Gates**: Estado de salud del proyecto basado en la tasa de éxito y fallos máximos permitidos.
