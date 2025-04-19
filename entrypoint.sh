#!/bin/sh
set -e

# (1) 백그라운드로 continuous 컴파일 실행
./gradlew compileJava --continuous --no-daemon &

# (2) 주 프로세스로 Spring Boot 실행
exec ./gradlew bootRun --no-daemon
