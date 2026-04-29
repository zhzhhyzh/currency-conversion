# Test Results

Test date: April 29, 2026

## Backend Build And Unit Tests

Command:

```powershell
& 'C:\Users\henry\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd' clean package
```

Result:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
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

Covered cases:

- USD to EUR conversion returns a positive converted amount.
- Zero amount returns zero.
- Same-currency conversion returns a 1:1 rate.
- Negative amount throws `CurrencyException`.
- Invalid currency code throws `CurrencyException`.
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

## Notes

- Maven is not on the global PATH in this workstation, so tests were run using the Maven distribution already present under `C:\Users\henry\.m2\wrapper\dists`.
- The backend targets Java 1.8 and the compiled application entry point verifies as class major version 52.
- Frontend dependencies install with `country-flag-icons@1.6.16`, the current published npm version available during verification.
- Local server smoke check passed on `http://localhost:8081/api/health` because port 8080 is already occupied by an Oracle listener on this workstation.
