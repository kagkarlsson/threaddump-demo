package no.bekk.threaddumpdemo.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;

import javax.sql.DataSource;
import java.net.http.HttpClient;

@SuppressWarnings("rawtypes")
@Configuration
public class Config implements ApplicationListener<ContextRefreshedEvent> {

    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";
    public static final int MAX_POOL_SIZE = 2;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public JdbcDatabaseContainer postgresDatabase() {
        JdbcDatabaseContainer container = new PostgreSQLContainerProvider().newInstance("13");
        return container
                .withUsername(USERNAME)
                .withPassword(PASSWORD);
    }

    @Bean
    public DataSource getDataSource(JdbcDatabaseContainer container) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(USERNAME);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setMaximumPoolSize(MAX_POOL_SIZE);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        JdbcTemplate jdbc = event.getApplicationContext().getBean(JdbcTemplate.class);
        DataSource ds = event.getApplicationContext().getBean(DataSource.class);
        jdbc.execute("select 1");
        System.out.println("Worked!");
    }
}
