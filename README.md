# Toyota & 32Bit POS Back-end Project

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
3. [Requirements](#requirements)
4. [Installation Instructions](#installation-instructions)
5. [API & DTO Reference](#api--dto-reference)
6. [Configuration Settings](#configuration-settings)


## Overview
_POS Project (Point of Sale) is a web-based system for efficient and secure sales operations. Features include user management, product management, sales processing, and reporting. It generates Excel reports and PDF invoices. Easily deployable with Docker._

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

<div align="center">
  <img alt="NoBack.png" src="Readme-Files%2FNoBack.png" width="800"/>
</div>

## Requirements
#### Docker
* Docker is used to containerize the application, making it easier to deploy and manage.
#### PostgreSQL
* The project requires PostgreSQL with pre-configured language settings. This database is used to store and manage service records.
#### Redis
* Used for temporarily storing products added to the bag and PDF files. A default Redis setup without additional configuration is sufficient.
* It is defined as default in the docker.compose file.
#### RabbitMQ
* Used extensively throughout the project for messaging. A default RabbitMQ setup without additional configuration is sufficient, as necessary settings are configured within Spring.
* It is defined as default in the docker.compose file.

---

## Installation Instructions
_Follow these steps for installation_

_**1.**_ Run maven clean build for services. Or if you are working within IntelliJ, follow the instructions in the gif.
```bash
  mvn clean install -DskipTests
```
<div align="left">
  <img alt="mvn-clean-install.gif" src="Readme-Files%2Fmvn-clean-install.gif" width="400"/>
</div>

**_2._** Then build the docker image for the modules with the attached code. Or if you are working within IntelliJ, follow the instructions in the gif
```bash
  docker build -t {service} .
```
<div align="left">
  <img alt="docker-build.gif" src="Readme-Files%2Fdocker-build.gif" width="400"/>
</div>

**_3._** Finally, let's run the docker compose file with the attached code. Or if you are working within IntelliJ, follow the instructions in the gif
```bash
  docker-compose up
```
<div align="left">
  <img alt="compose-up.gif" src="Readme-Files%2Fcompose-up.gif" width="400"/>
</div>


## API & DTO Reference

> For detailed API and DTO reference, visit the following link:
>
> [POS Project API & DTO Reference](https://calico-shirt-218.notion.site/POS-Project-API-DTO-Reference-16ce50d8cc2e473c95610739f0d85bbc?pvs=4)
> 
> Or you can build the html file below.
> 
> [POS Project API & DTO Reference.html](Readme-Files%2FPOS%20Project%20API%20%26%20DTO%20Reference.html)
> 
> Or you can review the pdf report.
> 
> [API & DTO Reference.pdf](Readme-Files%2FAPI%20%26%20DTO%20Reference.pdf)

## Configuration Settings
By default, Docker images use the UTC time zone. The following code snippet in the Docker build files sets the time zone to European/Istanbul. You can change this to any desired time zone by modifying the `TZ` environment variable.

```dockerfile
  ENV TZ=Europe/Istanbul
```


