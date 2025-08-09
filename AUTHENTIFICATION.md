# Documentation du Système d'Authentification

## Introduction

Cette documentation présente le système d'authentification mis en place dans l'application de signature électronique. Le système permet aux utilisateurs de s'inscrire, se connecter, et gérer leur session de manière sécurisée.

## Fonctionnalités implémentées

- **Inscription** : Création de compte utilisateur
- **Connexion/Déconnexion** : Authentification des utilisateurs
- **Gestion des sessions** : Sessions utilisateurs avec limite (1 session active)
- **Hachage de mot de passe** : Sécurité renforcée avec BCrypt
- **Validation** : Contrôles côté frontend et backend
- **Design responsive** : Interface utilisateur moderne avec Bootstrap
- **Protection CSRF** : Sécurité des formulaires contre les attaques Cross-Site Request Forgery

## Architecture technique

### Configuration de sécurité

Le système utilise Spring Security avec une configuration personnalisée dans :
- `SecurityConfig.java` : Configuration des règles d'accès, formulaires de connexion, et protections
- `AuthenticationConfig.java` : Configuration du fournisseur d'authentification

### Contrôleurs

- `AuthController.java` : Gestion des requêtes d'inscription, connexion et déconnexion

### Services

- `UserService.java` : Logique métier pour la gestion des utilisateurs et l'authentification

### Templates

Les templates sont organisés dans le dossier `templates/auth/` :
- `login.html` : Page de connexion
- `register.html` : Page d'inscription
- `layout.html` : Layout commun pour les pages d'authentification

## Guide d'utilisation

### Inscription

1. Accédez à `/register`
2. Remplissez le formulaire avec vos informations
3. Cliquez sur "Créer mon compte"
4. Vous serez redirigé vers la page de connexion

### Connexion

1. Accédez à `/login` (ou la page d'accueil qui redirige vers login)
2. Entrez votre nom d'utilisateur et mot de passe
3. Cliquez sur "Se connecter"
4. Vous serez redirigé vers le tableau de bord

### Déconnexion

1. Cliquez sur le bouton de déconnexion dans la navigation
2. Vous serez redirigé vers la page de connexion

## Dépannage

### Erreur 403 (Forbidden) lors de l'inscription

Si vous rencontrez une erreur 403 lors de la soumission du formulaire d'inscription, vérifiez :

1. **Protection CSRF** : Assurez-vous que le formulaire inclut un jeton CSRF
   ```html
   <input type="hidden" name="_csrf" th:value="${_csrf.token}" />
   ```

2. **Configuration de sécurité** : La configuration doit permettre l'accès public à `/register`
   ```java
   .requestMatchers("/login", "/login/**", "/register", "/register/**", "/error").permitAll()
   ```

3. **Traitement des formulaires** : Vérifiez que le contrôleur traite correctement les requêtes POST

### Erreur de démarrage de l'application

Si l'application ne démarre pas correctement :

1. **Vérifiez les logs** pour identifier les erreurs spécifiques
2. **Configuration Maven** : Assurez-vous que le pom.xml est correctement configuré
3. **Utilisez le script `start-app-nossl.bat`** pour démarrer en mode HTTP (sans SSL)

## Configuration HTTPS/HTTP

L'application peut fonctionner en deux modes :

### Mode HTTPS (SSL)

Utilisez la configuration standard avec le profil par défaut :
```bash
mvn spring-boot:run
```

### Mode HTTP (sans SSL)

Utilisez le profil "nossl" :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=nossl
```
Ou utilisez le script fourni :
```bash
.\start-app-nossl.bat
```

## Recommandations de sécurité

1. **Utilisation de HTTPS** : En production, utilisez toujours HTTPS
2. **Mots de passe robustes** : Imposez des règles de complexité pour les mots de passe
3. **Double authentification** : Envisagez d'implémenter une authentification à deux facteurs
4. **Verrouillage de compte** : Après plusieurs tentatives de connexion échouées
5. **Journalisation** : Enregistrez les tentatives de connexion et actions sensibles

## Prochaines étapes possibles

- Récupération de mot de passe
- Gestion des profils utilisateurs
- Permissions et rôles plus granulaires
- Authentification via des fournisseurs tiers (Google, GitHub, etc.)
