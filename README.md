# DEGO: Adjustable Objects for Scalable Concurrent Programming

Welcome to the **DEGO** project — a Java library that implements *Adjustable Objects*, a novel abstraction designed to facilitate efficient and scalable concurrent programming.

This project illustrates the concept of **adjustment** introduced in the paper:

> **Adjusted Objects: An Efficient and Principled Approach to Scalable Programming**

## 📘 Overview

Adjustable objects illustrate the principled approach of *adjustment*, which leverages how the interface of a shared object is used to provide finer control over synchronization, consistency, and performance. This repository includes:

- A Java library implementing core adjustable objects.
- A benchmark suite and Docker environment to run performance experiments.

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/BoubacarKaneTSP/DEGO.git
cd DEGO
```

### 2. Build the Docker image

All experiments are containerized for reproducibility. To build the Docker image, run:

```bash
docker build -t retwis-runner .
```

### 3. Run the experiments

```bash
docker run --rm \
  --cap-add=SYS_NICE \
  --cap-add=SYS_ADMIN \
  -v ./results:/app/results \
  retwis-runner
```

This command:
- Runs the benchmark suite inside a controlled container.
- Mounts a local `results/` folder to collect output data from the experiments.

---

## 📊 Output

Experiment results are saved in the `results/` directory. These can be used to reproduce the figures and insights discussed in the original paper.

---

## 🛠 Requirements

No manual installation needed! The Docker image includes:
- Java 22
- Python 3.11
- All required libraries and scripts

Ensure Docker is installed on your system before proceeding.

---

## 📖 Citation

If you use this repository or build upon this work, please cite the original paper:

> *Adjusted Objects: An Efficient and Principled Approach to Scalable Programming*  
> [Boubacar Kane, Pierre Sutra], [Middleware Conference], [2025].

