@echo off
chcp 65001 >nul
if not exist out-test mkdir out-test
javac -encoding UTF-8 -d out-test src\Ticket.java src\TicketStore.java test\TicketStoreTest.java || exit /b 1
java -Dstdout.encoding=UTF-8 -cp out-test TicketStoreTest
