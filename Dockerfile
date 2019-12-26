FROM openjdk:10-jre

LABEL maintainer "Rohan Nagar <rohannagar11@gmail.com>"

COPY ./application/target/application-*.jar thunder.jar

EXPOSE 8080 8081 8443 8444
ENTRYPOINT ["java", "-jar", "/thunder.jar", "server", "/home/config/config.yaml"]
