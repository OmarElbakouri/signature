@echo off
echo Initializing PostgreSQL database for Electronic Signature Service...

REM Set PostgreSQL path
set PSQL_PATH="C:\Program Files\PostgreSQL\17\bin\psql.exe"

REM Run initialization script
%PSQL_PATH% -U postgres -f init.sql

REM Check if the command was successful
if %ERRORLEVEL% EQU 0 (
    echo Database initialization completed successfully.
) else (
    echo Error: Database initialization failed.
    echo Please check if PostgreSQL is running and if the postgres user has the correct permissions.
    echo You may be prompted for the postgres user password.
)

pause
