FROM openjdk:8-jre-alpine

COPY ./application/target/application-*.jar thunder.jar

EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "/thunder.jar", "server", "/home/config/config.yaml"]

