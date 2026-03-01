## Configuration Email

### Pour le développement local (IntelliJ)

1. **Avec Gmail** (pour les vrais envois) :
    - Ouvrez `src/main/resources/application.properties`
    - Remplacez les valeurs :
      ```properties
      spring.mail.username=VOTRE_EMAIL@gmail.com
      spring.mail.password=VOTRE_MOT_DE_PASSE_APPLICATION
      ```

2. **Avec Mailtrap** (pour les tests, recommandé) :
    - Créez un compte sur [Mailtrap](https://mailtrap.io)
    - Dans `application.properties`, commentez la config Gmail
    - Activez la config Mailtrap :
      ```properties
      spring.mail.host=smtp.mailtrap.io
      spring.mail.port=2525
      spring.mail.username=votre-username-mailtrap
      spring.mail.password=votre-password-mailtrap
      ```

### Pour Docker

1. Copiez le fichier `.env.example` vers `.env` :
   ```bash
   cp .env.example .env