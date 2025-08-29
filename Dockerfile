FROM openjdk:21
ARG jarFile=target/EffectiveMobileBank-0.0.1-SNAPSHOT.jar
WORKDIR /opt/app
COPY ${jarFile} EffectiveMobileBank.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "EffectiveMobileBank.jar"]

