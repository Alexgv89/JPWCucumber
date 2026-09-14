package runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;


import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features") // Carpeta donde están tus archivos .feature dentro de src/test/resources
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "stepdefinitions, hooks") // Paquetes donde Cucumber buscará
                                                                                    // los Steps y Hooks
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true") // Oculta advertencias de publicación de
                                                                        // Cucumber
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm, pretty") // Plugin
                                                                                                                        // de
                                                                                                                        // Allure
                                                                                                                        // y
                                                                                                                        // consola
                                                                                                                        // bonita
public class TestRunner {
}
