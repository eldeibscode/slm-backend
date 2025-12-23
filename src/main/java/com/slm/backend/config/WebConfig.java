package com.slm.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.upload.base-dir:./}")
    private String uploadBaseDir;

    @Value("${app.upload.path:uploads/reports/}")
    private String uploadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Parse allowed origins from environment variable
        String[] origins = allowedOrigins.split(",");

        registry.addMapping("/**")
                .allowedOriginPatterns(origins) // Use patterns for flexibility (supports wildcards)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight requests for 1 hour
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images from /app/uploads/reports/
        // Docker config: APP_UPLOAD_BASE_DIR=/app/uploads, APP_UPLOAD_PATH=reports/
        // Pattern: /uploads/reports/** maps to file:/app/uploads/reports/

        // Build the full path, ensuring proper separators and trailing slash
        String fullPath = uploadBaseDir;
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }
        fullPath += uploadPath;
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }

        String resourceLocation = "file:" + fullPath;

        System.out.println("=== Resource Handler Configuration ===");
        System.out.println("uploadBaseDir: " + uploadBaseDir);
        System.out.println("uploadPath: " + uploadPath);
        System.out.println("resourceLocation: " + resourceLocation);
        System.out.println("=====================================");

        registry.addResourceHandler("/uploads/reports/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
