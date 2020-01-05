FROM adoptopenjdk/openjdk8:alpine-slim as builder

WORKDIR /build

# TODO:
# goal of splitting the build like this, is, to download all dependencies in a separate layer and make the builder
# cacheable. But this is obviously not working completely for now, since the following mvnw package still downloads
# stuff
COPY mvnw pom.xml /build/
COPY .mvn /build/.mvn/
RUN ./mvnw -B dependency:resolve dependency:resolve-plugins dependency:go-offline
COPY src/main/api /build/src/main/api
RUN ./mvnw -B generate-sources

COPY src /build/src/
RUN ./mvnw -B package -DskipTests


FROM adoptopenjdk/openjdk8:alpine-slim

COPY --from=builder /build/target/tadoexporter-*.jar tadoexporter.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar tadoexporter.jar
