FROM gradle:8.10.1-jdk21
WORKDIR /app

# 1) Gradle Wrapper & 빌드 스크립트 복사 → 의존성 캐시용
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 2) 소스코드 전체 복사
COPY src ./src

# 3) 엔트리포인트 복사
COPY entrypoint.sh ./
RUN chmod +x entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["./entrypoint.sh"]
