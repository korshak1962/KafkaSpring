package com.korshak.stockproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Stock Producer
 */
@SpringBootApplication
@EnableScheduling
public class StockProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockProducerApplication.class, args);
    }
}
