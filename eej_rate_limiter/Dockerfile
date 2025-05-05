FROM openjdk:17-jdk

ARG DEPENDENCY=build/dependency

# 3 following lines leverage docker cache for inquerently changed files.
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.enjoy.ds.ratelimiter.RatelimiterApplication"]