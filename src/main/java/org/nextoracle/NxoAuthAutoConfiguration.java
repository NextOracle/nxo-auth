package org.nextoracle;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot auto-configuration entry point for the nxo-auth library.
 *
 * <p>Registered via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * so it is picked up automatically when the JAR is on the classpath.
 * No {@code @Import} or {@code @ComponentScan} is needed in the consuming application.</p>
 *
 * <p><strong>JPA note:</strong> Spring Boot's JPA auto-configuration only scans the consuming
 * application's own packages. This class explicitly registers the library's entity and
 * repository packages so that {@code AuthUser}, {@code AuthRole} etc. are managed by JPA
 * and all {@code *Repository} interfaces get their Spring Data proxies created.
 * If the consuming application also has its own repositories in a different package it
 * should add {@code @EnableJpaRepositories(basePackages = {"com.yourapp.repository", "org.nextoracle.repository"})}
 * and {@code @EntityScan(basePackages = {"com.yourapp.entity", "org.nextoracle.entity"})}
 * on its main class (or a {@code @Configuration} class) to cover both packages.</p>
 */
@AutoConfiguration
@ComponentScan(basePackages = "org.nextoracle")
@EnableJpaRepositories(basePackages = "org.nextoracle.repository")
@EntityScan(basePackages = "org.nextoracle.entity")
public class NxoAuthAutoConfiguration {
}
