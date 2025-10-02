package com.korshak.stockconsumer.service;

import com.korshak.stockconsumer.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing stock price data storage and retrieval
 * In-memory storage for demo purposes (could be replaced with database)
 */
@Service
public class StockDataService {

    private static final Logger logger = LoggerFactory.getLogger(StockDataService.class);
    private static final int MAX_HISTORY_SIZE = 1000; // Keep last 1000 records per symbol

    // In-memory storage: Symbol -> List of StockPrices
    private final Map<String, List<StockPrice>> stockHistory = new ConcurrentHashMap<>();
    
    // Current prices: Symbol -> Latest StockPrice
    private final Map<String, StockPrice> currentPrices = new ConcurrentHashMap<>();

    /**
     * Add a new stock price update
     */
    public void addStockPrice(StockPrice stockPrice) {
        String symbol = stockPrice.getSymbol();
        
        // Update current price
        currentPrices.put(symbol, stockPrice);
        
        // Add to history
        stockHistory.computeIfAbsent(symbol, k -> Collections.synchronizedList(new ArrayList<>()))
                   .add(stockPrice);
        
        // Trim history if needed
        List<StockPrice> history = stockHistory.get(symbol);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0); // Remove oldest
        }
        
        logger.debug("Added stock price for {}: {}", symbol, stockPrice.getPrice());
    }

    /**
     * Get current price for a symbol
     */
    public StockPrice getCurrentPrice(String symbol) {
        return currentPrices.get(symbol);
    }

    /**
     * Get all current prices
     */
    public Map<String, StockPrice> getAllCurrentPrices() {
        return new HashMap<>(currentPrices);
    }

    /**
     * Get historical prices for a symbol
     */
    public List<StockPrice> getHistory(String symbol) {
        List<StockPrice> history = stockHistory.get(symbol);
        return history != null ? new ArrayList<>(history) : new ArrayList<>();
    }

    /**
     * Get recent history for a symbol (last N records)
     */
    public List<StockPrice> getRecentHistory(String symbol, int limit) {
        List<StockPrice> history = stockHistory.get(symbol);
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        
        int fromIndex = Math.max(0, history.size() - limit);
        return new ArrayList<>(history.subList(fromIndex, history.size()));
    }

    /**
     * Get history within time range
     */
    public List<StockPrice> getHistoryInRange(String symbol, LocalDateTime from, LocalDateTime to) {
        List<StockPrice> history = stockHistory.get(symbol);
        if (history == null) {
            return new ArrayList<>();
        }

        return history.stream()
                .filter(sp -> sp.getTimestamp().isAfter(from) && sp.getTimestamp().isBefore(to))
                .collect(Collectors.toList());
    }

    /**
     * Get all available symbols
     */
    public Set<String> getAvailableSymbols() {
        return new HashSet<>(currentPrices.keySet());
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSymbols", currentPrices.size());
        stats.put("totalMessages", stockHistory.values().stream().mapToInt(List::size).sum());
        stats.put("symbols", getAvailableSymbols());
        return stats;
    }

    /**
     * Clear all data (useful for testing)
     */
    public void clearAll() {
        stockHistory.clear();
        currentPrices.clear();
        logger.info("Cleared all stock data");
    }
}
