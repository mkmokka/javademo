# Step 1: Java 22 এনভায়রনমেন্টে Maven বিল্ড করা
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: রান করার জন্য অফিশিয়াল স্ট্যাবল Eclipse Temurin Java 22 ব্যবহার
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render.io সাধারণত PORT এনভায়রনমেন্ট ভেরিয়েবল ব্যবহার করে
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
