package org.nextoracle;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot auto-configuration entry point for the nxo-auth library.
 *
 * Registered via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * so it is picked up automatically when the JAR is on the classpath.
 * No @Import or @ComponentScan is needed in the consuming application.
 */
@AutoConfiguration
@ComponentScan(basePackages = "org.nextoracle")
public class NxoAuthAutoConfiguration {
}
