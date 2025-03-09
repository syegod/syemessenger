FROM openjdk:21

ENV JAVA_OPTS=""

WORKDIR /opt/service

COPY target/lib /opt/service/lib/
COPY target/syemessenger-1.0-SNAPSHOT.jar /opt/service/syemessenger.jar

ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -cp /opt/service/syemessenger.jar:/opt/service/lib/* io.syemessenger.ServiceRunner"]
