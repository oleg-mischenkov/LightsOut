# LightsOut Project

This is a Java application built with Maven and Java 17. The application accepts a file path as an input argument, performs specific calculations, outputs the result, and then terminates.

## Prerequisites

- Docker must be installed on your machine.

## Dockerfile

The provided Dockerfile uses a multi-stage build:

1. **Build Stage:** Uses Maven to build the project and generate the JAR file.
2. **Final Stage:** Uses a lightweight OpenJDK image to run the application.

## How to Build the Docker Image

Open your terminal in the project root directory and run:

```bash
docker build -t lightsout .
```

## How to Run

1.Open terminal and run the command

```bash
docker run -it lightsout
```
2.Inside the container, run the application with the desired resource file as an argument. For example:

```bash
java -jar lightsout 04.txt
```
The application will execute with the provided file.