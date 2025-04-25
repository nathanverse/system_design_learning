# Repository to learn system design
This repo contains apps built with the purpose of learning system design concepts.

## I. Rate limiter
### 1. Deployment step

#### 1.1. Build Docker image
```bash
$ ./gradlew build
$ mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*SNAPSHOT.jar)
$ docker build --build-arg DEPENDENCY=build/dependency -t narutosimaha/rate-limiter:[version] .
$ docker push narutosimaha/rate-limiter:[version]
```

#### 