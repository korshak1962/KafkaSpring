# Stock Consumer - Spring Boot WebSocket & SSE API

This is the **Stock Consumer** component of the Kafka Spring Boot project. It consumes stock price data from Kafka and provides real-time streaming APIs for the React frontend.

## Features

- ✅ **Kafka Consumer**: Consumes stock prices from `stock-prices` topic
- ✅ **WebSocket Support**: Real-time bidirectional communication
- ✅ **Server-Sent Events (SSE)**: HTTP-based streaming
- ✅ **REST API**: Current prices and historical data
- ✅ **CORS Enabled**: Ready for React frontend
- ✅ **In-Memory Storage**: Fast data retrieval (1000 records per symbol)
- ✅ **Multi-Symbol Support**: Handle multiple stock symbols
- ✅ **Connection Management**: Automatic cleanup of disconnected clients

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Kafka running (from docker-compose)
- Stock Producer running

## API Endpoints

### REST API (`/api/stock/*`)
- `GET /api/stock/health` - Health check + statistics
- `GET /api/stock/symbols` - Get all available symbols
- `GET /api/stock/current` - Get all current prices
- `GET /api/stock/current/{symbol}` - Get current price for symbol
- `GET /api/stock/history/{symbol}?limit=100` - Get historical data

### Server-Sent Events (`/api/stream/*`)
- `GET /api/stream/stocks` - Stream all stock price updates
- `GET /api/stream/stocks/{symbol}` - Stream specific symbol updates

### WebSocket (`/ws`)
- `/ws` - WebSocket endpoint (with SockJS)
- `/topic/stock-updates` - Subscribe to all updates
- `/topic/stock-updates/{symbol}` - Subscribe to symbol updates

## Running the Consumer

```bash
# Start consumer (keep producer running)
cd D:\KafkaSpring\stock-consumer
mvn clean spring-boot:run

# Test health endpoint
curl http://localhost:8082/api/stock/health

# Test SSE stream
curl http://localhost:8082/api/stream/stocks
```

## Expected Output

### Console Logs:
```
INFO c.k.s.service.StockConsumerService : Consumed stock price: StockPrice{symbol='AAPL'...}
DEBUG c.k.s.service.StockConsumerService : Broadcasted stock price via WebSocket and SSE: AAPL
```

### Health Response:
```json
{
  "status": "UP",
  "service": "stock-consumer",
  "totalSymbols": 5,
  "totalMessages": 250,
  "symbols": ["AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"]
}
```

## Next Steps

After verifying the Consumer is working:
1. React Frontend with real-time charts
2. WebSocket/SSE integration in React
3. Real-time stock price visualization
