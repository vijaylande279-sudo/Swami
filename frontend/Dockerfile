# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Runtime stage ---
FROM node:20-alpine
WORKDIR /app

COPY --from=build /app/dist/Hotel-Order-Management ./dist/Hotel-Order-Management
COPY --from=build /app/node_modules ./node_modules

EXPOSE 4000
CMD ["node", "dist/Hotel-Order-Management/server/server.mjs"]
