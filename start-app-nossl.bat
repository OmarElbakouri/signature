@echo off
echo ===================================================
echo Démarrage de l'application en mode HTTP (sans SSL)
echo ===================================================

call mvn spring-boot:run "-Dspring-boot.run.profiles=nossl"

pause
