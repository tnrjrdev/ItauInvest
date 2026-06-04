# ============================ Stage 1: Build ============================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache de dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Compila e empacota (testes executados no pipeline CI, pulados aqui)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ============================ Stage 2: Runtime ==========================
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Usuario nao-root por seguranca
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
