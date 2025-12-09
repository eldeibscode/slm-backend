package com.slm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SlmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlmBackendApplication.class, args);
    }
}
