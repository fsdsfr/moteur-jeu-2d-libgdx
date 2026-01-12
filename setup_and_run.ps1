# Script d'installation et de lancement automatique / Automatic setup and launch script
# Ce script télécharge une version locale de Gradle pour exécuter le projet sans installation globale.

$gradleVersion = "8.5"
$env:JAVA_HOME = $null  # Force usage of PATH java
$gradleUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
$installDir = Join-Path $PSScriptRoot ".gradle_local"
$zipFile = Join-Path $installDir "gradle.zip"
$gradleHome = Join-Path $installDir "gradle-$gradleVersion"
$gradleBin = Join-Path $gradleHome "bin\gradle.bat"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   Configuration Automatique / Auto Setup" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Vérifier si Gradle est déjà là
if (-not (Test-Path $gradleBin)) {
    Write-Host "[INFO] Gradle local non trouvé. Téléchargement en cours..." -ForegroundColor Yellow
    Write-Host "[INFO] Cela peut prendre quelques minutes..." -ForegroundColor Yellow
    
    # Créer le dossier
    if (-not (Test-Path $installDir)) {
        New-Item -ItemType Directory -Force -Path $installDir | Out-Null
    }

    # Télécharger
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $gradleUrl -OutFile $zipFile -UseBasicParsing
    }
    catch {
        Write-Error "[ERREUR] Échec du téléchargement. Vérifiez votre connexion internet."
        exit 1
    }

    Write-Host "[INFO] Téléchargement terminé. Extraction..." -ForegroundColor Yellow
    
    # Extraire
    try {
        Expand-Archive -Path $zipFile -DestinationPath $installDir -Force
    }
    catch {
        Write-Error "[ERREUR] Échec de l'extraction."
        exit 1
    }

    # Nettoyer
    Remove-Item $zipFile -ErrorAction SilentlyContinue
    
    Write-Host "[OK] Gradle a été installé dans le dossier du projet." -ForegroundColor Green
} else {
    Write-Host "[OK] Gradle local trouvé." -ForegroundColor Green
}

# 2. Lancer le jeu
Write-Host "`n[LANCEMENT] Démarrage du moteur de jeu..." -ForegroundColor Cyan
Write-Host "commande: $gradleBin clean run" -ForegroundColor Gray

& $gradleBin clean run
