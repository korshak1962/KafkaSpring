package com.korshak.stockconsumer.controller;

import com.korshak.stockconsumer.model.StockPrice;
import com.korshak.stockconsumer.service.StockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket Controller for real-time stock price communication
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private StockDataService stockDataService;

    /**
     * Handle subscription to all stock updates
     */
    @MessageMapping("/subscribe/all")
    @SendTo("/topic/stock-updates")
    public Map<String, StockPrice> subscribeToAllStocks() {
        logger.info("Client subscribed to all stock updates");
        return stockDataService.getAllCurrentPrices();
    }

    /**
     * Handle subscription to specific symbol
     */
    @MessageMapping("/subscribe/{symbol}")
    @SendTo("/topic/stock-updates/{symbol}")
    public StockPrice subscribeToSymbol(@DestinationVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();
        logger.info("Client subscribed to {} updates", upperSymbol);
        return stockDataService.getCurrentPrice(upperSymbol);
    }
}
