# Build stage
FROM mcr.microsoft.com/openjdk/jdk:17-mariner AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Run stage
FROM mcr.microsoft.com/openjdk/jdk:17-mariner
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]