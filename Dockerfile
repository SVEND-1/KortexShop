# Dockerfile (собирает внутри, не нужен Maven)
FROM maven:3.9.6-amazoncorretto-17 AS builder

WORKDIR /app

# Копируем POM для кэширования
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники
COPY src ./src

# Собираем
RUN mvn clean package -DskipTests

# Финальный образ
FROM amazoncorretto:17-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Создаем папки uploads
RUN mkdir -p /app/uploads/images
RUN chmod -R 755 /app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]