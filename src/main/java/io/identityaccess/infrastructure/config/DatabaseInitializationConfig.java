package io.identityaccess.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseInitializationConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "app.database.schema",
            name = "initialize-on-startup",
            havingValue = "true",
            matchIfMissing = false)
    public ConnectionFactoryInitializer databaseSchemaInitializer(ConnectionFactory connectionFactory) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/init/01-schema.sql"),
                new ClassPathResource("db/init/02-seed-roles.sql"));

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
