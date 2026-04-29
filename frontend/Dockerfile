FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY public ./public
COPY src ./src
COPY tsconfig.json ./

# Build the application
RUN npm run build

# Production stage
FROM node:22-alpine

WORKDIR /app

# Install a simple HTTP server
RUN npm install -g serve

# Copy built app from builder
COPY --from=builder /app/build ./build

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD node -e "require('http').get({host: 'localhost', port: process.env.PORT || 3000, path: '/'}, (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)}).on('error', () => process.exit(1))"

CMD ["sh", "-c", "node -e \"const fs=require('fs'); fs.writeFileSync('build/env.js', 'window.__ENV__ = '+JSON.stringify({REACT_APP_API_URL: process.env.REACT_APP_API_URL || 'http://localhost:8081/api'})+';\\n')\" && serve -s build -l ${PORT:-3000}"]
