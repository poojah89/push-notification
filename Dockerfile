FROM adoptopenjdk/openjdk8-openj9:alpine

ADD target/pushnotificationaut.jar app.jar

#RUN apk add --no-cache tzdata

ARG JAVA_ENV='-Xmx128m -XX:+IdleTuningGcOnIdle -Xtune:virtualized -Xscmx128m -Xscmaxaot100m'
ENV JAVA_OPS=$JAVA_ENV

ENTRYPOINT exec java $JAVA_OPS -Djava.security.egd=file:/dev/./urandom -jar /app.jar