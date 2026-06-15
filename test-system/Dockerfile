# Етап 1: Збірка проєкту (Build stage)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Спочатку копіюємо тільки pom.xml, щоб завантажити залежності (кешування)
COPY pom.xml .
RUN mvn dependency:go-offline
# Потім копіюємо весь код і збираємо .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Етап 2: Запуск проєкту (Run stage)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Копіюємо зібраний .jar з етапу збірки
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]