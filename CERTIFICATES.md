# Certificates for Electronic Signature Service

Ce document explique comment générer et configurer les certificats nécessaires pour le service de signature électronique.

## Certificats requis

Le projet nécessite les certificats suivants :

1. **Keystore PKCS12** (`keystore.p12`) - Pour la configuration SSL/HTTPS du serveur
2. **Clé privée** (`signing-key.pem`) - Pour signer les documents PDF
3. **Certificat public** (`certificate.pem`) - Pour vérifier les signatures

## Options pour générer les certificats

### Option 1 : Script automatique (recommandé)

Nous fournissons des scripts pour générer automatiquement tous les certificats nécessaires.

**Pour Windows :**
```
generate-certificates.bat
```

**Pour Linux/Mac :**
```
chmod +x generate-certificates.sh
./generate-certificates.sh
```

Ces scripts vont créer tous les fichiers nécessaires dans le dossier `src/main/resources/certificates/` et le keystore dans `src/main/resources/`.

### Option 2 : Commandes manuelles OpenSSL

Si vous préférez générer les certificats manuellement, voici les commandes OpenSSL à exécuter :

1. **Créer le dossier pour les certificats :**
   ```
   mkdir -p src/main/resources/certificates
   ```

2. **Générer une clé privée RSA :**
   ```
   openssl genrsa -out src/main/resources/certificates/signing-key.pem 2048
   ```

3. **Générer un certificat auto-signé :**
   ```
   openssl req -new -x509 -key src/main/resources/certificates/signing-key.pem -out src/main/resources/certificates/certificate.pem -days 3650 -subj "/C=FR/ST=State/L=City/O=Electronic Signature Service/OU=Development/CN=localhost"
   ```

4. **Créer un keystore PKCS12 pour SSL :**
   ```
   openssl pkcs12 -export -in src/main/resources/certificates/certificate.pem -inkey src/main/resources/certificates/signing-key.pem -out src/main/resources/keystore.p12 -name signature-service -password pass:changeit
   ```

### Option 3 : Exécuter l'application sans SSL

Pour tester l'application sans SSL, nous fournissons un profil de configuration alternatif :

```
mvn spring-boot:run -Dspring-boot.run.profiles=nossl
```

Ou avec Java :

```
java -jar target/electronic-signature-service.jar --spring.profiles.active=nossl
```

L'application sera alors accessible sur le port 8080 en HTTP : http://localhost:8080

## Configuration Docker

Si vous utilisez Docker, les certificats doivent être présents dans le dossier `certificates/` à la racine du projet. Le conteneur les montera automatiquement dans le chemin approprié.

Pour démarrer avec Docker :

```
docker-compose up -d
```

## Mots de passe par défaut

- **Keystore password** : `changeit`
- **Key alias** : `signature-service`

## Remarques importantes

- Ces certificats sont destinés à un usage de développement et de test uniquement.
- Pour un environnement de production, utilisez des certificats émis par une autorité de certification reconnue.
- Le mot de passe du keystore (`changeit`) devrait être modifié pour un environnement de production.
