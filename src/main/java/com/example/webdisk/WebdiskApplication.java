package com.example.webdisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The main entry point for the Webdisk application.
 * This class is annotated with @SpringBootApplication to indicate a Spring Boot application.
 * It also enables asynchronous method execution with the @EnableAsync annotation.
 * 
 * The main method uses SpringApplication.run to launch the application.
 */
@SpringBootApplication
@EnableAsync
public class WebdiskApplication {

    /**
     * The main method serves as the entry point for the Spring Boot application.
     * It delegates to Spring Boot's SpringApplication class by calling the run method.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(WebdiskApplication.class, args);
    }

}
