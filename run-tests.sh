#!/bin/sh
# DEVOPS/ТЕСТИРОВЩИК: запуск автотестов
set -e
mkdir -p out-test
javac -encoding UTF-8 -d out-test src/Ticket.java src/TicketStore.java test/TicketStoreTest.java
java -Dstdout.encoding=UTF-8 -cp out-test TicketStoreTest
