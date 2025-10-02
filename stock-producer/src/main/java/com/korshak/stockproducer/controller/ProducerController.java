package com.korshak.stockproducer.controller;

import com.korshak.stockproducer.service.StockPriceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for monitoring the stock producer
 */
@RestController
@RequestMapping("/api/producer")
public class ProducerController {

    @Autowired
    private StockPriceGeneratorService stockPriceGeneratorService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "stock-producer");
        response.put("currentPrice", stockPriceGeneratorService.getCurrentPrice());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current stock price
     */
    @GetMapping("/current-price")
    public ResponseEntity<Map<String, Object>> getCurrentPrice() {
        Map<String, Object> response = new HashMap<>();
        response.put("currentPrice", stockPriceGeneratorService.getCurrentPrice());
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
