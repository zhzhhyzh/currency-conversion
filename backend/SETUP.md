# Backend API Configuration

## Setup Instructions

### 1. Prerequisites
- Java 1.8 or higher
- Maven 3.8.0 or higher

### 2. Configuration

#### Option A: Environment Variables
Set the following environment variable:
```bash
export OPENEXCHANGERATES_API_KEY=your_api_key_here
```

#### Option B: application.properties File
Edit `src/main/resources/application.properties`:
```properties
openexchangerates.api.key=your_api_key_here
openexchangerates.api.url=https://openexchangerates.org/api/latest.json
server.port=8080
```

#### Option C: Command Line
```bash
java -jar target/currency-api-1.0.0.jar \
  --openexchangerates.api.key=your_api_key_here
```

### 3. Building

```bash
# Clean build
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build with specific profile
mvn clean install -P production
```

### 4. Running

#### Development Mode
```bash
mvn spring-boot:run
```

#### Production Mode
```bash
java -jar target/currency-api-1.0.0.jar
```

#### Docker
```bash
docker build -t currency-api:latest .
docker run -p 8080:8080 \
  -e OPENEXCHANGERATES_API_KEY=your_key \
  currency-api:latest
```

## API Key Setup

### Getting Free API Key
1. Visit https://openexchangerates.org/signup/free
2. Create account with email
3. Verify email
4. Copy API key from dashboard
5. Add to `application.properties` or environment variable

### Free Tier Limits
- **Requests per Month**: 1,500
- **Update Frequency**: Monthly
- **Base Currency**: USD only
- **Rounding**: 2-5 decimal places

## Testing

### Unit Tests
```bash
mvn test

# Run specific test
mvn test -Dtest=CurrencyConversionServiceTest

# Skip tests during build
mvn package -DskipTests
```

### Integration Tests
```bash
mvn verify

# With code coverage
mvn clean test jacoco:report
# Report location: target/site/jacoco/index.html
```

### API Testing

#### Health Check
```bash
curl http://localhost:8080/api/health
```

#### Convert Currency
```bash
curl "http://localhost:8080/api/convert?from=USD&to=EUR&amount=100"
```

#### Get Exchange Rates
```bash
curl http://localhost:8080/api/rates
```

## Logging Configuration

### Default Logging
```properties
logging.level.root=INFO
logging.level.com.currency=DEBUG
```

### Change Log Level
- Update `application.properties`
- Restart application

### View Logs
```bash
# Real-time logs
tail -f logs/application.log

# Last 100 lines
tail -100 logs/application.log

# Docker logs
docker logs -f currency-api
```

## Performance Tuning

### JVM Arguments
```bash
java -Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -jar target/currency-api-1.0.0.jar
```

### Connection Pool
Modify in `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

## Troubleshooting

### Issue: Invalid API Key
**Error**: `API Error: invalid app_id`
**Solution**: Verify API key in environment variable or properties file

### Issue: Rate Limit Exceeded
**Error**: `API Error: rate limit exceeded`
**Solution**: Wait for month to reset or upgrade subscription

### Issue: Connection Timeout
**Error**: `Unable to fetch exchange rates from API`
**Solution**: Check internet connection, verify API service is up

### Issue: Port Already in Use
**Error**: `Address already in use: 8080`
**Solution**: Change port in `application.properties` or kill process on port 8080

## Production Deployment

### Recommended Settings
```properties
# Server
server.port=8080
server.servlet.context-path=/api

# API
openexchangerates.api.key=${OPENEXCHANGERATES_API_KEY}

# Logging
logging.level.root=WARN
logging.level.com.currency=INFO

# Cache
cache.duration.seconds=3600
```

### Docker Deployment
```bash
# Build image
docker build -t currency-api:1.0.0 .

# Run container
docker run -d \
  --name currency-api \
  -p 8080:8080 \
  -e OPENEXCHANGERATES_API_KEY=your_key \
  currency-api:1.0.0

# View logs
docker logs currency-api

# Stop container
docker stop currency-api
```

## Monitoring

### Health Endpoint
```bash
curl http://localhost:8080/api/health
```

### Metrics (if enabled)
```bash
curl http://localhost:8080/actuator/metrics
```

### Custom Monitoring
Extend `GlobalExceptionHandler` to log metrics to monitoring service.
