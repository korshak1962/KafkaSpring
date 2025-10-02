# Stock Producer - Kafka Spring Boot Application

This is the **Stock Producer** component of the Kafka Spring Boot project. It generates fake stock price data and streams it to a Kafka topic.

## Features

- ✅ Generates monotonically increasing stock prices with realistic fluctuations
- ✅ Supports multiple stock symbols (AAPL, GOOGL, MSFT, AMZN, TSLA)
- ✅ Configurable price generation parameters
- ✅ Sends data to Kafka topic `stock-prices`
- ✅ REST endpoints for monitoring
- ✅ Comprehensive logging

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Apache Kafka running on localhost:9092

## Configuration

The application can be configured via `application.properties`:

```properties
# Server configuration
server.port=8081

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092

# Stock Producer Settings
stock.producer.topic=stock-prices
stock.producer.interval=1000          # Generation interval in ms
stock.producer.initial-price=100.0    # Starting stock price
stock.producer.max-change=5.0         # Maximum price change per update
```

## Running the Application

### 1. Start Kafka
Make sure Kafka is running on localhost:9092. If you're using Docker:

```bash
# Start Zookeeper
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest

# Start Kafka
docker run -d --name kafka -p 9092:9092 \
  --link zookeeper:zookeeper \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
```

### 2. Run the Producer

```bash
# Build and run
mvn clean spring-boot:run

# Or build jar and run
mvn clean package
java -jar target/stock-producer-0.0.1-SNAPSHOT.jar
```

## API Endpoints

- **Health Check**: `GET http://localhost:8081/api/producer/health`
- **Current Price**: `GET http://localhost:8081/api/producer/current-price`

## Generated Data Format

The producer generates JSON messages in this format:

```json
{
  "symbol": "AAPL",
  "price": 105.25,
  "change": 2.15,
  "changePercent": 2.08,
  "timestamp": "2024-01-15T10:30:45"
}
```

## Kafka Topic

- **Topic Name**: `stock-prices`
- **Key**: Stock symbol (string)
- **Value**: StockPrice object (JSON)

## Logging

The application provides detailed logging:
- Stock price generation events
- Kafka send confirmations
- Error handling and retries

## Next Steps

After reviewing this producer, we'll create:
1. **Spring Boot Consumer** (with SSE/WebSocket API)
2. **React TypeScript Frontend** (with real-time charts)
