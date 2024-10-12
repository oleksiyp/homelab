FROM ubuntu:22.04

RUN apt-get update -q &&\
    apt-get install -q -y jq curl openjdk-17-jre-headless &&\
    KUBECTL_VERSION=$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt) &&\
    curl -q -LO https://storage.googleapis.com/kubernetes-release/release/$KUBECTL_VERSION/bin/linux/amd64/kubectl -o kubectl &&\
    chmod +x kubectl &&\
    mv kubectl /usr/bin/kubectl

