FROM eclipse-temurin:21.0.13_9-jdk-jammy@sha256:4b1a74fbffbe6b4e7b6f0171a472478282fb463790a51edf10e465d578f22b69 AS build
WORKDIR /build
COPY backend/ ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp -DskipTests package
RUN mkdir -p /health && javac -d /health container/HealthCheck.java
FROM eclipse-temurin:21.0.13_9-jdk-jammy@sha256:4b1a74fbffbe6b4e7b6f0171a472478282fb463790a51edf10e465d578f22b69
RUN groupadd --gid 10001 lmt && useradd --uid 10001 --gid 10001 --no-create-home lmt && mkdir -p /data/otp-inbox /data/quarantine && chown -R 10001:10001 /data
WORKDIR /app
COPY --from=build /build/target/lmt-core-0.1.0-SNAPSHOT.jar /app/lmt.jar
COPY --from=build /health /app/health
COPY --chmod=755 backend/container/entrypoint.sh /app/bin/entrypoint
USER 10001:10001
ENTRYPOINT ["/app/bin/entrypoint"]
CMD ["api"]
