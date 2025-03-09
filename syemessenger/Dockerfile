FROM openjdk:21

WORKDIR /opt/service

COPY target/lib /opt/service/lib/
COPY target/syemessenger-1.0-SNAPSHOT.jar /opt/service/syemessenger.jar

ENTRYPOINT ["java", "-cp", "/opt/service/syemessenger.jar:/opt/service/lib/*", "io.syemessenger.ServiceRunner"]