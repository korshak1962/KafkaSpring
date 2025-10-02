import React from 'react';
import { StockPrice } from '../types/stock';

interface StockCardProps {
  stock: StockPrice;
}

export const StockCard: React.FC<StockCardProps> = ({ stock }) => {
  const isPositive = stock.change >= 0;
  const changeColor = isPositive ? 'text-green-600' : 'text-red-600';
  const bgColor = isPositive ? 'bg-green-50' : 'bg-red-50';
  const arrow = isPositive ? '▲' : '▼';

  return (
    <div className={`${bgColor} rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow`}>
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-2xl font-bold text-gray-800">{stock.symbol}</h3>
          <p className="text-xs text-gray-500 mt-1">
            {new Date(stock.timestamp).toLocaleTimeString()}
          </p>
        </div>
        <div className={`${changeColor} text-lg font-semibold`}>
          {arrow}
        </div>
      </div>
      
      <div className="space-y-2">
        <div>
          <p className="text-3xl font-bold text-gray-900">
            ${stock.price.toFixed(2)}
          </p>
        </div>
        
        <div className="flex items-center space-x-3">
          <span className={`${changeColor} text-sm font-medium`}>
            {isPositive ? '+' : ''}{stock.change.toFixed(2)}
          </span>
          <span className={`${changeColor} text-sm font-medium`}>
            ({isPositive ? '+' : ''}{stock.changePercent.toFixed(2)}%)
          </span>
        </div>
      </div>
    </div>
  );
};
