# Toyota & 32Bit POS (Point of Sale) Back-end Project

[![Docker](https://img.shields.io/badge/Docker-Supported-blue.svg)](https://www.docker.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Powered-green.svg)](https://spring.io/projects/spring-boot)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-orange.svg)](https://microservices.io/)

A comprehensive, microservices-based Point of Sale system developed for Toyota, featuring robust sales operations, user management, and reporting capabilities.

## Table of Contents
1. [Project Overview](#overview)
2. [Microservices Architecture](#microservices-architecture)
   1. [Api Gateway](#api-gateway)
   2. [Eureka Server](#eureka-server)
   3. [Security](#security)
   4. [Product Service](#product-service)
   5. [User Service](#user-service)
   6. [Sale Service](#sale-service)
   7. [Reporting Service](#reporting-service)
3. [Project Architecture](#project-architecture)
4. [API & DTO Reference](#api--dto-reference)
5. [Installation Instructions](#installation-instructions)
   1. [Quick Start](#quick-start)
   2. [Detailed Setup](#detailed-setup)
6. [Configuration](#configuration)
7. [AWS and Jenkins](#aws-and-jenkins)
   1. [Required Installations](#required-installations)
   2. [Jenkins and Portainer Installation](#jenkins-and-portainer-installation)
      1. [Run Jenkins](#jenkins)
      2. [Run Portainer](#portainer)
   3. [Up The Project on EC2](#up-the-project)
   4. [Jenkins Settings](#jenkins-settings)
8. [Contact Information](#contact-information)


## Overview
_POS Project (Point of Sale) is a web-based system for efficient and secure sales operations. Features include user management, product management, sales processing, and reporting. It generates Excel reports and PDF invoices. Easily deployable with Docker._

<br>

## Microservices Architecture
### Api Gateway
* Receives all incoming requests and forwards them to the Security Service for authentication and authorization.
* Directs requests to appropriate services based on authorization results.
* Communicates with Eureka Server for accessing and routing to various microservices.

### Eureka Server
* Maintains information about service instances.
* Registers each service instance as they start up.

### Security
* Authenticates users and generates JWT tokens for secure access.
* Validates authorization headers to ensure secure access to various endpoints.
* Retrieves user-specific information based on authentication details and communicates this information to other services when needed.

### Product Service
* Manages product-related tasks, including adding, deleting, and updating products.
* Provides detailed filtering, pagination, and prefix-based search for product retrieval.

### User Service
* Handles essential user management tasks, including adding, deleting, and retrieving users.

### Sale Service
* Manages adding products to and removing products from a shopping bag.
* Handles the finalization of sales transactions and allows for sale cancellations.
* Applies and removes promotional campaigns from shopping bags.

### Reporting Service
* Generates and tracks the status of **PDF receipts** for sales transactions.
* Retrieves sales data based on specific criteria and generates **Excel reports** sent via **email.**
* Schedules, lists, and cancels **periodic reporting jobs** to automate report generation and distribution.

<br>

## Project Architecture

<div align="center">
  <img alt="NoBack.png" src="Readme-Files%2FArchitecture.png" width="800"/>
</div>


## API & DTO Reference

Detailed API and DTO documentation is available in multiple formats:

- **Online Documentation**: [POS Project API & DTO Reference](https://calico-shirt-218.notion.site/POS-Project-API-DTO-Reference-16ce50d8cc2e473c95610739f0d85bbc?pvs=4)
- **HTML File**: [POS Project API & DTO Reference.html](Readme-Files%2FPOS%20Project%20API%20%26%20DTO%20Reference.html)
- **PDF Report**: [API & DTO Reference.pdf](Readme-Files%2FAPI%20%26%20DTO%20Reference.pdf)

<br>

## Installation Instructions

### Quick Start
If you have Docker installed and running, you can use this quick setup method:

1. Navigate to the main directory containing `docker-compose.yml`.
2. Run the following command:

```bash
docker-compose up -d
```

This will retrieve and start all necessary services from the docker hub.

<br>

### Detailed Setup
For developers or those who need more control:

#### _Requirements_
Before proceeding with the detailed setup, ensure you have the following components:

- **Docker**: Used to containerize the application for easy deployment and management.
- **PostgreSQL**: Required with pre-configured language settings for storing and managing service records.
- **Redis**: Used for temporarily storing products added to the bag and PDF files. A default setup is sufficient.
- **RabbitMQ**: Used for messaging throughout the project. A default setup is sufficient.

**_Note:_** PostgreSQL, Redis, and RabbitMQ configurations are provided on Docker hub : `emirakts`.

<br>

#### 1. Build Services
Run maven clean build for services. Or if you are working within IntelliJ, follow the instructions in the gif.

```bash
mvn clean install -DskipTests
```

<div align="left">
  <img alt="mvn-clean-install.gif" src="Readme-Files%2Fmvn-clean-install.gif" width="400"/>
</div>

#### 2. Build Docker Images
Then build the docker image for the modules with the attached code. Or if you are working within IntelliJ, follow the instructions in the gif

```bash
docker build -t {service} .
```

<div align="left">
  <img alt="docker-build.gif" src="Readme-Files%2Fdocker-build.gif" width="400"/>
</div>

#### 3. Run Docker Compose
Finally, let's run the docker compose file with the attached code. Or if you are working within IntelliJ, follow the instructions in the gif

```bash
docker-compose up -d
```

<div align="left">
  <img alt="compose-up.gif" src="Readme-Files%2Fcompose-up.gif" width="400"/>
</div>

<br>

## Configuration
By default, Docker images use the UTC time zone. The following code snippet in the Docker build files sets the time zone to European/Istanbul. You can change this to any desired time zone by modifying the `TZ` environment variable.

```dockerfile
  ENV TZ=Europe/Istanbul
```

<br>

## AWS and Jenkins
First of all, we need an EC2 machine on AWS. Although the free tier version is sufficient for a few services, it does not meet our ram needs. For this reason, we need a minimum T2 or T3 medium machine.

**_After starting the instance, we must open the necessary ports from the security group settings._**

>
>   Gateway - 8889
>
>   Jenkins - 8080
>
>   Portainer - 9443
>
>   PostgreSQL - 5432


### Required installations
**`!`** After connecting with SSH Key or directly from AWS Console, the necessary installations should be made with the following steps.

```bash
sudo yum update -y
sudo yum install docker -y
sudo service docker start
  
sudo systemctl enable docker
```

Docker compose installation:
```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

Allows running Docker commands without using sudo:
```bash
sudo usermod -aG docker ec2-user
newgrp docker
```

<br>

### Jenkins and Portainer installation
Can be run with the following two codes or docker-compose.

#### Jenkins
```bash
docker volume create jenkins_home && docker run -d --name jenkins --user root -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v /home/ec2-user:/home/ec2-user emirakts/jenkins-with-docker
```
#### Portainer
_Note: portainer accepts requests over https._
```bash
docker run -d -p 8000:8000 -p 9443:9443 --name portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce:latest
```

#### Docker Compose for Jenkins and Portainer

> [Jenkins-Portainer Compose](Readme-Files/dockerfiles/jenkins-portainer/docker-compose.yml)

<br>

### Up The Project
Let's create and write our compose file with nano file editor.
```bash
nano docker-compose.yml
```
Then we just need to make it up.
```bash
docker-compose up -d
```

<br>

### Jenkins Settings
First enter your docker and git credentials in jenkinse and then create a pipeline job and set it up as follows.

**_1._** Git Project Connection

<div align="left">
  <img alt="compose-up.gif" src="Readme-Files%2FJ-git.png" width="400"/>
</div>

<br>

**_2._** The wewbhook trigger should be created via git as seen in the gif. setting must then be checked in jenkins

<div align="left">
  <img alt="compose-up.gif" src="Readme-Files%2FJ-webhook.gif" width="400"/>
</div>

<br>

<div align="left">
  <img alt="compose-up.gif" src="Readme-Files%2FJ-trigger.png" width="400"/>
</div>

<br>

**_3._** Finally, the pipeline must be entered.

<div align="left">
  <img alt="compose-up.gif" src="Readme-Files%2FJ-pipeline.png" width="400"/>
</div>

<br>

- **Pipeline file**: [Jenkins-Pipeline](Readme-Files/Jenkins-pipeline/Jenkinsfile)

<br>

## Contact Information

For any inquiries or support related to the Toyota & 32Bit POS Back-end Project, please contact me at:

**Email**: _emirakts00@gmail.com_
