FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

# Installazione delle librerie grafiche AWT/Swing X11 e font necessari
RUN apt-get update && apt-get install -y --no-install-recommends \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/target/htn-charlie-game-1.0.0.jar ./app.jar

# Configurazione display X11 di default
ENV DISPLAY=:0

# Avvio applicazione Swing
ENTRYPOINT ["java", "-jar", "app.jar"]
