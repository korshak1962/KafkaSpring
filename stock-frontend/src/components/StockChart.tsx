import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { StockPrice } from '../types/stock';

interface StockChartProps {
  data: StockPrice[];
  symbol: string;
}

export const StockChart: React.FC<StockChartProps> = ({ data, symbol }) => {
  // Transform data for recharts
  const chartData = data.map((stock) => ({
    time: new Date(stock.timestamp).toLocaleTimeString(),
    price: stock.price,
    change: stock.change,
  }));

  // Determine line color based on overall trend
  const firstPrice = data[0]?.price || 0;
  const lastPrice = data[data.length - 1]?.price || 0;
  const lineColor = lastPrice >= firstPrice ? '#10b981' : '#ef4444';

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h3 className="text-xl font-bold text-gray-800 mb-4">{symbol} Price Chart</h3>
      
      {data.length === 0 ? (
        <div className="flex items-center justify-center h-64">
          <p className="text-gray-500">Waiting for data...</p>
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis 
              dataKey="time" 
              stroke="#6b7280"
              tick={{ fontSize: 12 }}
              angle={-45}
              textAnchor="end"
              height={80}
            />
            <YAxis 
              stroke="#6b7280"
              tick={{ fontSize: 12 }}
              domain={['auto', 'auto']}
              tickFormatter={(value) => `$${value.toFixed(2)}`}
            />
            <Tooltip 
              contentStyle={{ 
                backgroundColor: '#fff', 
                border: '1px solid #e5e7eb',
                borderRadius: '0.5rem',
                padding: '0.75rem'
              }}
              formatter={(value: number) => [`$${value.toFixed(2)}`, 'Price']}
            />
            <Legend />
            <Line 
              type="monotone" 
              dataKey="price" 
              stroke={lineColor}
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 6 }}
              name={`${symbol} Price`}
            />
          </LineChart>
        </ResponsiveContainer>
      )}
      
      <div className="mt-4 text-sm text-gray-600">
        <p>Showing last {data.length} data points</p>
      </div>
    </div>
  );
};
