export interface StockPrice {
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  timestamp: string;
}

export interface StockHistory {
  symbol: string;
  data: StockPrice[];
}

export interface HealthStatus {
  status: string;
  service: string;
  totalMessages: number;
  totalSymbols: number;
  symbols: string[];
  timestamp: string;
}
