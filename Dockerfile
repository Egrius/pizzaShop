# Stage 1
FROM gradle:9.2.1-jdk21-ubi-minimal AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2

FROM amazoncorretto:21-alpine3.24-jdk

WORKDIR /usr/src/app

RUN adduser -D appuser

COPY --from=build --chown=appuser /app/build/libs/pizzaShop-0.0.1-SNAPSHOT.jar .

USER appuser


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "pizzaShop-0.0.1-SNAPSHOT.jar"]