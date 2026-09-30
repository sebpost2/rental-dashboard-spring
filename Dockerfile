FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Tests need Docker (Testcontainers), so they run in CI, not inside this build.
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 app
WORKDIR /app
COPY --from=build /src/target/rental-dashboard-spring-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "app.jar"]
