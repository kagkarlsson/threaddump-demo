package no.bekk.threaddumpdemo.config;


import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;

import javax.sql.DataSource;

@SuppressWarnings("rawtypes")
@Configuration
public class DatabaseConfig implements ApplicationListener<ContextRefreshedEvent> {
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";

    @Bean(initMethod = "start", destroyMethod = "stop")
    public JdbcDatabaseContainer postgresDatabase() {
        JdbcDatabaseContainer container = new PostgreSQLContainerProvider().newInstance("13");
        return container
                .withUsername(USERNAME)
                .withPassword(PASSWORD);
    }

    @Bean
    public DataSource getDataSource(JdbcDatabaseContainer container) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()
                .url(container.getJdbcUrl())
                .username(USERNAME)
                .password(PASSWORD);
        return dataSourceBuilder.build();
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        JdbcTemplate jdbc = event.getApplicationContext().getBean(JdbcTemplate.class);
        jdbc.execute("select 1");
        System.out.println("Worked!");
    }
}
