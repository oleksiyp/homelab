FROM alpine/java:22-jdk as builder

WORKDIR /tmp
COPY application.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract
RUN ls /tmp

FROM alpine/java:22-jre

WORKDIR /app

COPY --from=builder /tmp/dependencies/ /app/
COPY --from=builder /tmp/spring-boot-loader/ /app/
COPY --from=builder /tmp/snapshot-dependencies/ /app/
COPY --from=builder /tmp/application/ /app/

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]