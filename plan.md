# Currency Conversion Application Plan

## Goal
Build a full-stack Currency Conversion Web Application that uses Open Exchange Rates `latest.json`, performs local conversion calculations, caches latest rates in Redis until currency cutoff time, and provides a React TypeScript interface using lightweight `country-flag-icons`.

## Architecture

### Backend
- Spring Boot REST API with MVC separation:
  - `controller`: HTTP endpoints and request validation handoff.
  - `service`: Open Exchange Rates fetch, Redis cache, conversion logic.
  - `dto`: API response contracts.
  - `model`: exchange-rate cache payload.
  - `util`: cutoff-time and fallback cache helpers.
  - `config`: CORS, Redis, REST client, scheduling.
- External API:
  - `GET https://openexchangerates.org/api/latest.json?app_id={OPENEXCHANGERATES_API_KEY}`
- Required endpoints:
  - `GET /api/convert?from=USD&to=EUR&amount=100`
  - `GET /api/rates`
  - `GET /api/health`

### Frontend
- React 18 + TypeScript.
- API client layer calls the Spring Boot backend.
- Converter UI includes:
  - source currency selector
  - target currency selector
  - amount input
  - swap control
  - calculated result
  - loading and error states
- Use `country-flag-icons` from npm for SVG flag rendering to reduce image weight.

### Infrastructure
- Redis cache service for latest exchange rates.
- Docker Compose starts backend, frontend, and Redis.
- `.env.example` documents required API key and service URLs.

## Conversion Logic
- Open Exchange Rates free tier returns USD-based rates.
- Use local calculation instead of restricted `/convert` endpoint.
- Formula:
  - `rate = toRate / fromRate`
  - `convertedAmount = amount * rate`
- Validate:
  - `from` and `to` are three-letter currency codes.
  - `amount` is finite and not negative.
  - both currencies exist in latest rates.

## Redis Cache Strategy
- Store the full `latest.json` rate payload under one Redis key because Open Exchange Rates returns all rates at once.
- Cache TTL is based on the earliest applicable next cutoff among available currencies so no currency-specific quote remains beyond its cutoff.
- Cutoff timezone is GMT+8.
- Cutoff table:
  - 11:00: JPY, NZD
  - 12:00: AUD, PHP
  - 15:00: BND, CNY, HKD, IDR, SGD, THB
  - 16:00: AED, BDT, CAD, CHF, DKK, EUR, GBP, INR, LKR, NOK, PKR, SAR, SEK, USD, ZAR
- Default for currencies not listed: 16:00 GMT+8.
- If Redis is unavailable, fall back to a short in-memory cache so the API remains usable during local development.

## Implementation Steps
1. Refresh this plan before coding.
2. Audit current backend, frontend, Docker, and README.
3. Fix backend correctness:
   - reliable currency rate lookup
   - finite amount validation
   - Redis serialization and TTL behavior
   - test compatibility
4. Fix frontend integration:
   - ensure `country-flag-icons` usage works with Create React App
   - support assignment currency list
   - graceful API error display
5. Update README and test result documentation.
6. Run backend tests and frontend build.
7. Report exact commands run and any remaining setup requirements, especially the Open Exchange Rates API key.

## Deliverables
- Source code.
- `README.md` with setup, run, test, cURL examples, and configuration notes.
- Unit test result summary.
- Docker Compose configuration including Redis.
