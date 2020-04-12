ARG BUILD_IMAGE=openjdk:8
ARG TEST_IMAGE=adoptopenjdk/openjdk11:alpine
ARG RUNTIME_IMAGE=adoptopenjdk/openjdk11:alpine-jre

FROM $BUILD_IMAGE as builder

WORKDIR /build

# TODO: Make this work. Goal of splitting the build like this, is, to download all dependencies in a separate layer
# and make the builder cacheable. The final `mvnw package` should work offline. But this is obviously not working
# completely for now, `mvnw package` still downloads a lot of stuff
COPY .mvn /build/.mvn/
COPY mvnw pom.xml /build/

COPY tado-exporter/pom.xml /build/tado-exporter/pom.xml
COPY tado-util/pom.xml /build/tado-util/pom.xml

COPY tado-api /build/tado-api
RUN ./mvnw -B -pl tado-api -am install

COPY tado-util/src /build/tado-util/src
RUN ./mvnw -B -pl tado-util -am install

RUN ./mvnw -B clean dependency:resolve dependency:resolve-plugins dependency:go-offline

COPY tado-exporter/src /build/tado-exporter/src
RUN ./mvnw -B package

# Integration tests
FROM $TEST_IMAGE as test

WORKDIR /build

COPY --from=builder /root/.m2/repository /root/.m2/repository
COPY --from=builder /build /build

RUN ./mvnw -B surefire:test failsafe:integration-test failsafe:verify

# Build runtime image
FROM $RUNTIME_IMAGE

COPY --from=builder /build/tado-exporter/target/tado-exporter-*.jar tado-exporter.jar
EXPOSE 8080
USER 65535:65535
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar tado-exporter.jar
