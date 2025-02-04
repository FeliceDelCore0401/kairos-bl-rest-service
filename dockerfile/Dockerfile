ARG BASE_VERSION=17.0.4_8-jre-focal
FROM fra.ocir.io/sisalspa/base-images/eclipse-temurin:$BASE_VERSION
COPY target/bl-rest-service*.jar /app.jar
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]