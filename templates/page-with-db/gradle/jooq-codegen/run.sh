docker build . -t jooq-code-gen
mkdir -p $2
docker run -v $1:/work/changelog -e PACKAGE_NAME=$3 jooq-code-gen | tar xz -C $2