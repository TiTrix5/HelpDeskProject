# DEVOPS: запуск в контейнере
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY src ./src
COPY web ./web
RUN mkdir out && javac -encoding UTF-8 -d out src/*.java
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-cp", "out", "Main"]
