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
- Render Blueprint deployment for frontend, backend, and Redis-compatible Key Value service
- Backend unit tests

## Requirements

- Docker and Docker Compose
- Free Open Exchange Rates API key from [openexchangerates.org](https://openexchangerates.org/signup/free)

## Live Deployment

- Frontend: https://currency-frontend-3mdz.onrender.com
- Backend: https://currency-api-9wye.onrender.com
- Backend health check: https://currency-api-9wye.onrender.com/api/health
- Example live conversion: https://currency-api-9wye.onrender.com/api/convert?from=USD&to=EUR&amount=100
- Render Redis service ID: `red-d7otnspf9bms73fg3qng`

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

Docker Compose runs the frontend with `REACT_APP_API_URL=http://localhost:8081/api`.
Render runs the frontend with `REACT_APP_API_URL=https://currency-api-9wye.onrender.com/api`.

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
mvn clean verify
```

Verified result:

```text
Tests run: 59, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Line coverage: 87.89%
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

Docker image verification:

```text
docker compose config --quiet
docker compose build backend frontend
Service backend  Built
Service frontend  Built
```

## AI Assistance

OpenAI was used as a development assistant throughout the project. The major prompts and workflows were:

- Planning: asked OpenAI to create `plan.md` for the Currency Conversion assignment, including backend, frontend, Redis caching, Docker, tests, and deployment tasks.
- System analysis and design: asked OpenAI to review the assignment requirements and map them to a Spring Boot MVC backend, React TypeScript frontend, Redis cache strategy, Open Exchange Rates integration, and Render deployment setup.
- Code implementation: prompted OpenAI to help implement the custom `/api/convert` calculation, `/api/rates` refresh behavior, Redis cache lookup before external API calls, cutoff-time TTL logic, readable API errors, and frontend dynamic conversion with `useEffect`.
- UI implementation: prompted OpenAI to refine the frontend to match the provided currency converter design, including light blue/white styling, searchable currency dropdowns, circular flags from `country-flag-icons`, swap control, indicative exchange rate text, and footer credit.
- Testing: prompted OpenAI to add JUnit tests for controller, service, Redis cache, cutoff TTL, readable error handling, and Render Redis URL configuration, with a JaCoCo coverage gate.
- Docker and deployment: prompted OpenAI to configure local Docker Compose and Render Blueprint deployment for the backend, frontend, and Redis-compatible Render Key Value service, including runtime frontend API configuration.

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
