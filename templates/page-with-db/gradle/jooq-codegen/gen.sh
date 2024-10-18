
(
  docker-entrypoint.sh postgres &

  until psql -h "localhost" -U $POSTGRES_USER -d $POSTGRES_DB -c '\q'; do
    echo "PostgreSQL is still starting up..."
    sleep 0.33
  done

  echo "PostgreSQL is up and running!"

  ./liquibase/liquibase --changeLogFile=changelog/db.changelog-master.yaml \
              --url=jdbc:postgresql://localhost:5432/$POSTGRES_DB \
              --username=$POSTGRES_USER \
              --password=$POSTGRES_PASSWORD \
              update

  gradle jooqCodegen

) 1>&2

tar cz jooq -C /work/build/generated-sources
