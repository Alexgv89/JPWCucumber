#!/bin/bash
# Script para ejecutar tests en múltiples navegadores para Allure Run

echo "Running tests in Chrome..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=chrome || true

echo "Running tests in Firefox..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=firefox || true

echo "Running tests in Safari..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=safari || true
