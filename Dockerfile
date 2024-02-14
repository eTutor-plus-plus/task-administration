# Optimize JAR
FROM eclipse-temurin:21-jre AS builder
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Build final image
FROM eclipse-temurin:21-jre
EXPOSE 8080

# Copy layered JAR
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Angular App
COPY target/classes/static/app/* /public/

# Spring
ENV SPRING_PROFILES_ACTIVE=prod
#ENV SERVER_PORT=8080

## Database
#ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/etutor
#ENV SPRING_DATASOURCE_USERNAME=etutor
#ENV SPRING_DATASOURCE_PASSWORD=TBD
#ENV SPRING_FLYWAY_USER=etutor_admin
#ENV SPRING_FLYWAY_PASSWORD=TBD

## Mail
#ENV SPRING_MAIL_SENDER=etutor@dke.uni-linz.ac.at
#ENV SPRING_MAIL_HOST=smtp.uni-linz.ac.at
#ENV SPRING_MAIL_PORT=587
#ENV SPRING_MAIL_USERNAME=etutor
#ENV SPRING_MAIL_PASSWORD=TBD

ENTRYPOINT ["java", "-Xmx6g", "org.springframework.boot.loader.launch.JarLauncher"]
