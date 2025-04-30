# Image de base : Ubuntu 22.04
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Paris

RUN apt-get update && apt-get install -y \
    software-properties-common \
    wget curl unzip git gnupg ca-certificates lsb-release \
    python3.11 python3.11-dev python3.11-venv \
    openjdk-22-jdk \
    python3-pip \
    perf \
    && apt-get clean

RUN ln -sf /usr/bin/python3.11 /usr/bin/python && \
    ln -sf /usr/bin/python3.11 /usr/bin/python3

RUN pip install --no-cache-dir gdown

WORKDIR /app

COPY run_experiences.sh .
COPY download.sh .

RUN chmod +x run_experiences.sh download.sh

ENTRYPOINT ["./download.sh"]
CMD ["./run_experiences.sh"]
