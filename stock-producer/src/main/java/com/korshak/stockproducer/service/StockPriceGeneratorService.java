package com.korshak.stockproducer.service;

import com.korshak.stockproducer.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stock Price Generator Service that produces fake stock prices
 * and sends them to Kafka topic
 */
@Service
public class StockPriceGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceGeneratorService.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();
    
    // Configuration properties
    @Value("${stock.producer.topic}")
    private String topicName;
    
    @Value("${stock.producer.initial-price}")
    private double initialPrice;
    
    @Value("${stock.producer.max-change}")
    private double maxChange;
    
    // Current stock states
    private final AtomicReference<Double> currentPrice = new AtomicReference<>();
    private final String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
    private int currentSymbolIndex = 0;
    
    public StockPriceGeneratorService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Initialize the current price after properties are loaded
     */
    public void initializePrice() {
        if (currentPrice.get() == null) {
            currentPrice.set(initialPrice);
            logger.info("Initialized stock price to: {}", initialPrice);
        }
    }
    
    /**
     * Generate and send stock price data every second
     */
    @Scheduled(fixedRateString = "${stock.producer.interval}")
    public void generateAndSendStockPrice() {
        initializePrice();
        
        // Get current symbol (rotate through symbols)
        String symbol = symbols[currentSymbolIndex];
        currentSymbolIndex = (currentSymbolIndex + 1) % symbols.length;
        
        // Calculate price change (-maxChange to +maxChange)
        double changeAmount = (random.nextDouble() - 0.5) * 2 * maxChange;
        
        // Update current price (ensure it doesn't go below 1.0)
        double oldPrice = currentPrice.get();
        double newPrice = Math.max(1.0, oldPrice + changeAmount);
        currentPrice.set(newPrice);
        
        // Calculate change percentage
        double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;
        
        // Create stock price object
        StockPrice stockPrice = new StockPrice(
            symbol,
            Math.round(newPrice * 100.0) / 100.0, // Round to 2 decimal places
            Math.round(changeAmount * 100.0) / 100.0,
            Math.round(changePercent * 100.0) / 100.0,
            LocalDateTime.now()
        );

      sendToKafka(symbol, stockPrice);
    }

  private void sendToKafka(String symbol, StockPrice stockPrice) {
    // Send to Kafka
    try {
        kafkaTemplate.send(topicName, symbol, stockPrice)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent stock price: {}", stockPrice);
                } else {
                    logger.error("Failed to send stock price: {}", stockPrice, ex);
                }
            });
    } catch (Exception e) {
        logger.error("Error sending stock price to Kafka", e);
    }
  }

  /**
     * Get current price for monitoring
     */
    public double getCurrentPrice() {
        return currentPrice.get() != null ? currentPrice.get() : initialPrice;
    }
}
