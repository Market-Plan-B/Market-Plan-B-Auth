# 빌드 스테이지
FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# 소스 복사
COPY build.gradle settings.gradle ./
COPY src ./src
COPY gradle ./gradle

# JAR 빌드
RUN gradle clean build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]