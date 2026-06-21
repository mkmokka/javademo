# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Runtime Stage (openjdk:17-jdk-slim এর পরিবর্তে eclipse-temurin ব্যবহার করা হয়েছে)
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/javademo-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

