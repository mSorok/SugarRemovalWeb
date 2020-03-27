FROM openjdk:8u171-slim
EXPOSE 8092
VOLUME /tmp

COPY target/sugarremovalweb-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

