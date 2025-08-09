@echo off
echo ===================================================
echo Démarrage de l'application en mode HTTPS (port 8443)
echo ===================================================
echo Vérification du certificat SSL...
if not exist "src\main\resources\keystore.p12" (
    echo Génération du certificat SSL...
    keytool -genkeypair -alias signature-service -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src\main\resources\keystore.p12 -validity 365 -dname "CN=localhost, OU=Development, O=Electronic Signature Service, L=City, ST=State, C=US" -storepass changeit -keypass changeit
    echo Certificat SSL généré avec succès!
)
echo Démarrage avec SSL activé...
mvn spring-boot:run -Dspring-boot.run.profiles=ssl
pause
