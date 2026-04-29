# Currency Conversion Web Application

Full-stack currency converter using Spring Boot, React, TypeScript, Redis, Docker, and the Open Exchange Rates free-tier `latest.json` endpoint.

## Features

- `GET /api/convert?from=USD&to=EUR&amount=100`
- `GET /api/rates` refreshes latest rates from Open Exchange Rates and updates Redis
- Local conversion calculation because the Open Exchange Rates `/convert` endpoint is restricted on the free plan
- Redis cache for latest rates, expiring at the next GMT+8 currency cutoff
- Demand-driven API usage: first conversion fills Redis, later conversions read cached rates
- In-memory fallback cache when Redis is unavailable
- React TypeScript frontend with lightweight SVG flags from `country-flag-icons`
- Docker Compose setup for frontend, backend, and Redis
- Backend unit tests

## Requirements

- Docker and Docker Compose
- Free Open Exchange Rates API key from [openexchangerates.org](https://openexchangerates.org/signup/free)

## Configuration

Create a root `.env` file for Docker Compose:

```env
OPENEXCHANGERATES_API_KEY=your_api_key_here
```

Redis defaults:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
redis.enabled=true
exchange-rates.schedule.enabled=false
```

Docker Compose builds the frontend with `REACT_APP_API_URL=http://localhost:8081/api`.

## Run With Docker

```bash
docker compose up -d --build
```

URLs:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8081/api`
- Redis: `localhost:6379`

Stop the stack:

```bash
docker compose down
```

## API Examples

Convert currency:

```bash
curl "http://localhost:8081/api/convert?from=USD&to=EUR&amount=100"
```

Example response:

```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "convertedAmount": 92.5,
  "rate": 0.925,
  "timestamp": "2026-04-29T06:00:00"
}
```

Refresh cache from Open Exchange Rates and return all latest rates:

```bash
curl "http://localhost:8081/api/rates"
```

Health check:

```bash
curl "http://localhost:8081/api/health"
```

Cache status:

```bash
curl "http://localhost:8081/api/cache/status"
```

## Redis Cutoff Cache

Open Exchange Rates returns USD-based rates for all currencies. The backend stores the fetched rate payload in Redis and calculates a TTL to expire at the nearest next cutoff time in GMT+8 among the supported currencies.

The backend checks Redis first for every conversion. If Redis contains `exchange_rates:exchange_rates_usd`, no Open Exchange Rates request is made. If Redis misses, the backend fetches `latest.json`, stores it in Redis, and calculates the conversion locally.

Calling `/api/rates` always fetches the latest payload from Open Exchange Rates and refreshes both Redis and the in-memory fallback cache.

Cutoff table:

| Time GMT+8 | Currency codes |
| --- | --- |
| 11:00 | JPY, NZD |
| 12:00 | AUD, PHP |
| 15:00 | BND, CNY, HKD, IDR, SGD, THB |
| 16:00 | AED, BDT, CAD, CHF, DKK, EUR, GBP, INR, LKR, NOK, PKR, SAR, SEK, USD, ZAR |

Any currency not listed uses the default 16:00 GMT+8 cutoff.

## Conversion Formula

Open Exchange Rates free tier returns rates relative to USD.

```text
rate = toRate / fromRate
convertedAmount = amount * rate
```

## Tests

Backend:

```bash
cd backend
mvn test
```

Verified result:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Frontend:

```bash
cd frontend
npm run build
```

Verified result:

```text
Compiled successfully.
```

See `TEST_RESULTS.md` for the exact local commands and notes.

## Project Structure

```text
backend/
  src/main/java/com/currency/
    config/
    controller/
    dto/
    exception/
    model/
    service/
    util/
frontend/
  src/components/
  src/services/
  src/types/
docker-compose.yml
plan.md
TEST_RESULTS.md
```
