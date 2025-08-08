# Creating the Keystore File

The `keystore.p12` file is a binary file that cannot be directly created as text. You need to generate it using one of the following methods:

## Option 1: Using the provided scripts

Run the `generate-certificates.bat` (Windows) or `generate-certificates.sh` (Linux/Mac) script from the project root directory. This will automatically generate all required certificate files including the keystore.

## Option 2: Manual creation with OpenSSL

If you have OpenSSL installed, run this command from the project root:

```
openssl pkcs12 -export -in src/main/resources/certificates/certificate.pem -inkey src/main/resources/certificates/signing-key.pem -out src/main/resources/keystore.p12 -name signature-service -password pass:changeit
```

## Option 3: Using Java keytool

If you have Java installed, you can create a keystore with:

```
keytool -genkeypair -alias signature-service -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore.p12 -validity 3650 -storepass changeit
```

When prompted, use the following information:
- First and Last name: localhost
- Organizational unit: Development
- Organization: Electronic Signature Service
- City: City
- State: State
- Country: FR

## Option 4: Run without SSL

If you can't create the keystore file, you can run the application without SSL by using the provided non-SSL profile:

```
mvn spring-boot:run -Dspring-boot.run.profiles=nossl
```

This will start the application on port 8080 using HTTP instead of HTTPS.
