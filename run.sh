#!/bin/sh
# DEVOPS: сборка и запуск одной командой (нужен JDK 11+)
set -e
mkdir -p out
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
