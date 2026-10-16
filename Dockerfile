FROM amazoncorretto:21-alpine3.24-jdk

WORKDIR /usr/src/app

RUN adduser -D appuser

COPY --chown=appuser ./build/libs/pizzaShop-0.0.1-SNAPSHOT.jar .

USER appuser


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "pizzaShop-0.0.1-SNAPSHOT.jar"]