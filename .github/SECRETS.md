# Configuration des Secrets GitHub

Pour que les pipelines CI/CD fonctionnent correctement, vous devez configurer les secrets suivants dans votre dépôt GitHub.

## Accès aux Secrets

Allez dans : **Settings** > **Secrets and variables** > **Actions** > **New repository secret**

## Secrets Requis pour le CD (Déploiement)

### 1. `VPS_HOST`
- **Description** : Adresse IP ou nom de domaine de votre serveur VPS
- **Exemple** : `192.168.1.100` ou `monserveur.com`

### 2. `VPS_USER`
- **Description** : Nom d'utilisateur SSH pour se connecter au VPS
- **Exemple** : `ubuntu`, `root`, `deploy`

### 3. `VPS_SSH_KEY`
- **Description** : Clé privée SSH pour l'authentification (format PEM)
- **Comment obtenir** :
  ```bash
  # Sur votre machine locale, générez une paire de clés SSH
  ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_deploy

  # Ajoutez la clé publique sur le VPS
  ssh-copy-id -i ~/.ssh/github_deploy.pub user@your-vps

  # Copiez le contenu de la clé privée
  cat ~/.ssh/github_deploy
  ```
- **Note** : Collez tout le contenu incluant `-----BEGIN OPENSSH PRIVATE KEY-----` et `-----END OPENSSH PRIVATE KEY-----`

## Configuration du Serveur VPS

Sur votre serveur VPS, créez le répertoire de déploiement :

```bash
# Se connecter au VPS
ssh user@your-vps

# Créer le répertoire de déploiement
sudo mkdir -p /opt/evenement
sudo chown $USER:$USER /opt/evenement
cd /opt/evenement

# Créer le fichier docker-compose.yml
# Copiez le contenu de votre docker-compose.yml local

# Créer le fichier .env avec vos variables de production
nano .env
```

### Exemple de fichier `.env` pour la production

```env
# Database
DB_USER=postgres
DB_PASSWORD=VOTRE_MOT_DE_PASSE_SECURISE
DB_NAME=event_location_db
DB_PORT=5432

# Application
APP_PORT=8080
APP_BASE_URL=https://votre-domaine.com

# JWT (générez une clé sécurisée de 64+ caractères)
JWT_SECRET=votre-cle-jwt-tres-longue-et-securisee-ici

# Email Configuration
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=votre-email@gmail.com
SPRING_MAIL_PASSWORD=votre-app-password

APP_EMAIL_FROM=noreply@votre-domaine.com
APP_EMAIL_FROM_NAME=Location Événement
```

## Permissions GitHub Packages

Le pipeline CD utilise GitHub Container Registry (GHCR) pour stocker les images Docker.

### Configuration automatique
Les permissions sont déjà configurées dans le workflow :
```yaml
permissions:
  contents: read
  packages: write
```

Le `GITHUB_TOKEN` est automatiquement fourni par GitHub Actions.

## Vérification

Pour vérifier que tout fonctionne :

1. **Pipeline CI** : Déclenché automatiquement sur chaque push/PR vers `main`
2. **Pipeline CD** : Déclenché automatiquement sur chaque push vers `main`
3. **Déploiement manuel** : Utilisez l'onglet "Actions" > "CD" > "Run workflow"

## Sécurité

- ⚠️ Ne commitez **JAMAIS** de secrets dans le code
- ⚠️ Utilisez des mots de passe forts et uniques pour la production
- ⚠️ Limitez l'accès SSH au VPS (utilisez fail2ban, changez le port SSH, etc.)
- ⚠️ Utilisez HTTPS en production avec Let's Encrypt
