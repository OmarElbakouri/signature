@echo off
echo ===================================================
echo Certificate Generator for Electronic Signature Service
echo ===================================================

REM Create certificates directory if it doesn't exist
if not exist "src\main\resources\certificates" mkdir "src\main\resources\certificates"

REM Generate a new RSA private key
echo Generating RSA private key...
openssl genrsa -out src\main\resources\certificates\signing-key.pem 2048

REM Generate a self-signed certificate
echo Generating self-signed certificate...
openssl req -new -x509 -key src\main\resources\certificates\signing-key.pem -out src\main\resources\certificates\certificate.pem -days 3650 -subj "/C=FR/ST=State/L=City/O=Electronic Signature Service/OU=Development/CN=localhost" -nodes

REM Generate PKCS12 keystore for SSL
echo Generating PKCS12 keystore for SSL...
openssl pkcs12 -export -in src\main\resources\certificates\certificate.pem -inkey src\main\resources\certificates\signing-key.pem -out src\main\resources\keystore.p12 -name signature-service -passout pass:changeit

echo ===================================================
echo Certificate generation completed!
echo ===================================================
echo The following files have been created:
echo - src\main\resources\certificates\signing-key.pem (Private key for PDF signing)
echo - src\main\resources\certificates\certificate.pem (Certificate for PDF signing)
echo - src\main\resources\keystore.p12 (Keystore for SSL/HTTPS)
echo ===================================================
echo You can now start the application with SSL enabled.
echo ===================================================

pause
