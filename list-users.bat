@echo off
echo Listing PostgreSQL users...

REM Set PostgreSQL path
set PSQL_PATH="C:\Program Files\PostgreSQL\17\bin\psql.exe"

REM List users with -l flag (list databases)
echo This will show databases and their owners:
%PSQL_PATH% -U postgres -l

echo.
echo If you know a valid username and password, you can use those credentials
echo in the application configuration files (application.yml and application-nossl.yml).

pause
