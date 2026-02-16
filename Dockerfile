# =========================
<<<<<<< HEAD
# STEP 1 — Build application
=======
# STEP 1 : Build avec Maven + Java 21
>>>>>>> 773af81 (first commit)
# =========================
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

<<<<<<< HEAD
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# =========================
# STEP 2 — Create custom JRE with jlink
# =========================
FROM eclipse-temurin:21-jdk-alpine AS jre-builder

RUN apk add --no-cache binutils

# Create minimal JRE (auto-detect modules)
RUN $JAVA_HOME/bin/jlink \
    --add-modules ALL-MODULE-PATH \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /custom-jre

# =========================
# STEP 3 — Minimal runtime (distroless)
# =========================
FROM gcr.io/distroless/base-debian12

WORKDIR /app

# Copy custom minimal JRE
COPY --from=jre-builder /custom-jre /opt/java

# Copy application
COPY --from=builder /app/target/batch-processing-app.jar app.jar

ENV PATH="/opt/java/bin:${PATH}"

EXPOSE 8777

ENTRYPOINT ["java","-jar","app.jar"]
=======
# Copier le pom.xml pour le cache
COPY pom.xml .

# Copier le code source
COPY src ./src

# Build du projet
RUN mvn clean package -DskipTests=false

# =========================
# STEP 2 : Runtime JRE Alpine
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Installer curl pour les healthchecks
RUN apk add --no-cache curl

# Copier le jar
COPY --from=builder /app/target/batch-processing-app.jar app.jar

# Exposer le port réel du service
EXPOSE 8777

# Lancer l'application
ENTRYPOINT ["java","-jar","app.jar"]
>>>>>>> 773af81 (first commit)
