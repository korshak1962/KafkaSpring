package com.korshak.stockconsumer.controller;

import com.korshak.stockconsumer.model.StockPrice;
import com.korshak.stockconsumer.service.StockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API Controller for stock price data
 */
@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StockApiController {

    @Autowired
    private StockDataService stockDataService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "stock-consumer");
        response.put("timestamp", LocalDateTime.now());
        response.putAll(stockDataService.getStatistics());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all available symbols
     */
    @GetMapping("/symbols")
    public ResponseEntity<Set<String>> getSymbols() {
        return ResponseEntity.ok(stockDataService.getAvailableSymbols());
    }

    /**
     * Get current price for all symbols
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, StockPrice>> getCurrentPrices() {
        return ResponseEntity.ok(stockDataService.getAllCurrentPrices());
    }

    /**
     * Get current price for specific symbol
     */
    @GetMapping("/current/{symbol}")
    public ResponseEntity<StockPrice> getCurrentPrice(@PathVariable String symbol) {
        StockPrice price = stockDataService.getCurrentPrice(symbol.toUpperCase());
        if (price != null) {
            return ResponseEntity.ok(price);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get historical data for specific symbol
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<StockPrice>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "100") int limit) {
        List<StockPrice> history = stockDataService.getRecentHistory(symbol.toUpperCase(), limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get historical data within time range
     */
    @GetMapping("/history/{symbol}/range")
    public ResponseEntity<List<StockPrice>> getHistoryInRange(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<StockPrice> history = stockDataService.getHistoryInRange(symbol.toUpperCase(), from, to);
        return ResponseEntity.ok(history);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(stockDataService.getStatistics());
    }

    /**
     * Clear all data (for testing)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearData() {
        stockDataService.clearAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All stock data cleared");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
