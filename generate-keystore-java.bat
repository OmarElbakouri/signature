@echo off
echo ===================================================
echo Keystore Generator using Java keytool
echo ===================================================

REM Create certificates directory if it doesn't exist
if not exist "src\main\resources" mkdir "src\main\resources"

REM Generate keystore with self-signed certificate
echo Generating keystore with self-signed certificate...
keytool -genkeypair -alias signature-service -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src\main\resources\keystore.p12 -validity 3650 -storepass changeit -dname "CN=localhost, OU=Development, O=Electronic Signature Service, L=City, ST=State, C=FR" -keypass changeit

echo ===================================================
echo Keystore generation completed!
echo ===================================================
echo The following file has been created:
echo - src\main\resources\keystore.p12 (Keystore for SSL/HTTPS)
echo ===================================================
echo You can now start the application with SSL enabled.
echo ===================================================

pause
