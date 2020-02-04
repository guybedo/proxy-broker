FROM openjdk:8

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

RUN adduser appuser
WORKDIR /home/appuser

USER appuser

ADD target/*-exec.jar app.jar

EXPOSE 8080

VOLUME /var/log
VOLUME /tmp
VOLUME /var/run/docker.sock

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar","/home/appuser/app.jar"]
