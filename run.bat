@echo off
setlocal
REM Script de lancement du jeu LibGDX Platformer
REM Windows batch file

echo ========================================
echo LibGDX Platformer Game Engine Launcher
echo ========================================
echo.

set "LOCAL_GRADLE=%~dp0.gradle_local\gradle-8.5\bin\gradle.bat"

REM S'assurer que JAVA_HOME est valide (Gradle s'appuie souvent dessus)
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo [INFO] JAVA_HOME=%JAVA_HOME%
    ) else (
        echo [WARN] JAVA_HOME est defini mais invalide : %JAVA_HOME%
        set "JAVA_HOME="
    )
)

REM Si JAVA_HOME n'est pas defini, essayer de le deduire depuis `where java`
if not defined JAVA_HOME (
    for /f "usebackq delims=" %%J in (`where java 2^>nul`) do (
        set "JAVA_EXE=%%J"
        goto :got_java
    )
)

:got_java
if not defined JAVA_HOME (
    if defined JAVA_EXE (
        for %%I in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpI"
        for %%I in ("%JAVA_BIN%..") do set "JAVA_HOME=%%~fI"
        if exist "%JAVA_HOME%\bin\java.exe" (
            echo [INFO] JAVA_HOME detecte : %JAVA_HOME%
        ) else (
            set "JAVA_HOME="
        )
    )
)

REM Dernier recours : chemin courant d'installation typique (JDK 21)
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
        echo [INFO] JAVA_HOME defini automatiquement : %JAVA_HOME%
    )
)

REM 1. Essayer dutiliser Gradle Local
if exist "%LOCAL_GRADLE%" (
    echo [INFO] Utilisation de Gradle Local...
    call "%LOCAL_GRADLE%" run
    goto :end
)

REM 2. Essayer dutiliser Gradle Global
where gradle >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] Utilisation de Gradle Global...
    call gradle run
    goto :end
)

REM 3. Si aucun Gradle n'est trouve
echo [ERREUR] Gradle n'est pas trouve.
echo.
echo Veuillez executer le script d'installation automatique :
echo Right-click 'setup_and_run.ps1' -> Run with PowerShell
echo Ou executez la commande suivante dans PowerShell :
echo powershell -ExecutionPolicy Bypass -File setup_and_run.ps1
echo.

:end
if not defined JAVA_HOME (
    echo.
    echo [WARN] JAVA_HOME n'est pas defini.
    echo Si Gradle echoue avec JAVA_HOME invalide, installez un JDK (ex: 21) et definissez :
    echo   setx JAVA_HOME "C:\Program Files\Java\jdk-21"
    echo Puis relancez ce script.
)
pause

endlocal
