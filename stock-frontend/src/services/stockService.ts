import { StockPrice, HealthStatus } from '../types/stock';

const API_BASE_URL = 'http://localhost:8082';

export class StockService {
  private eventSource: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 2000;

  /**
   * Connect to SSE stream for real-time stock updates
   */
  connectToStream(onMessage: (stock: StockPrice) => void, onError?: (error: Event) => void): void {
    if (this.eventSource) {
      this.eventSource.close();
    }

    const url = `${API_BASE_URL}/api/stream/stocks`;
    this.eventSource = new EventSource(url);

    this.eventSource.addEventListener('stock-price', (event) => {
      try {
        const stockPrice: StockPrice = JSON.parse(event.data);
        onMessage(stockPrice);
        this.reconnectAttempts = 0; // Reset on successful message
      } catch (error) {
        console.error('Error parsing stock price:', error);
      }
    });

    this.eventSource.addEventListener('info', (event) => {
      console.log('SSE Info:', event.data);
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE Error:', error);
      
      if (onError) {
        onError(error);
      }

      // Auto-reconnect logic
      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        this.reconnectAttempts++;
        console.log(`Reconnecting... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
        
        setTimeout(() => {
          this.connectToStream(onMessage, onError);
        }, this.reconnectDelay * this.reconnectAttempts);
      } else {
        console.error('Max reconnection attempts reached');
        this.disconnect();
      }
    };

    this.eventSource.onopen = () => {
      console.log('SSE Connection opened');
      this.reconnectAttempts = 0;
    };
  }

  /**
   * Disconnect from SSE stream
   */
  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      console.log('SSE Connection closed');
    }
  }

  /**
   * Get current price for all symbols
   */
  async getCurrentPrices(): Promise<Record<string, StockPrice>> {
    const response = await fetch(`${API_BASE_URL}/api/stock/current`);
    if (!response.ok) {
      throw new Error('Failed to fetch current prices');
    }
    return response.json();
  }

  /**
   * Get current price for specific symbol
   */
  async getCurrentPrice(symbol: string): Promise<StockPrice> {
    const response = await fetch(`${API_BASE_URL}/api/stock/current/${symbol}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch price for ${symbol}`);
    }
    return response.json();
  }

  /**
   * Get historical data for symbol (from in-memory storage)
   * Fast but limited to last 1000 messages
   */
  async getHistory(symbol: string, limit: number = 100): Promise<StockPrice[]> {
    const response = await fetch(`${API_BASE_URL}/api/stock/history/${symbol}?limit=${limit}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch history for ${symbol}`);
    }
    return response.json();
  }

  /**
   * Get historical data directly from Kafka
   * Slower but returns ALL messages stored in Kafka (not just last 1000)
   * Use this to get messages that came before the client connected
   */
  async getKafkaHistory(symbol: string, limit: number = 500): Promise<StockPrice[]> {
    const response = await fetch(`${API_BASE_URL}/api/stock/history/kafka/${symbol}?limit=${limit}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch Kafka history for ${symbol}`);
    }
    return response.json();
  }

  /**
   * Get ALL historical data from Kafka (all symbols)
   */
  async getAllKafkaHistory(limit: number = 1000): Promise<StockPrice[]> {
    const response = await fetch(`${API_BASE_URL}/api/stock/history/kafka/all?limit=${limit}`);
    if (!response.ok) {
      throw new Error('Failed to fetch all Kafka history');
    }
    return response.json();
  }

  /**
   * Get historical data within time range from Kafka
   */
  async getKafkaHistoryByTimeRange(
    symbol: string, 
    from: Date, 
    to: Date
  ): Promise<StockPrice[]> {
    const fromISO = from.toISOString();
    const toISO = to.toISOString();
    const response = await fetch(
      `${API_BASE_URL}/api/stock/history/kafka/${symbol}/range?from=${fromISO}&to=${toISO}`
    );
    if (!response.ok) {
      throw new Error(`Failed to fetch Kafka history by time range for ${symbol}`);
    }
    return response.json();
  }

  /**
   * Get available symbols
   */
  async getSymbols(): Promise<string[]> {
    const response = await fetch(`${API_BASE_URL}/api/stock/symbols`);
    if (!response.ok) {
      throw new Error('Failed to fetch symbols');
    }
    return response.json();
  }

  /**
   * Get health status (includes Kafka message count)
   */
  async getHealth(): Promise<HealthStatus> {
    const response = await fetch(`${API_BASE_URL}/api/stock/health`);
    if (!response.ok) {
      throw new Error('Failed to fetch health status');
    }
    return response.json();
  }

  /**
   * Get Kafka-specific statistics
   */
  async getKafkaStats(): Promise<{ totalMessages: number; topic: string; timestamp: string }> {
    const response = await fetch(`${API_BASE_URL}/api/stock/stats/kafka`);
    if (!response.ok) {
      throw new Error('Failed to fetch Kafka statistics');
    }
    return response.json();
  }
}

export const stockService = new StockService();
