# syntax=docker/dockerfile:1

# One Dockerfile for every backend module. Pick the module with the MODULE build arg:
#   docker build --build-arg MODULE=ms-reservas ReservaFit-Backend
# compose.yaml builds one image per module this way.
ARG MODULE

# ---------- Build stage: package the Spring Boot jar of one module ----------
FROM maven:3.9-eclipse-temurin-25 AS build
ARG MODULE
WORKDIR /build

# Resolve dependencies first so this layer stays cached until a pom.xml changes
COPY pom.xml ./
COPY reservafit-events/pom.xml reservafit-events/
COPY api-gateway/pom.xml api-gateway/
COPY ms-usuarios/pom.xml ms-usuarios/
COPY ms-gimnasios/pom.xml ms-gimnasios/
COPY ms-reservas/pom.xml ms-reservas/
RUN test -n "$MODULE" || { echo "Build arg MODULE is required, e.g. ms-reservas" >&2; exit 1; }
# reservafit-events is built from source below, so it is not downloaded here
RUN mvn -B -ntp -pl "$MODULE" -am dependency:go-offline -DexcludeGroupIds=cl.nxiiio

COPY . .
# Tests run with `./mvnw test`, not on every image build
RUN mvn -B -ntp -pl "$MODULE" -am -DskipTests package \
    && cp "$MODULE"/target/"$MODULE"-*.jar /build/app.jar

# ---------- Runtime stage: JRE only ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# curl is only used by the healthchecks in compose.yaml (the JRE image does not ship with it)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app \
    && mkdir /app/logs && chown app:app /app/logs

COPY --from=build /build/app.jar app.jar

USER app

# Override at runtime to tune the JVM, e.g. `-e JAVA_OPTS=-Xmx512m`
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# exec so the JVM is PID 1 and receives SIGTERM on `docker stop`
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
