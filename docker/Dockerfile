FROM openjdk:17

WORKDIR /app

# We expect a single executable JAR file to be downloaded in the GitHub Actions pipeline
COPY spring-petclinic-3.5.0-SNAPSHOT.jar /app/petclinic.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/petclinic.jar"]
