#!/bin/bash
# Script para ejecutar suite completa (API + Cross-Browser UI Headless) para Allure Run

echo "Running API tests (REST Assured)..."
mvn test -Dcucumber.filter.tags="@api" || true

echo "Running UI tests in Chrome (Headless)..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=chrome -Dheadless=true || true

echo "Running UI tests in Firefox (Headless)..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=firefox -Dheadless=true || true

echo "Running UI tests in Safari (Headless)..."
mvn test -Dcucumber.filter.tags="@cross-browser" -Dbrowser=safari -Dheadless=true || true
