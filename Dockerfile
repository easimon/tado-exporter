ARG BUILD_IMAGE=adoptopenjdk:11-hotspot
ARG TEST_IMAGE=adoptopenjdk:15-hotspot
ARG RUNTIME_IMAGE=adoptopenjdk:15-jre-hotspot

FROM $BUILD_IMAGE as builder

WORKDIR /build

COPY .mvn /build/.mvn/
COPY mvnw pom.xml /build/
COPY tado-api/pom.xml /build/tado-api/pom.xml
COPY tado-util/pom.xml /build/tado-util/pom.xml
COPY tado-exporter/pom.xml /build/tado-exporter/pom.xml

RUN ./mvnw -B de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

COPY tado-api/src /build/tado-api/src
RUN ./mvnw -DskipTests -B -pl tado-api -am install

COPY tado-util/src /build/tado-util/src
RUN ./mvnw -DskipTests -B -pl tado-util -am install

COPY tado-exporter/src /build/tado-exporter/src
RUN ./mvnw -DskipTests -B package

# Integration tests
FROM $TEST_IMAGE as test

WORKDIR /build

COPY --from=builder /root/.m2/repository /root/.m2/repository
COPY --from=builder /build /build

RUN ./mvnw -B verify

# Build runtime image
FROM $RUNTIME_IMAGE

COPY --from=builder /build/tado-exporter/target/tado-exporter-*.jar tado-exporter.jar
ENV JAVA_OPTS -Xmx64m -Xms64m
EXPOSE 8080
USER 65535:65535
CMD exec java ${JAVA_OPTS} -jar tado-exporter.jar
