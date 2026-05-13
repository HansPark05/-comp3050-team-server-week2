FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY map.txt ./
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./target/classes
COPY --from=build /app/map.txt ./
EXPOSE 8000
CMD ["java", "-cp", "target/classes", "Test"]
