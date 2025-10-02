# Stock Frontend - Real-time Dashboard

React + TypeScript frontend for real-time stock price visualization.

## Features

- 📊 **Real-time Charts** - Live price updates using Recharts
- 🔄 **SSE Connection** - Server-Sent Events for instant updates
- 📈 **Price Cards** - Color-coded stock price displays
- 🎨 **Modern UI** - Tailwind CSS styling
- 📱 **Responsive** - Works on all devices
- 🔌 **Auto-reconnect** - Handles connection drops

## Prerequisites

- Node.js 18+ 
- npm or yarn
- Consumer backend running on localhost:8082

## Installation

```bash
cd D:\KafkaSpring\stock-frontend

# Install dependencies
npm install
```

## Running the Application

```bash
# Development mode
npm run dev

# The app will be available at http://localhost:3000
```

## Build for Production

```bash
npm run build
npm run preview
```

## Technology Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Recharts** - Charting library
- **Tailwind CSS** - Styling
- **EventSource API** - SSE connection

## Project Structure

```
src/
├── components/
│   ├── StockCard.tsx       - Individual stock display
│   ├── StockChart.tsx      - Price chart component
│   └── StockDashboard.tsx  - Main dashboard
├── services/
│   └── stockService.ts     - API & SSE connection
├── types/
│   └── stock.ts            - TypeScript interfaces
├── App.tsx                 - Root component
├── main.tsx                - Entry point
└── index.css               - Global styles
```

## API Endpoints Used

- `GET /api/stock/current` - Current prices
- `GET /api/stock/history/{symbol}` - Historical data
- `GET /api/stream/stocks` - SSE stream

## Features Explained

### Real-time Updates
- Uses Server-Sent Events (SSE) for push notifications
- Auto-reconnects on connection loss
- Shows connection status indicator

### Price Cards
- Green background for positive changes
- Red background for negative changes
- Click to select and view chart

### Charts
- Real-time line chart using Recharts
- Shows last 50 data points
- Auto-updates as new data arrives
- Smooth animations

## Troubleshooting

### Connection Issues
If you see "disconnected" status:
1. Ensure Consumer is running on port 8082
2. Check browser console for errors
3. Verify CORS is enabled on backend

### No Data Showing
1. Make sure Producer is sending data
2. Check Consumer logs for incoming messages
3. Refresh the page

## Next Steps

This completes the full-stack real-time data pipeline:
- Producer → Kafka → Consumer → React Frontend
