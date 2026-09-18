# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Commands

### Test Execution
- **Full Suite (with Allure Run & Quality Gates)**: `npm run test`
- **Smoke Tests**: `npm run test:smoke`
- **Regression Suite**: `npm run test:regression`
- **API Tests (REST Assured)**: `npm run test:api`
- **Specific Browser**: `BROWSER=firefox npm run test:browser` (Replace `firefox` with `chrome`, `safari`, `edge`)
- **Specific Tag**: `TAG=@your_tag npm run test:tag`
- **Cross-Browser Execution**: `npm run test:cross-browser`
- **Headless Execution**: `npm run test:headless` or `BROWSER=firefox npm run test:browser:headless`

### Report Management
- **Generate Standard Report**: `npm run report:generate`
- **Open Interactive Report**: `npm run report:open`
- **Generate Static Single-File Report**: `npm run report:single`
- **Clear Results**: `npm run clear:results`
- **Clear History (Reset Trends)**: `npm run clear:history`
- **Clear Everything**: `npm run clear:all`

### Build & Maven
- **Standard Test Run**: `mvn test`
- **API Tests Only**: `mvn test -Dcucumber.filter.tags="@api"`

## High-Level Architecture

### Framework Stack
- **Core**: JUnit 5 + Cucumber + Playwright (Java) + REST Assured (API).
- **Reporting**: Allure (with custom `allurerc.js` for Quality Gates and environment filtering, plus Allure-REST-Assured filter).
- **Build Tool**: Maven.

### Key Components
- **`Hooks.java`**: Manages the test lifecycle (`@Before`, `@After`). Handles browser launch, environment resolution from `config.properties`, and injects mandatory Allure labels (`environment`, `browser`, `executor`) for report filtering.
- **`PlaywrightManager.java`**: Ensures thread-safe execution during parallel runs by using `ThreadLocal` for Playwright, Browser, Context, and Page instances.
- **Configuration**: Environment-specific URLs and settings are stored in `src/test/resources/config.properties`.
- ** Parallelism**: Enabled via `junit-platform.properties` (`cucumber.execution.parallel.enabled=true`).

### Allure Strategy
- **Result Consolidation**: Cross-browser runs save results into browser-specific directories (`target/allure-results-[browser]`), which are then merged into `target/allure-results` before report generation.
- **Quality Gates**: Defined in `allurerc.js`. The report identifies success rates and failure thresholds.
- **Environment Filtering**: Tests are labeled with their environment (e.g., "qa", "prod") during the `@Before` hook, allowing the Allure report to filter results by environment.

### Project Structure
- `src/test/java/hooks`: Test lifecycle and metadata.
- `src/test/java/manager`: Resource isolation and Playwright management.
- `src/test/java/pages`: UI Page Objects (Playwright).
- `src/test/java/services`: API Client / Service Objects (REST Assured).
- `src/test/java/stepdefinitions`: Cucumber step implementations.
- `src/test/resources/features`: Gherkin feature files.
- `src/test/resources/config.properties`: Global environment configuration.
