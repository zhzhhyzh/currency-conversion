# Test Results

Test date: April 29, 2026

## Backend Build And Unit Tests

Command:

```powershell
& 'C:\Users\henry\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd' clean verify
```

Result:

```text
Tests run: 59, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

JaCoCo coverage:

```text
Line coverage: 87.89% (399 covered, 55 missed)
Coverage gate: 80% minimum line coverage
Report: backend/target/site/jacoco/index.html
```

Java 8 bytecode check:

```text
major version: 52
```

Backend smoke check:

```text
GET http://localhost:8081/api/health
Currency Conversion API is running
```

Live Render smoke checks:

```text
GET https://currency-api-9wye.onrender.com/api/health
Currency Conversion API is running

GET https://currency-api-9wye.onrender.com/api/convert?from=USD&to=EUR&amount=100
200 OK with from=USD, to=EUR, amount=100.0, and a calculated convertedAmount
```

Covered cases:

- Base, reverse, and cross-currency conversion formulas.
- Lowercase and padded currency code normalization.
- Zero amount and same-currency conversion.
- Negative, non-finite, invalid source, and invalid target amount/currency errors.
- Missing source/target exchange rates and invalid zero exchange-rate values.
- `/api/convert`, `/api/rates`, `/api/health`, and `/api/cache/*` controller responses.
- Readable JSON errors for missing query parameters, invalid amount values, domain exceptions, and unexpected exceptions.
- Redis cache get/set/delete/TTL/health behavior, including failure fallbacks.
- Render Redis `REDIS_URL` connection string parsing and fallback behavior.
- In-memory cache expiration and clearing.
- GMT+8 cutoff time and TTL calculation behavior.
- Scheduled exchange-rate refresh is disabled by default, so the first external API fetch is demand-driven by `/api/convert` or `/api/rates`.

## Frontend Build

Command:

```powershell
npm install
npm run build
```

Result:

```text
Compiled successfully.
```

Production bundle output:

```text
114.93 kB  build\static\js\main.b90dd07b.js
1.38 kB    build\static\css\main.ae17dad8.css
```

## Docker And Compose Checks

Docker Compose syntax:

```powershell
docker compose config --quiet
```

Result:

```text
Passed
```

Docker image build command attempted:

```powershell
docker compose build backend frontend
```

Result:

```text
Service backend  Built
Service frontend  Built
```

The backend and frontend Docker images build successfully from the same Dockerfiles used by Docker Compose and Render.

## Notes

- Maven is not on the global PATH in this workstation, so tests were run using the Maven distribution already present under `C:\Users\henry\.m2\wrapper\dists`.
- The backend targets Java 1.8 and the compiled application entry point verifies as class major version 52.
- Frontend dependencies install with `country-flag-icons@1.6.16`, the current published npm version available during verification.
- Local server smoke check passed on `http://localhost:8081/api/health` because port 8080 is already occupied by an Oracle listener on this workstation.
- Live frontend: `https://currency-frontend-3mdz.onrender.com`
- Live backend: `https://currency-api-9wye.onrender.com`
- Render Redis service ID: `red-d7otnspf9bms73fg3qng`
