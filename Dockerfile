# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/javademo-1.0-SNAPSHOT.jar app.jar

# Render-এর ডাইনামিক পোর্ট সাপোর্টের জন্য গ্লোবাল এনভায়রনমেন্ট কনফিগারেশন
ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
