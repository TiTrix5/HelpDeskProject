@echo off
REM DEVOPS: сборка и запуск одной командой (нужен JDK 11+)
chcp 65001 >nul
if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java || exit /b 1
java -cp out Main
