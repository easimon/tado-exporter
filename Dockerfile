ARG BUILD_IMAGE=openjdk:8
ARG TEST_IMAGE=adoptopenjdk/openjdk11:alpine
ARG RUNTIME_IMAGE=adoptopenjdk/openjdk11:alpine-jre

FROM $BUILD_IMAGE as builder

WORKDIR /build

# TODO: Make this work. Goal of splitting the build like this, is, to download all dependencies in a separate layer
# and make the builder cacheable. The following `mvnw package` should work offline. But this is obviously not working
# completely for now, `mvnw package` still downloads a lot of stuff
COPY mvnw pom.xml /build/
COPY .mvn /build/.mvn/
RUN ./mvnw -B clean dependency:resolve dependency:resolve-plugins dependency:go-offline
COPY src/main/api /build/src/main/api
RUN ./mvnw -B generate-sources

COPY src /build/src/
RUN ./mvnw -B package

# Integration tests
FROM $TEST_IMAGE as test

WORKDIR /build

COPY --from=builder /root/.m2/repository /root/.m2/repository
COPY mvnw pom.xml /build/
COPY .mvn /build/.mvn/
COPY src /build/src/
COPY --from=builder /build/target /build/target
RUN ./mvnw -B surefire:test failsafe:integration-test failsafe:verify

# Build runtime image
FROM $RUNTIME_IMAGE

COPY --from=builder /build/target/tadoexporter-*.jar tadoexporter.jar
EXPOSE 8080
USER 65535:65535
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar tadoexporter.jar
