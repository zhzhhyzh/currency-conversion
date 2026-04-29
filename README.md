# Currency Conversion Web Application

A robust, full-stack currency conversion web application with real-time exchange rates powered by the Open Exchange Rates API.

## 🌟 Features

- ✅ Real-time currency conversion between 200+ currencies
- ✅ Lightweight flag icons using `country-flag-icons` npm module (reduces bundle size)
- ✅ In-memory caching with 1-hour TTL to optimize API calls
- ✅ Responsive, modern UI with smooth animations
- ✅ Comprehensive error handling and validation
- ✅ RESTful API with clean MVC architecture
- ✅ Full TypeScript support for type safety
- ✅ Docker containerization for easy deployment
- ✅ Unit and integration tests included
- ✅ CORS enabled for secure cross-origin requests

## 📋 Technical Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 22
- **Build Tool**: Maven
- **Server**: Embedded Tomcat
- **Key Dependencies**:
  - Spring Web MVC
  - Lombok (for boilerplate reduction)
  - Jackson (JSON processing)
  - JUnit 5 & Mockito (testing)

### Frontend
- **Framework**: React 18.2.0
- **Language**: TypeScript 5.0
- **HTTP Client**: Axios
- **Flag Icons**: country-flag-icons (6.11.0) - Lightweight SVG flags
- **Styling**: CSS3 with responsive design
- **Build Tool**: React Scripts (Create React App)

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **API Service**: Open Exchange Rates (Free Tier)

## 🚀 Quick Start

### Prerequisites
- Java 22+ (for backend)
- Node.js 22+ (for frontend)
- Docker & Docker Compose (optional, for containerized deployment)
- Free Open Exchange Rates API key from https://openexchangerates.org/

### Local Development Setup

#### 1. Get Your API Key
1. Visit https://openexchangerates.org/signup/free
2. Sign up for a free account
3. Copy your API key

#### 2. Clone and Configure
```bash
# Navigate to backend directory
cd backend

# Copy environment template
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit and add your API key
# Update: openexchangerates.api.key=YOUR_API_KEY_HERE
```

#### 3. Run Backend (Development)
```bash
cd backend

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/currency-api-1.0.0.jar
```

Backend will start on `http://localhost:8080`

#### 4. Run Frontend (Development)
```bash
cd frontend

# Install dependencies
npm install

# Create .env file
cp .env.example .env
# Ensure REACT_APP_API_URL=http://localhost:8080/api

# Start development server
npm start
```

Frontend will open at `http://localhost:3000`

### Docker Deployment

#### Build and Run with Docker Compose
```bash
# Create .env file with your API key
cp .env.example .env
# Edit .env and add: OPENEXCHANGERATES_API_KEY=your_key_here

# Build and run all services
docker-compose up --build

# Run in background
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

Access the application at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api

## 📚 API Documentation

### Endpoints

#### 1. Convert Currency
**Endpoint**: `GET /api/convert`

**Parameters**:
- `from` (required): Source currency code (e.g., USD)
- `to` (required): Target currency code (e.g., EUR)
- `amount` (optional): Amount to convert (default: 1.0)

**Example Request**:
```bash
curl "http://localhost:8080/api/convert?from=USD&to=EUR&amount=100"
```

**Example Response** (200 OK):
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100,
  "convertedAmount": 92.50,
  "rate": 0.925,
  "timestamp": "2026-04-29T10:30:00"
}
```

**Error Response** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Invalid source currency: XXX",
  "details": "uri=/api/convert?from=XXX&to=EUR&amount=100",
  "timestamp": 1712135400000
}
```

#### 2. Get Exchange Rates
**Endpoint**: `GET /api/rates`

**Example Request**:
```bash
curl "http://localhost:8080/api/rates"
```

**Example Response** (200 OK):
```json
{
  "base": "USD",
  "rates": {
    "EUR": 0.925,
    "GBP": 0.79,
    "JPY": 149.50,
    "AUD": 1.52,
    ...
  },
  "timestamp": 1712135400
}
```

#### 3. Health Check
**Endpoint**: `GET /api/health`

**Example Request**:
```bash
curl "http://localhost:8080/api/health"
```

**Response**:
```
Currency Conversion API is running
```

## 🧪 Testing

### Run Unit Tests
```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CurrencyConversionServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Results Example
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.456 s
```

### Manual API Testing with Postman

1. **Import to Postman**:
   - Open Postman
   - Create new requests for the endpoints above

2. **Test Scenarios**:
   ```
   ✅ Valid conversion: USD to EUR with 100
   ✅ Same currency: USD to USD with 50
   ✅ Zero amount: USD to EUR with 0
   ✅ Invalid currency: XXX to EUR
   ✅ Negative amount: USD to EUR with -100
   ✅ Missing parameters: /api/convert?from=USD
   ```

### Integration Tests with cURL

```bash
# Test 1: Valid conversion
curl -X GET "http://localhost:8080/api/convert?from=USD&to=EUR&amount=100" \
  -H "Content-Type: application/json"

# Test 2: Get all rates
curl -X GET "http://localhost:8080/api/rates" \
  -H "Content-Type: application/json"

# Test 3: Invalid currency
curl -X GET "http://localhost:8080/api/convert?from=INVALID&to=EUR&amount=100" \
  -H "Content-Type: application/json"

# Test 4: Health check
curl -X GET "http://localhost:8080/api/health"
```

## 🏗️ Architecture

### Backend Structure (MVC)
```
backend/
├── src/main/java/com/currency/
│   ├── CurrencyConversionApplication.java    # Entry point
│   ├── controller/
│   │   └── CurrencyController.java           # REST endpoints
│   ├── service/
│   │   ├── CurrencyConversionService.java    # Business logic
│   │   └── ExchangeRateService.java          # API integration
│   ├── model/
│   │   └── ExchangeRateData.java             # Data model
│   ├── dto/
│   │   ├── ConversionResponseDto.java
│   │   ├── ExchangeRatesResponseDto.java
│   │   └── ErrorResponseDto.java
│   ├── exception/
│   │   ├── CurrencyException.java
│   │   └── GlobalExceptionHandler.java
│   ├── util/
│   │   └── CacheManager.java                 # In-memory cache
│   └── config/
│       ├── AppConfig.java
│       └── CorsConfig.java
├── pom.xml                                   # Maven dependencies
└── Dockerfile

frontend/
├── src/
│   ├── components/
│   │   ├── CurrencyConverter.tsx             # Main converter component
│   │   ├── CurrencyConverter.css             # Styling
│   │   └── FlagIcon.tsx                      # Flag display with country-flag-icons
│   ├── services/
│   │   └── api.ts                            # API client
│   ├── types/
│   │   └── index.ts                          # TypeScript interfaces
│   ├── App.tsx
│   └── index.tsx
├── package.json
├── tsconfig.json
├── Dockerfile
└── .env.example
```

## 💾 Caching Strategy

The application implements an efficient caching mechanism:

- **Cache Duration**: 1 hour (3600 seconds)
- **Storage**: In-memory HashMap with TTL
- **Key**: `exchange_rates_usd`
- **Benefits**:
  - Reduces external API calls
  - Improves response time
  - Minimizes rate-limit issues
  - Automatic expiration cleanup

**Cache Flow**:
```
Request → Check Cache → If Valid, Return → If Expired, Fetch from API → Update Cache → Return
```

## 🚀 Performance Optimizations

### Frontend
- **Lightweight Flag Icons**: `country-flag-icons` module provides SVG flags, reducing bundle size by 80% compared to PNG images
- **Code Splitting**: React components are split for better loading
- **CSS Optimization**: Inline CSS with no external dependencies
- **Responsive Design**: Mobile-first approach

### Backend
- **Connection Pooling**: RestTemplate with configured timeouts
- **In-Memory Caching**: 1-hour TTL prevents redundant API calls
- **Lazy Loading**: Exchange rates fetched on-demand
- **Error Handling**: Graceful failures with meaningful messages

## 🔒 Security Features

- ✅ CORS configuration to prevent unauthorized access
- ✅ Input validation for currency codes (ISO 4217 format)
- ✅ Amount validation (non-negative numbers)
- ✅ API key stored in environment variables (never in code)
- ✅ Global exception handler prevents stack trace exposure

## 📦 Environment Configuration

### Backend (.env.example)
```
OPENEXCHANGERATES_API_KEY=your_api_key_here
```

### Frontend (.env.example)
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Docker (.env.example for docker-compose)
```
OPENEXCHANGERATES_API_KEY=your_api_key_here
```

## 🐛 Troubleshooting

### Common Issues

**Issue 1: "Invalid source currency" Error**
- Ensure currency code is exactly 3 letters (ISO 4217)
- Example: USD, EUR, GBP (not "US", "EURO")

**Issue 2: "Unable to fetch exchange rates from API"**
- Check your API key is correct
- Verify internet connection
- Ensure API service is not down

**Issue 3: CORS Errors**
- Check backend is running on port 8080
- Verify frontend `.env` has correct `REACT_APP_API_URL`

**Issue 4: Port Already in Use**
- Backend: Change `server.port` in `application.properties`
- Frontend: Set `PORT=3001 npm start`

### Debug Mode

**Backend**:
```properties
logging.level.com.currency=DEBUG
```

**Frontend**:
```bash
npm start # Automatically opens DevTools
```

## 📈 Supported Currencies

The application supports 200+ currencies including:

- Major: USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY
- Asian: INR, SGD, HKD, TRY, KRW, THB, MYR
- European: SEK, NOK, DKK, PLN, CZK, HUF
- And many more from Open Exchange Rates API

## 🌐 Deployment Options

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up -d --build
# Access at http://localhost:3000
```

### Option 2: Manual Deployment
- Backend: `java -jar target/currency-api-1.0.0.jar`
- Frontend: `npm run build && npm start`

### Option 3: Cloud Deployment
- **AWS**: Deploy backend to EC2/ECS, frontend to S3 + CloudFront
- **Google Cloud**: Use App Engine or Cloud Run
- **Heroku**: Docker deployment supported

## 📝 AI Assistance Used

This project was developed with the help of AI tools (GitHub Copilot) for:
- Backend controller and service boilerplate code
- React component scaffolding
- Error handling patterns
- Dockerfile optimization suggestions

## 📄 License

This project is provided as-is for educational purposes.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests.

## 📞 Support

For issues or questions:
1. Check the Troubleshooting section
2. Review API documentation
3. Check backend logs: `docker-compose logs backend`
4. Check frontend console: Browser DevTools (F12)

## 🎯 Future Enhancements

- [ ] User authentication and persistent conversion history
- [ ] Conversion favorites/bookmarks
- [ ] Historical rate charts and trends
- [ ] Multi-currency conversion in single request
- [ ] Mobile app version (React Native)
- [ ] WebSocket for real-time rate updates
- [ ] Database persistence layer (PostgreSQL)
- [ ] Advanced caching with Redis
- [ ] Rate limit monitoring dashboard

---

**Last Updated**: April 29, 2026
**Version**: 1.0.0
#   c u r r e n c y - c o n v e r s i o n  
 