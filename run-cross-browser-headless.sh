#!/bin/bash
# Script para ejecutar tests en múltiples navegadores modo HEADLESS para Allure Run

echo "Running tests in Chrome (Headless)..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=chrome -Dheadless=true || true

echo "Running tests in Firefox (Headless)..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=firefox -Dheadless=true || true

echo "Running tests in Safari (Headless)..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=safari -Dheadless=true || true
