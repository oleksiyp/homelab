
FROM postgres:14-alpine as postgres

RUN apk add --no-cache openjdk17 bash curl gradle

RUN mkdir /work && chown postgres:postgres /work

WORKDIR /work
USER postgres

ENV POSTGRES_DB=mydb
ENV POSTGRES_USER=myuser
ENV POSTGRES_PASSWORD=mypassword

EXPOSE 5432


RUN curl -Loliquibase.tar.gz https://github.com/liquibase/liquibase/releases/download/v4.29.2/liquibase-4.29.2.tar.gz && \
    mkdir liquibase &&  \
    tar -xzvf liquibase.tar.gz -C liquibase && \
    rm liquibase.tar.gz

COPY ../src/main/resources/db/changelog/changelog.yaml changelog.yaml
COPY build.gradle build.gradle

RUN (docker-entrypoint.sh postgres &) && sleep 5 && \
    ./liquibase/liquibase --changeLogFile=changelog.yaml \
                --url=jdbc:postgresql://localhost:5432/mydb \
                --username=myuser \
                --password=mypassword \
                update && \
    gradle jooqCodegen
