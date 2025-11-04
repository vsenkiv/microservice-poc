package com.microservices.order.configuration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

@Configuration
public class DatabaseInitializationConfiguration {

  @Bean
  @Profile("local")
  public boolean initializeDatabase(JdbcTemplate jdbcTemplate) {
    try {
      // Load and execute schema SQL files (e.g., schema.sql, schema-products.sql)
      executeSqlFromClasspath(jdbcTemplate, "classpath:schema.sql");
      executeSqlFromClasspath(jdbcTemplate, "classpath:schema-products.sql");

      // Load and execute data SQL files (e.g., data.sql, data-products.sql)
      executeSqlFromClasspath(jdbcTemplate, "classpath:data.sql");
      executeSqlFromClasspath(jdbcTemplate, "classpath:data-products.sql");

      System.out.println("Database initialized successfully!");
      return true; // Return success
    } catch (Exception e) {
      System.err.println("Database initialization failed: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Database initialization failed", e);
    }
  }

  private void executeSqlFromClasspath(JdbcTemplate jdbcTemplate, String classpathResource)
      throws Exception {
    try (InputStream inputStream = getClass().getResourceAsStream(
        classpathResource.replace("classpath:", "/"))) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Could not find resource: " + classpathResource);
      }
      String sql = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

      // Split SQL statements by ';' (adjust based on your requirements)
      String[] sqlStatements = sql.split(";");

      for (String statement : sqlStatements) {
        // Ignore blank statements
        if (statement.trim().isEmpty()) {
          continue;
        }

        // Execute the SQL statement
        jdbcTemplate.execute(statement.trim());
      }
    }
  }

}
