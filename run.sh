#!/bin/bash
# Script de lancement du jeu LibGDX Platformer
# Linux / macOS bash script

echo "========================================"
echo "LibGDX Platformer Game Engine"
echo "========================================"
echo

# Vérifier si Gradle est disponible
GRADLE_CMD="gradle"
LOCAL_GRADLE="./.gradle_local/gradle-8.5/bin/gradle"

if [ -f "$LOCAL_GRADLE" ]; then
    echo "[INFO] Using local Gradle: $LOCAL_GRADLE"
    GRADLE_CMD="$LOCAL_GRADLE"
    chmod +x "$LOCAL_GRADLE"
elif ! command -v gradle &> /dev/null; then
    echo "[ERREUR] Gradle n'est pas installé ou pas dans le PATH"
    echo "Veuillez installer Gradle depuis https://gradle.org/"
    exit 1
fi

# Vérifier la version de Java
if ! command -v java &> /dev/null; then
    echo "[ERREUR] Java n'est pas installé ou pas dans le PATH"
    echo "Veuillez installer Java 11+ depuis https://www.oracle.com/java/"
    exit 1
fi

echo "[OK] Java et Gradle trouvés"
echo

# Compilation
echo "[COMPILATION] Compilation du projet..."
$GRADLE_CMD clean build
if [ $? -ne 0 ]; then
    echo "[ERREUR] La compilation a échoué"
    exit 1
fi

echo "[OK] Compilation réussie"
echo

# Lancement
echo "[LANCEMENT] Démarrage du jeu..."
$GRADLE_CMD run
