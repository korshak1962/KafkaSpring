package com.korshak.stockconsumer.service;

import com.korshak.stockconsumer.controller.StockStreamController;
import com.korshak.stockconsumer.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service that consumes stock prices and broadcasts them via WebSocket and SSE
 */
@Service
public class StockConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(StockConsumerService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private StockStreamController stockStreamController;

    /**
     * Consume messages from stock-prices topic
     */
    @KafkaListener(topics = "${stock.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeStockPrice(StockPrice stockPrice) {
        try {
            logger.info("Consumed stock price: {}", stockPrice);

            // Store the stock price data
            stockDataService.addStockPrice(stockPrice);

            // Broadcast to WebSocket subscribers
            messagingTemplate.convertAndSend("/topic/stock-updates", stockPrice);
            messagingTemplate.convertAndSend("/topic/stock-updates/" + stockPrice.getSymbol(), stockPrice);

            // Broadcast to SSE subscribers
            stockStreamController.broadcastStockPrice(stockPrice);

            logger.debug("Broadcasted stock price via WebSocket and SSE: {}", stockPrice.getSymbol());

        } catch (Exception e) {
            logger.error("Error processing stock price: {}", stockPrice, e);
        }
    }
}
