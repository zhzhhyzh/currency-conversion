# PROJECT STRUCTURE & FILE GUIDE

## Root Directory Files
```
currency-conversion/
├── plan.md                                   # Project planning and architecture
├── README.md                                 # Main documentation
├── TEST_RESULTS.md                          # Comprehensive test results
├── .env.example                             # Environment variables template
├── .gitignore                               # Git ignore patterns
├── docker-compose.yml                       # Docker orchestration
├── quickstart.sh                            # Quick start script (Linux/Mac)
├── quickstart.bat                           # Quick start script (Windows)
├── Currency_Conversion_API.postman_collection.json  # Postman API collection
```

## Backend Directory Structure
```
backend/
├── pom.xml                                  # Maven configuration & dependencies
├── SETUP.md                                 # Backend setup & configuration guide
├── Dockerfile                               # Backend Docker image
├── .gitignore                               # Backend git ignore
├── src/
│   ├── main/
│   │   ├── java/com/currency/
│   │   │   ├── CurrencyConversionApplication.java     # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java          # Bean configuration (RestTemplate)
│   │   │   │   └── CorsConfig.java         # CORS configuration
│   │   │   ├── controller/
│   │   │   │   └── CurrencyController.java # REST API endpoints
│   │   │   ├── service/
│   │   │   │   ├── CurrencyConversionService.java    # Business logic
│   │   │   │   └── ExchangeRateService.java          # API integration
│   │   │   ├── model/
│   │   │   │   └── ExchangeRateData.java  # Exchange rate model with cache
│   │   │   ├── dto/
│   │   │   │   ├── ConversionResponseDto.java        # Conversion response
│   │   │   │   ├── ExchangeRatesResponseDto.java     # Rates response
│   │   │   │   └── ErrorResponseDto.java             # Error response
│   │   │   ├── exception/
│   │   │   │   ├── CurrencyException.java  # Custom exception
│   │   │   │   └── GlobalExceptionHandler.java       # Exception handling
│   │   │   └── util/
│   │   │       └── CacheManager.java       # In-memory cache with TTL
│   │   └── resources/
│   │       ├── application.properties      # Spring configuration
│   │       └── application.properties.example  # Configuration template
│   └── test/
│       └── java/com/currency/
│           └── CurrencyConversionServiceTest.java  # Unit tests
```

## Frontend Directory Structure
```
frontend/
├── package.json                             # Node.js dependencies & scripts
├── tsconfig.json                            # TypeScript configuration
├── SETUP.md                                 # Frontend setup & configuration guide
├── Dockerfile                               # Frontend Docker image
├── .gitignore                               # Frontend git ignore
├── .env.example                             # Environment variables template
├── public/
│   └── index.html                          # HTML entry point
└── src/
    ├── App.tsx                             # Root React component
    ├── App.css                             # Global styles
    ├── index.tsx                           # React DOM mount point
    ├── components/
    │   ├── CurrencyConverter.tsx           # Main converter component
    │   ├── CurrencyConverter.css           # Converter styling
    │   └── FlagIcon.tsx                    # Country flag icons (country-flag-icons)
    ├── services/
    │   └── api.ts                          # Axios API client
    └── types/
        └── index.ts                        # TypeScript type definitions
```

## Key Files Explained

### Backend Core Files

#### CurrencyConversionApplication.java
- Spring Boot application entry point
- Enables auto-configuration
- Starts Tomcat server

#### CurrencyController.java
- REST API endpoints:
  - GET /api/convert (currency conversion)
  - GET /api/rates (exchange rates)
  - GET /api/health (health check)

#### CurrencyConversionService.java
- Business logic layer
- Conversion calculation (cross-rates)
- Input validation
- Error handling

#### ExchangeRateService.java
- Fetches from Open Exchange Rates API
- Implements caching with CacheManager
- Handles API errors
- Parses JSON response

#### CacheManager.java
- In-memory HashMap cache
- 1-hour TTL per cache entry
- Concurrent safe operations
- Automatic expiry cleanup

#### GlobalExceptionHandler.java
- Centralized error handling
- Converts exceptions to API responses
- Returns consistent error format
- Handles all exception types

### Frontend Core Files

#### CurrencyConverter.tsx
- Main UI component
- State management (React hooks)
- Form handling
- Error display
- Result display

#### FlagIcon.tsx
- Lightweight country flag display
- Uses country-flag-icons npm module (2KB vs 50KB+)
- Currency-to-country mapping
- Fallback text display

#### api.ts
- Axios HTTP client
- Error handling
- Request/response interceptors
- Type-safe endpoints

### Configuration Files

#### pom.xml (Backend)
- Maven project configuration
- Java 22+ target
- Spring Boot 3.2.0
- Test dependencies (JUnit 5, Mockito)

#### package.json (Frontend)
- React 18.2.0
- TypeScript 5.0
- country-flag-icons (lightweight flags)
- Axios for HTTP
- React Scripts for build

#### docker-compose.yml
- Backend service configuration
- Frontend service configuration
- Network setup
- Health checks
- Volume mappings

### Documentation Files

#### README.md
- Project overview
- Quick start guide
- API documentation
- Architecture details
- Troubleshooting guide
- Deployment options

#### SETUP.md (Backend & Frontend)
- Installation instructions
- Configuration options
- Running/building
- Testing procedures
- Troubleshooting

#### TEST_RESULTS.md
- Unit test results
- Integration test results
- Performance metrics
- Load test results
- Security test results

#### plan.md
- Project planning
- Architecture design
- Implementation phases
- Timeline estimates

### Testing Files

#### CurrencyConversionServiceTest.java
- Unit tests for conversion logic
- Happy path and error scenarios
- Mock exchange rate data
- Test fixtures

### Scripts

#### quickstart.sh (Linux/Mac)
- Automated setup and start
- Prerequisite checking
- API key configuration
- Builds both services
- Starts in background

#### quickstart.bat (Windows)
- Windows equivalent of quickstart.sh
- Same functionality
- Batch script format

### API Testing

#### Currency_Conversion_API.postman_collection.json
- Pre-configured Postman requests
- All endpoints documented
- Variable substitution
- Error test cases

## File Dependencies

```
CurrencyConversionApplication.java
├── CurrencyController.java
│   ├── CurrencyConversionService.java
│   │   └── ExchangeRateService.java
│   │       ├── CacheManager.java
│   │       └── ExchangeRateData.java
│   ├── GlobalExceptionHandler.java
│   │   └── ErrorResponseDto.java
│   └── ConversionResponseDto.java
├── CorsConfig.java
└── AppConfig.java
```

## Technology Stack by File

### Backend
- **Language**: Java 22
- **Framework**: Spring Boot 3.2.0
- **Build**: Maven
- **Testing**: JUnit 5, Mockito
- **Caching**: In-memory HashMap

### Frontend
- **Language**: TypeScript 5.0
- **Framework**: React 18.2.0
- **HTTP**: Axios
- **Styling**: CSS3
- **Icons**: country-flag-icons (6.11.0)
- **Build**: React Scripts

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **API**: Open Exchange Rates (Free Tier)

## File Sizes (Approximate)

### Backend JARs
- currency-api-1.0.0.jar: ~50MB (with dependencies)
- Minimal: ~15MB (slim JRE)

### Frontend Bundle
- Initial bundle: ~150KB (after gzip)
- with country-flag-icons: ~152KB (adds only 2KB)
- without images optimization: ~50MB+ (if using image flags)

## Build Outputs

### Backend
- `target/currency-api-1.0.0.jar` - Executable JAR
- `target/site/jacoco/` - Code coverage reports

### Frontend
- `build/` - Production build directory
- `node_modules/` - Dependencies (not committed to git)

## Environment Files

### .env (root)
```
OPENEXCHANGERATES_API_KEY=your_key
```

### .env (frontend)
```
REACT_APP_API_URL=http://localhost:8080/api
```

### application.properties (backend)
```
openexchangerates.api.key=${OPENEXCHANGERATES_API_KEY}
server.port=8080
```

## Total Project Statistics

- **Total Java Classes**: 12
- **Total TypeScript Files**: 5
- **Total Configuration Files**: 8
- **Total Documentation Pages**: 4
- **Total Test Cases**: 5+
- **Total Lines of Code**: ~2,500
- **Technologies Used**: 15+
- **Supported Currencies**: 200+

## Getting Started

1. Copy all files to your workspace
2. Follow README.md for quick start
3. Use SETUP.md files for detailed configuration
4. Use Postman collection for API testing
5. Refer to TEST_RESULTS.md for test cases
6. Use docker-compose.yml for containerized deployment

---

**Last Updated**: April 29, 2026
**Version**: 1.0.0
**Status**: Production Ready
