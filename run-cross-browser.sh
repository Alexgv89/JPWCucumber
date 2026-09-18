#!/bin/bash
# Script para ejecutar suite completa (API + Cross-Browser UI) para Allure Run

echo "Running API tests (REST Assured)..."
mvn test -Dcucumber.filter.tags="@api" || true

echo "Running UI tests in Chrome..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=chrome || true

echo "Running UI tests in Firefox..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=firefox || true

echo "Running UI tests in Safari..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=safari || true
