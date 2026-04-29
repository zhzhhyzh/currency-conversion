@echo off
REM Quick Start Script for Currency Conversion Application (Windows)

setlocal enabledelayedexpansion

echo ================================
echo Currency Conversion - Quick Start
echo ================================
echo.

REM Check prerequisites
echo [1/4] Checking prerequisites...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java 22+ is required but not installed.
    exit /b 1
)

where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Node.js (npm) is required but not installed.
    exit /b 1
)

echo OK: Java and Node.js are installed
echo.

REM Get API Key
echo [2/4] Configuring API Key...
if not defined OPENEXCHANGERATES_API_KEY (
    echo Please enter your OpenExchangeRates API key
    echo (Get free key from https://openexchangerates.org/signup/free)
    set /p api_key="API Key: "
    set OPENEXCHANGERATES_API_KEY=!api_key!
) else (
    echo OK: API Key found in environment
)
echo.

REM Setup Backend
echo [3/4] Setting up Backend...
cd backend

if exist "target\currency-api-1.0.0.jar" (
    echo OK: JAR file already exists
) else (
    echo Building backend...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo ERROR: Backend build failed
        exit /b 1
    )
    echo OK: Backend built successfully
)

cd ..
echo.

REM Setup Frontend
echo [4/4] Setting up Frontend...
cd frontend

if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
    if %errorlevel% neq 0 (
        echo ERROR: npm install failed
        exit /b 1
    )
    echo OK: Dependencies installed
) else (
    echo OK: Dependencies already installed
)

if not exist ".env" (
    copy .env.example .env > nul
    echo OK: Created .env file
)

cd ..
echo.

echo ================================
echo Next Steps:
echo ================================
echo.
echo 1. Start Backend:
echo    cd backend
echo    mvn spring-boot:run
echo.
echo 2. Start Frontend (in new terminal):
echo    cd frontend
echo    npm start
echo.
echo Backend API: http://localhost:8080/api
echo Frontend UI: http://localhost:3000
echo.
