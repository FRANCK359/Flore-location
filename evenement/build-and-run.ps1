# build-and-run.ps1
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "COMPILATION ET DÉPLOIEMENT AVEC DOCKER" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Nettoyer
Write-Host "`n1. Nettoyage du projet..." -ForegroundColor Yellow
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue

# 2. Compiler avec Maven
Write-Host "`n2. Compilation avec Maven..." -ForegroundColor Yellow
.\mvnw clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    exit 1
}

# 3. Vérifier que le JAR existe
Write-Host "`n3. Vérification du fichier JAR..." -ForegroundColor Yellow
$jarFile = Get-ChildItem -Path target -Filter "*.jar" | Select-Object -First 1
if ($jarFile) {
    Write-Host "✅ JAR trouvé : $($jarFile.Name)" -ForegroundColor Green
} else {
    Write-Host "❌ Aucun fichier JAR trouvé" -ForegroundColor Red
    exit 1
}

# 4. Arrêter les conteneurs existants
Write-Host "`n4. Arrêt des conteneurs..." -ForegroundColor Yellow
docker-compose down

# 5. Reconstruire et démarrer
Write-Host "`n5. Démarrage des conteneurs..." -ForegroundColor Yellow
docker-compose up -d --build

# 6. Vérifier l'état
Write-Host "`n6. État des conteneurs :" -ForegroundColor Yellow
Start-Sleep -Seconds 5
docker-compose ps

# 7. Afficher les logs
Write-Host "`n7. Logs de l'application (Ctrl+C pour arrêter) :" -ForegroundColor Yellow
docker-compose logs -f app