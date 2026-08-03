package org.nextoracle.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(SpringLiquibase.class)
public class NxoAuthLiquibaseConfig {

    @Bean(name = "nxoAuthLiquibase")
    public SpringLiquibase nxoAuthLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/nxo-auth-changelog.yaml");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
