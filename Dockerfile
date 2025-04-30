# Image de base : Ubuntu 22.04
FROM ubuntu:22.04

# Éviter les prompts pendant l'installation
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Paris

# Mettre à jour les paquets, ajouter le dépôt Python, puis installer les dépendances
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y software-properties-common wget curl unzip git gnupg ca-certificates lsb-release \
    && add-apt-repository ppa:deadsnakes/ppa -y \
    && apt-get update \
    && apt-get install -y \
       python3.11 \
       python3.11-dev \
       python3.11-venv \
       python3-pip \
       openjdk-17-jdk \
       maven \
       numactl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Symlinks pour que "python" et "python3" pointent vers Python 3.11
RUN ln -sf /usr/bin/python3.11 /usr/bin/python \
    && ln -sf /usr/bin/python3.11 /usr/bin/python3

# Installer gdown pour télécharger depuis Google Drive
RUN pip install --no-cache-dir gdown

# Définir le répertoire de travail
WORKDIR /app

# Copier les scripts dans le conteneur
COPY experiences/download.sh run_experiences.sh test.sh run_mining.sh *.py ./

COPY java/ /app/java/

# Rendre les scripts exécutables
RUN chmod +x download.sh run_experiences.sh

# Point d’entrée : d’abord download.sh, puis run_experiences.sh
ENTRYPOINT ["./download.sh"]
CMD ["./run_experiences.sh"]
