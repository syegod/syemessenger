# Multi-Server Messenger App

## Overview

The **Multi-Server Messenger App** is a fault-tolerant, highly performant messaging system designed to ensure smooth operation across multiple servers. Its architecture allows for scalability, resilience, and efficient handling of message data, ensuring that the system remains operational even in the event of server failures.

## Features

- **Multi-server architecture**: Ensures messages are distributed and handled across multiple servers to avoid single points of failure.
- **Fault-tolerant system**: Automatically recovers from server crashes or failures.
- **High performance**: Optimized message handling and data processing for fast communication.
- **Persistence**: Reliable storage and retrieval of messages using a robust database system.
- **Kafka integration**: Handles message distribution and real-time updates efficiently.
- **Comprehensive testing**: Implemented unit, integration, and containerized tests to ensure system reliability and correctness.

## Tech Stack

- **Framework**: Spring DI, Spring Kafka
- **Web Server**: Jetty
- **Database**: PostgreSQL with Spring Data JPA and Hibernate
- **ORM**: Hibernate
- **Transaction Management**: HikariCP
- **Database Migration**: Liquibase
- **Messaging**: Kafka
- **Serialization**: SBE (Simple Binary Encoding)
- **Testing**: JUnit, Mockito, Testcontainers
- **Logging**: SLF4J
- **Containerization**: Docker
- **JSON Processing**: Jackson
- **Java 21+**
- **Maven**
- **Docker** (for running the Postgres database and Kafka)

## Architecture

The system is built with a focus on:

1. **Scalability**: Designed to handle growing loads by distributing tasks across multiple servers.
2. **Resilience**: Fault-tolerant mechanisms ensure the system remains operational even if one or more servers go down.
3. **Performance**: Uses optimized protocols like **SBE** for efficient message serialization, ensuring fast communication between clients and servers.
4. **Persistence**: Relies on **PostgreSQL** for storing messages and system data, with **Liquibase** managing schema migrations.

### Additional Details

You can find in-depth documentation on the architecture, message flow, and server configuration in the [Wiki](https://github.com/syegod/syemessenger/wiki).

