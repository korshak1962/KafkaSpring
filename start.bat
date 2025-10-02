@echo off
echo Starting Kafka Stock Producer Project...
echo.

echo 1. Starting Kafka with Docker Compose...
docker-compose up -d

echo.
echo 2. Waiting for Kafka to be ready...
timeout /t 10 /nobreak > nul

echo.
echo 3. Starting Stock Producer...
cd stock-producer
start mvn clean spring-boot:run

echo.
echo Services starting:
echo - Kafka: http://localhost:9092
echo - Kafka UI: http://localhost:8080
echo - Stock Producer API: http://localhost:8081/api/producer/health
echo.
echo Press any key to exit...
pause > nul
