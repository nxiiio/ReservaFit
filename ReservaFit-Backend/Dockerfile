# syntax=docker/dockerfile:1

# ---------- Build stage: package the Spring Boot jar ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Resolve dependencies first so this layer stays cached until pom.xml changes
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
# Tests run with `./mvnw test`, not on every image build
RUN mvn -B -ntp -DskipTests package

# ---------- Runtime stage: JRE only ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# curl is only used by the HEALTHCHECK (the JRE image does not ship with it)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build /build/target/*.jar app.jar

USER app
EXPOSE 8080

# Override at runtime to tune the JVM, e.g. `-e JAVA_OPTS=-Xmx512m`
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# GET /api/gimnasios is public and hits the database, so a 200 means the API is serving
HEALTHCHECK --interval=10s --timeout=3s --start-period=45s --retries=6 \
    CMD curl -fsS http://127.0.0.1:8080/api/gimnasios > /dev/null || exit 1

# exec so the JVM is PID 1 and receives SIGTERM on `docker stop`
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
