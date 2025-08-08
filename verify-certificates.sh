#!/bin/bash

echo "==================================================="
echo "Certificate Verification for Electronic Signature Service"
echo "==================================================="

CERT_DIR="src/main/resources/certificates"
KEYSTORE="src/main/resources/keystore.p12"

echo "Checking certificate directory..."
if [ -d "$CERT_DIR" ]; then
    echo "[OK] Certificate directory exists: $CERT_DIR"
else
    echo "[ERROR] Certificate directory not found: $CERT_DIR"
    echo "Creating directory..."
    mkdir -p "$CERT_DIR"
    echo "Directory created."
fi

echo
echo "Checking certificate files..."
if [ -f "$CERT_DIR/certificate.pem" ]; then
    echo "[OK] Certificate file exists: $CERT_DIR/certificate.pem"
else
    echo "[ERROR] Certificate file not found: $CERT_DIR/certificate.pem"
    echo "Please run generate-certificates.sh to create the certificate files."
fi

if [ -f "$CERT_DIR/signing-key.pem" ]; then
    echo "[OK] Private key file exists: $CERT_DIR/signing-key.pem"
else
    echo "[ERROR] Private key file not found: $CERT_DIR/signing-key.pem"
    echo "Please run generate-certificates.sh to create the private key file."
fi

echo
echo "Checking keystore file..."
if [ -f "$KEYSTORE" ]; then
    echo "[OK] Keystore file exists: $KEYSTORE"
else
    echo "[WARNING] Keystore file not found: $KEYSTORE"
    echo "Please run generate-certificates.sh to create the keystore file."
    echo "Alternatively, you can run the application with the nossl profile:"
    echo "mvn spring-boot:run -Dspring-boot.run.profiles=nossl"
fi

echo
echo "==================================================="
echo "Certificate verification completed!"
echo "==================================================="
echo "If any errors were reported, please run generate-certificates.sh"
echo "to create the missing certificate files."
echo
echo "You can also run the application with the nossl profile:"
echo "mvn spring-boot:run -Dspring-boot.run.profiles=nossl"
echo "==================================================="
