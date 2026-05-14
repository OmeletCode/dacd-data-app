# Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY weather-extractor/pom.xml weather-extractor/
COPY spacex-extractor/pom.xml spacex-extractor/
COPY event-store-builder/pom.xml event-store-builder/
COPY rain-fade-monitor/pom.xml rain-fade-monitor/
RUN mvn dependency:go-offline -B

COPY . .
RUN mvn clean package -DskipTests -pl rain-fade-monitor -am

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/rain-fade-monitor/target/rain-fade-monitor-1.0-SNAPSHOT.jar app.jar
# Necesitamos copiar el eventstore y el datamart si se van a usar archivos locales, 
# pero idealmente se pasan por volúmenes o argumentos.
ENTRYPOINT ["java", "-jar", "app.jar", "/app/eventstore", "/app/datamart.db"]
