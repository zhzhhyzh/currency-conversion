#!/bin/bash

# Quick Start Script for Currency Conversion Application
# This script sets up and runs the entire application

set -e  # Exit on error

echo "================================"
echo "Currency Conversion - Quick Start"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}[1/5] Checking prerequisites...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java 22+ is required but not installed.${NC}"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo -e "${RED}Node.js (npm) is required but not installed.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Java and Node.js are installed${NC}"
echo ""

# Get API Key
echo -e "${YELLOW}[2/5] Configuring API Key...${NC}"
if [ -z "$OPENEXCHANGERATES_API_KEY" ]; then
    echo "Please enter your OpenExchangeRates API key"
    echo "(Get free key from https://openexchangerates.org/signup/free)"
    read -p "API Key: " api_key
    export OPENEXCHANGERATES_API_KEY=$api_key
else
    echo -e "${GREEN}✓ API Key found in environment${NC}"
fi
echo ""

# Setup Backend
echo -e "${YELLOW}[3/5] Setting up Backend...${NC}"
cd backend

if [ -f "target/currency-api-1.0.0.jar" ]; then
    echo -e "${GREEN}✓ JAR file already exists${NC}"
else
    echo "Building backend..."
    mvn clean package -DskipTests > /dev/null 2>&1
    echo -e "${GREEN}✓ Backend built successfully${NC}"
fi

cd ..
echo ""

# Setup Frontend
echo -e "${YELLOW}[4/5] Setting up Frontend...${NC}"
cd frontend

if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install > /dev/null 2>&1
    echo -e "${GREEN}✓ Dependencies installed${NC}"
else
    echo -e "${GREEN}✓ Dependencies already installed${NC}"
fi

# Create .env if it doesn't exist
if [ ! -f ".env" ]; then
    cp .env.example .env
    echo -e "${GREEN}✓ Created .env file${NC}"
fi

cd ..
echo ""

# Start services
echo -e "${YELLOW}[5/5] Starting services...${NC}"
echo ""
echo "Starting Backend (Port 8080)..."
cd backend
java -jar target/currency-api-1.0.0.jar &
BACKEND_PID=$!
cd ..

# Wait for backend to start
sleep 3

echo "Starting Frontend (Port 3000)..."
cd frontend
npm start &
FRONTEND_PID=$!
cd ..

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}✓ Application Started!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Backend API: http://localhost:8080/api"
echo "Frontend UI: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop all services"
echo ""

# Wait for interrupt
wait $BACKEND_PID $FRONTEND_PID
