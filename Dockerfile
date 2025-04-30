# Image de base
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Paris

# Installer dépendances + Python + Maven + numactl
RUN apt-get update \
 && apt-get install -y \
    software-properties-common \
    wget curl unzip git gnupg ca-certificates lsb-release \
    python3.11 python3.11-dev python3.11-venv python3-pip \
    maven \
    numactl \
 && ln -sf /usr/bin/python3.11 /usr/bin/python \
 && ln -sf /usr/bin/python3.11 /usr/bin/python3 \
 && pip install --no-cache-dir gdown

# Installer manuellement OpenJDK 22 depuis Adoptium
RUN mkdir -p /opt/java \
 && wget -q https://github.com/adoptium/temurin22-binaries/releases/download/jdk-22%2B36/OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz \
 && tar -xzf OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz -C /opt/java --strip-components=1 \
 && rm OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz

# Définir JAVA_HOME et mettre java 22 en PATH
ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

# Répertoire de travail
WORKDIR /app

# Copier le code Java et les scripts
COPY java/ /app/java/
COPY experiences/download.sh run_experiences.sh test.sh run_mining.sh *.py ./

RUN chmod +x download.sh run_experiences.sh test.sh run_mining.sh
ENTRYPOINT ["./download.sh"]
CMD ["./run_experiences.sh"]
