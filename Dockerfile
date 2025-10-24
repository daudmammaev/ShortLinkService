FROM ubuntu:latest
LABEL authors="user"
ARG JAR_FILE=out/artifacts/ShortLinks_jar/short-links.jar
ENTRYPOINT ["top", "-b", "java","-jar","app.jar"]


