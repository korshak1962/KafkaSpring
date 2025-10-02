package com.korshak.stockconsumer.controller;

import com.korshak.stockconsumer.model.StockPrice;
import com.korshak.stockconsumer.service.KafkaHistoryService;
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

    @Autowired
    private KafkaHistoryService kafkaHistoryService;

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
        
        // Add Kafka message count
        long kafkaMessageCount = kafkaHistoryService.getMessageCount();
        response.put("kafkaMessageCount", kafkaMessageCount);
        
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
     * Get historical data for specific symbol (from in-memory storage)
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<StockPrice>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "100") int limit) {
        List<StockPrice> history = stockDataService.getRecentHistory(symbol.toUpperCase(), limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get historical data for specific symbol directly from Kafka
     * Use this to get messages that arrived before client connected
     */
    @GetMapping("/history/kafka/{symbol}")
    public ResponseEntity<List<StockPrice>> getKafkaHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "500") int limit) {
        List<StockPrice> history = kafkaHistoryService.getHistoryFromKafka(symbol.toUpperCase(), limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get all historical data from Kafka (all symbols)
     */
    @GetMapping("/history/kafka/all")
    public ResponseEntity<List<StockPrice>> getAllKafkaHistory(
            @RequestParam(defaultValue = "1000") int limit) {
        List<StockPrice> history = kafkaHistoryService.getAllHistoryFromKafka(limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get historical data within time range (from in-memory storage)
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
     * Get historical data within time range directly from Kafka
     */
    @GetMapping("/history/kafka/{symbol}/range")
    public ResponseEntity<List<StockPrice>> getKafkaHistoryInRange(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<StockPrice> history = kafkaHistoryService.getHistoryByTimeRange(symbol.toUpperCase(), from, to);
        return ResponseEntity.ok(history);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = stockDataService.getStatistics();
        stats.put("kafkaMessageCount", kafkaHistoryService.getMessageCount());
        return ResponseEntity.ok(stats);
    }

    /**
     * Get Kafka-specific statistics
     */
    @GetMapping("/stats/kafka")
    public ResponseEntity<Map<String, Object>> getKafkaStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", kafkaHistoryService.getMessageCount());
        stats.put("topic", "stock-prices");
        stats.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(stats);
    }

    /**
     * Clear all data (for testing)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearData() {
        stockDataService.clearAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All in-memory stock data cleared");
        response.put("note", "Kafka data is not affected");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
