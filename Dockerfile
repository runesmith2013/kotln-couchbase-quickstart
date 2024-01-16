FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:11
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/distributions/*.zip /app/ktor-docker-sample.zip
WORKDIR /app
RUN unzip ktor-docker-sample.zip
RUN mv com.couchbase.kotlin-quickstart-*/* .
RUN ls -lah bin
ENV KTOR_ENV=dev
ENTRYPOINT ["/app/bin/com.couchbase.kotlin-quickstart"]
