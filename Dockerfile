FROM maven:3.9.6-eclipse-temurin-21 AS build
LABEL authors="user"
ARG JAR_FILE=out/artifacts/ShortLinks_jar/short-links.jar
ENTRYPOINT ["top", "-b", "java","-jar","app.jar"]


