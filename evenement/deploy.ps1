# deploy.ps1
param(
    [switch]$Build,
    [switch]$Up,
    [switch]$Down,
    [switch]$Logs,
    [switch]$Clean,
    [string]$Service = "app"
)

$ErrorActionPreference = "Stop"

function Show-Menu {
    Clear-Host
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "   GESTIONNAIRE DOCKER - EVENT LOCATION   " -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Démarrer l'application (build + up)"
    Write-Host "2. Arrêter l'application"
    Write-Host "3. Redémarrer l'application"
    Write-Host "4. Voir les logs"
    Write-Host "5. Nettoyer tout (down -v)"
    Write-Host "6. Accéder à PostgreSQL"
    Write-Host "7. Backup de la base de données"
    Write-Host "8. Restaurer la base de données"
    Write-Host "9. Voir l'état des conteneurs"
    Write-Host "10. Quitter"
    Write-Host ""
}

function Start-Application {
    Write-Host "`n📦 Compilation de l'application..." -ForegroundColor Yellow
    ./mvnw clean package -DskipTests

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Erreur de compilation" -ForegroundColor Red
        return
    }

    Write-Host "`n🐳 Démarrage des conteneurs..." -ForegroundColor Yellow
    docker-compose up -d --build

    Write-Host "`n✅ Application démarrée !" -ForegroundColor Green
    Write-Host "📱 Application: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "📊 Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
    Write-Host "🗄️  pgAdmin: http://localhost:5050 (admin@admin.com / admin)" -ForegroundColor Cyan
}

function Show-Logs {
    param([string]$ServiceName)

    if ($ServiceName -eq "all") {
        docker-compose logs -f
    } else {
        docker-compose logs -f $ServiceName
    }
}

function Backup-Database {
    $backupFile = "backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
    Write-Host "💾 Sauvegarde de la base de données..." -ForegroundColor Yellow

    docker exec event-postgres pg_dump -U postgres event_location_db > $backupFile

    if (Test-Path $backupFile) {
        Write-Host "✅ Sauvegarde créée: $backupFile" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de la sauvegarde" -ForegroundColor Red
    }
}

function Restore-Database {
    $files = Get-ChildItem -Filter "backup_*.sql" | Sort-Object LastWriteTime -Descending

    if ($files.Count -eq 0) {
        Write-Host "❌ Aucun fichier de backup trouvé" -ForegroundColor Red
        return
    }

    Write-Host "Fichiers disponibles :" -ForegroundColor Yellow
    for ($i = 0; $i -lt $files.Count; $i++) {
        Write-Host "$($i+1). $($files[$i].Name) ($($files[$i].LastWriteTime))"
    }

    $choice = Read-Host "Choisissez un fichier (1-$($files.Count))"
    $index = [int]$choice - 1

    if ($index -ge 0 -and $index -lt $files.Count) {
        $selectedFile = $files[$index].Name
        Write-Host "📦 Restauration de $selectedFile..." -ForegroundColor Yellow

        Get-Content $selectedFile | docker exec -i event-postgres psql -U postgres -d event_location_db

        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Restauration terminée" -ForegroundColor Green
        } else {
            Write-Host "❌ Erreur lors de la restauration" -ForegroundColor Red
        }
    }
}

# Menu principal
do {
    Show-Menu
    $choice = Read-Host "Choisissez une option"

    switch ($choice) {
        "1" { Start-Application }
        "2" {
            Write-Host "`n🛑 Arrêt des conteneurs..." -ForegroundColor Yellow
            docker-compose down
            Write-Host "✅ Conteneurs arrêtés" -ForegroundColor Green
        }
        "3" {
            Write-Host "`n🔄 Redémarrage..." -ForegroundColor Yellow
            docker-compose restart
            Write-Host "✅ Conteneurs redémarrés" -ForegroundColor Green
        }
        "4" {
            $serviceChoice = Read-Host "Service (app/postgres/pgadmin/all)"
            Show-Logs -ServiceName $serviceChoice
        }
        "5" {
            Write-Host "`n🧹 Nettoyage complet..." -ForegroundColor Yellow
            docker-compose down -v
            Write-Host "✅ Nettoyage terminé" -ForegroundColor Green
        }
        "6" {
            Write-Host "`n🔌 Connexion à PostgreSQL..." -ForegroundColor Yellow
            docker exec -it event-postgres psql -U postgres -d event_location_db
        }
        "7" { Backup-Database }
        "8" { Restore-Database }
        "9" {
            Write-Host "`n📊 État des conteneurs :" -ForegroundColor Cyan
            docker-compose ps
        }
        "10" {
            Write-Host "Au revoir !" -ForegroundColor Yellow
            break
        }
        default { Write-Host "❌ Option invalide" -ForegroundColor Red }
    }

    if ($choice -ne "4" -and $choice -ne "6" -and $choice -ne "10") {
        Write-Host "`nAppuyez sur Entrée pour continuer..." -ForegroundColor Gray
        Read-Host
    }
} while ($choice -ne "10")