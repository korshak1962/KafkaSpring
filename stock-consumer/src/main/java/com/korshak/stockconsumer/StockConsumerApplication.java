package com.korshak.stockconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Stock Consumer
 * Provides WebSocket and SSE endpoints for real-time stock data streaming
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class StockConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockConsumerApplication.class, args);
    }
}
