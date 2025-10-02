package com.korshak.stockconsumer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.korshak.stockconsumer.model.StockPrice;
import com.korshak.stockconsumer.service.StockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;

/**
 * Server-Sent Events (SSE) Controller for real-time stock price streaming
 */
@RestController
@RequestMapping("/api/stream")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StockStreamController {

    private static final Logger logger = LoggerFactory.getLogger(StockStreamController.class);
    private static final long SSE_TIMEOUT = 300_000L; // 5 minutes

    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private ObjectMapper objectMapper;

    // Store active SSE connections
    private final CopyOnWriteArrayList<SseEmitter> allStockEmitters = new CopyOnWriteArrayList<>();
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> symbolEmitters = new ConcurrentHashMap<>();

    /**
     * SSE endpoint for all stock price updates
     */
    @GetMapping(value = "/stocks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllStocks() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        allStockEmitters.add(emitter);
        logger.info("New SSE client connected for all stocks. Active connections: {}", allStockEmitters.size());

        // Send current prices immediately
        try {
            Map<String, StockPrice> currentPrices = stockDataService.getAllCurrentPrices();
            if (!currentPrices.isEmpty()) {
                for (StockPrice price : currentPrices.values()) {
                    sendStockPrice(emitter, price);
                }
            } else {
                emitter.send(SseEmitter.event()
                    .name("info")
                    .data("Connected to stock price stream. Waiting for data..."));
            }
        } catch (IOException e) {
            logger.error("Error sending initial data to SSE client", e);
            allStockEmitters.remove(emitter);
        }

        // Handle emitter completion/timeout
        emitter.onCompletion(() -> {
            allStockEmitters.remove(emitter);
            logger.info("SSE client disconnected from all stocks. Active connections: {}", allStockEmitters.size());
        });

        emitter.onTimeout(() -> {
            allStockEmitters.remove(emitter);
            logger.info("SSE client timeout for all stocks. Active connections: {}", allStockEmitters.size());
        });

        emitter.onError((throwable) -> {
            allStockEmitters.remove(emitter);
            logger.error("SSE error for all stocks client", throwable);
        });

        return emitter;
    }

    /**
     * SSE endpoint for specific symbol updates
     */
    @GetMapping(value = "/stocks/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSymbol(@PathVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        symbolEmitters.computeIfAbsent(upperSymbol, k -> new CopyOnWriteArrayList<>()).add(emitter);
        logger.info("New SSE client connected for symbol {}. Active connections: {}", 
                   upperSymbol, symbolEmitters.get(upperSymbol).size());

        // Send current price immediately
        try {
            StockPrice currentPrice = stockDataService.getCurrentPrice(upperSymbol);
            if (currentPrice != null) {
                sendStockPrice(emitter, currentPrice);
            } else {
                emitter.send(SseEmitter.event()
                    .name("info")
                    .data("Connected to " + upperSymbol + " price stream. Waiting for data..."));
            }
        } catch (IOException e) {
            logger.error("Error sending initial data to SSE client for symbol {}", upperSymbol, e);
            symbolEmitters.get(upperSymbol).remove(emitter);
        }

        // Handle emitter completion/timeout
        emitter.onCompletion(() -> {
            CopyOnWriteArrayList<SseEmitter> emitters = symbolEmitters.get(upperSymbol);
            if (emitters != null) {
                emitters.remove(emitter);
                logger.info("SSE client disconnected from {}. Active connections: {}", 
                           upperSymbol, emitters.size());
            }
        });

        emitter.onTimeout(() -> {
            CopyOnWriteArrayList<SseEmitter> emitters = symbolEmitters.get(upperSymbol);
            if (emitters != null) {
                emitters.remove(emitter);
                logger.info("SSE client timeout for {}. Active connections: {}", 
                           upperSymbol, emitters.size());
            }
        });

        emitter.onError((throwable) -> {
            CopyOnWriteArrayList<SseEmitter> emitters = symbolEmitters.get(upperSymbol);
            if (emitters != null) {
                emitters.remove(emitter);
                logger.error("SSE error for {} client", upperSymbol, throwable);
            }
        });

        return emitter;
    }

    /**
     * Get SSE connection statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStreamStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("allStocksConnections", allStockEmitters.size());
        
        Map<String, Integer> symbolConnections = new HashMap<>();
        symbolEmitters.forEach((symbol, emitters) -> 
            symbolConnections.put(symbol, emitters.size()));
        stats.put("symbolConnections", symbolConnections);
        
        int totalConnections = allStockEmitters.size() + 
            symbolEmitters.values().stream().mapToInt(CopyOnWriteArrayList::size).sum();
        stats.put("totalConnections", totalConnections);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Broadcast stock price to all relevant SSE clients
     * This method will be called by StockConsumerService
     */
    public void broadcastStockPrice(StockPrice stockPrice) {
        // Broadcast to all stocks subscribers
        broadcastToEmitters(allStockEmitters, stockPrice, "all-stocks");
        
        // Broadcast to specific symbol subscribers
        String symbol = stockPrice.getSymbol();
        CopyOnWriteArrayList<SseEmitter> symbolSpecificEmitters = symbolEmitters.get(symbol);
        if (symbolSpecificEmitters != null && !symbolSpecificEmitters.isEmpty()) {
            broadcastToEmitters(symbolSpecificEmitters, stockPrice, symbol);
        }
    }

    private void broadcastToEmitters(CopyOnWriteArrayList<SseEmitter> emitters, StockPrice stockPrice, String type) {
        if (emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                sendStockPrice(emitter, stockPrice);
            } catch (IOException e) {
                logger.warn("Failed to send data to SSE client for {}, removing connection", type);
                emitters.remove(emitter);
            }
        }
    }

    private void sendStockPrice(SseEmitter emitter, StockPrice stockPrice) throws IOException {
        try {
            String json = objectMapper.writeValueAsString(stockPrice);
            emitter.send(SseEmitter.event()
                .name("stock-price")
                .data(json));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing stock price to JSON", e);
            throw new IOException("JSON serialization error", e);
        }
    }
}
