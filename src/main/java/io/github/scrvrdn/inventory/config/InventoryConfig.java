package io.github.scrvrdn.inventory.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class InventoryConfig {

    @Bean
    public DataSource dataSource() throws IOException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.sqlite.JDBC");
        Path dbPath = Paths.get(System.getProperty("user.home"), ".inventory", "database.db");
        Files.createDirectories(dbPath.getParent());
        dataSource.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
        
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("PRAGMA foreign_keys = ON");

        return jdbcTemplate;
    }
}
