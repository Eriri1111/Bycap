package com.bycap.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry Point for Bycap Commercial Payment Gateway.
 * Launches the embedded web server, REST API endpoints, JPA Database persistence,
 * and serves the interactive HTML/JS Dashboard at http://localhost:8080.
 */
@SpringBootApplication
public class BycapGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BycapGatewayApplication.class, args);
    }
}
