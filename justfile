#!/usr/bin/env just --justfile

path_to_graalvm := "/home/aviplayer/.jdks/graalvm-jdk-23.0.2"

build:
  ./gradlew  build
run:
  ./gradlew --stacktrace shadowJar
  {{path_to_graalvm}}/bin/java -jar build/libs/ml-svc-1.0-SNAPSHOT.jar