FROM node:20-alpine AS build
WORKDIR /app
COPY . .
RUN npm install --legacy-peer-deps
COPY . .

# Используем ARG для передачи переменных при сборке
ARG REACT_APP_API_BASE_URL
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

RUN npm run build

FROM node:20-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /app/build ./build
EXPOSE 3000
CMD ["serve", "-s", "build", "-l", "3000"]