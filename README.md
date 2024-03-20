# IPSL Task Management

An Implementation that will allow CRUD operations for managing fictional tasks.

Please find document for the APIs [here](https://documenter.getpostman.com/view/1107645/2sA358eRYM)

As part of the implementation, I have used docker compose to manage postgres and redis. The docker compose is 
managed by spring boot starter docker compose for ease of development.

To reduce the friction managing database changes, i've used liquibase. This allows
environment provision where all the required tables and initial data are added via liquibase
migrations.

To enable ease of testing both unit and integration tests, i've integrated testcontainers.

## Requirements
For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Gradle 8.6](https://gradle.org)
- [Docker](https://www.docker.com/products/docker-desktop/)

## Running the application locally
As a prerequisite, please ensure that you have installed docker.
```shell
./gradlew clean bootRun
```
## Testing the application
For the test to be green, please ensure that docker is installed and running.
```shell
./gradlew clean test
```

