import React, { useState, useEffect } from 'react';
import { StockPrice } from '../types/stock';
import { stockService } from '../services/stockService';
import { StockCard } from './StockCard';
import { StockChart } from './StockChart';

export const StockDashboard: React.FC = () => {
  const [stocks, setStocks] = useState<Record<string, StockPrice>>({});
  const [history, setHistory] = useState<Record<string, StockPrice[]>>({});
  const [selectedSymbol, setSelectedSymbol] = useState<string>('AAPL');
  const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'disconnected'>('connecting');
  const [totalMessages, setTotalMessages] = useState<number>(0);

  const MAX_HISTORY_POINTS = 50;

  useEffect(() => {
    // Fetch initial data
    const fetchInitialData = async () => {
      try {
        const currentPrices = await stockService.getCurrentPrices();
        setStocks(currentPrices);
        
        // Fetch history for each symbol
        const symbols = Object.keys(currentPrices);
        for (const symbol of symbols) {
          const symbolHistory = await stockService.getHistory(symbol, MAX_HISTORY_POINTS);
          setHistory(prev => ({ ...prev, [symbol]: symbolHistory }));
        }
        
        // Set default selected symbol if available
        if (symbols.length > 0 && !stocks[selectedSymbol]) {
          setSelectedSymbol(symbols[0]);
        }
      } catch (error) {
        console.error('Error fetching initial data:', error);
      }
    };

    fetchInitialData();

    // Connect to SSE stream
    stockService.connectToStream(
      (stockPrice) => {
        // Update current prices
        setStocks(prev => ({
          ...prev,
          [stockPrice.symbol]: stockPrice
        }));

        // Update history
        setHistory(prev => {
          const symbolHistory = prev[stockPrice.symbol] || [];
          const updatedHistory = [...symbolHistory, stockPrice];
          
          // Keep only last MAX_HISTORY_POINTS
          if (updatedHistory.length > MAX_HISTORY_POINTS) {
            updatedHistory.shift();
          }
          
          return {
            ...prev,
            [stockPrice.symbol]: updatedHistory
          };
        });

        setTotalMessages(prev => prev + 1);
        setConnectionStatus('connected');
      },
      (error) => {
        console.error('SSE Error:', error);
        setConnectionStatus('disconnected');
      }
    );

    // Cleanup on unmount
    return () => {
      stockService.disconnect();
    };
  }, []);

  const stockList = Object.values(stocks).sort((a, b) => a.symbol.localeCompare(b.symbol));
  const chartData = history[selectedSymbol] || [];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Real-time Stock Dashboard</h1>
              <p className="text-sm text-gray-600 mt-1">Powered by Kafka Stream Processing</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center">
                <div className={`h-3 w-3 rounded-full mr-2 ${
                  connectionStatus === 'connected' ? 'bg-green-500' : 
                  connectionStatus === 'connecting' ? 'bg-yellow-500 animate-pulse' : 
                  'bg-red-500'
                }`}></div>
                <span className="text-sm text-gray-600 capitalize">{connectionStatus}</span>
              </div>
              <div className="text-sm text-gray-600">
                Messages: <span className="font-semibold">{totalMessages}</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stock Cards Grid */}
        <div className="mb-8">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Current Prices</h2>
          {stockList.length === 0 ? (
            <div className="bg-white rounded-lg shadow-md p-8 text-center">
              <div className="animate-pulse">
                <div className="h-4 bg-gray-300 rounded w-3/4 mx-auto mb-4"></div>
                <div className="h-4 bg-gray-300 rounded w-1/2 mx-auto"></div>
              </div>
              <p className="text-gray-500 mt-4">Loading stock data...</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
              {stockList.map((stock) => (
                <div 
                  key={stock.symbol} 
                  onClick={() => setSelectedSymbol(stock.symbol)}
                  className={`cursor-pointer transform transition-transform hover:scale-105 ${
                    selectedSymbol === stock.symbol ? 'ring-4 ring-blue-500' : ''
                  }`}
                >
                  <StockCard stock={stock} />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Chart Section */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-800">Price History</h2>
            <select
              value={selectedSymbol}
              onChange={(e) => setSelectedSymbol(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              {stockList.map((stock) => (
                <option key={stock.symbol} value={stock.symbol}>
                  {stock.symbol}
                </option>
              ))}
            </select>
          </div>
          <StockChart data={chartData} symbol={selectedSymbol} />
        </div>

        {/* Footer Info */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-3">About This Dashboard</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-600">
            <div>
              <p className="font-semibold text-gray-800 mb-1">Data Source</p>
              <p>Kafka message broker streaming real-time stock prices</p>
            </div>
            <div>
              <p className="font-semibold text-gray-800 mb-1">Update Frequency</p>
              <p>Real-time updates via Server-Sent Events (SSE)</p>
            </div>
            <div>
              <p className="font-semibold text-gray-800 mb-1">Technology Stack</p>
              <p>React + TypeScript + Recharts + Tailwind CSS</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
