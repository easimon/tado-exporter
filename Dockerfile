FROM adoptopenjdk/openjdk8:alpine-slim as builder

WORKDIR /build

# TODO: Make this work. Goal of splitting the build like this, is, to download all dependencies in a separate layer
# and make the builder cacheable. The following `mvnw package` should work offline. But this is obviously not working
# completely for now, `mvnw package` still downloads a lot of stuff
COPY mvnw pom.xml /build/
COPY .mvn /build/.mvn/
RUN ./mvnw -B dependency:resolve dependency:resolve-plugins dependency:go-offline
COPY src/main/api /build/src/main/api
RUN ./mvnw -B generate-sources

COPY src /build/src/
# TODO: Add proper test coverage, and tests that don't require valid Tado credentials to execute.
RUN ./mvnw -B package -DskipTests


FROM adoptopenjdk/openjdk8:alpine-slim

COPY --from=builder /build/target/tadoexporter-*.jar tadoexporter.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar tadoexporter.jar
