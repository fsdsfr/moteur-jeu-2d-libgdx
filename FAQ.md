# FAQ et Dépannage

## Questions Fréquemment Posées

### Installation et Configuration

#### Q1: Que dois-je installer pour faire fonctionner le projet?

**R:** Vous avez besoin de :
- Java Development Kit 11+ ([télécharger ici](https://www.oracle.com/java/technologies/downloads/))
- Gradle 6.0+ ([télécharger ici](https://gradle.org/releases/))
- Un IDE comme IntelliJ IDEA, Eclipse ou VS Code (optionnel mais recommandé)

Vérifiez l'installation :
```bash
java -version
gradle -v
```

#### Q2: Comment compiler le projet?

**R:** Exécutez dans le répertoire racine :
```bash
gradle clean build
```

Cela créera un build dans `build/`.

#### Q3: Comment lancer le jeu?

**R:** Plusieurs options :

**Windows :**
```bash
run.bat
```

**Linux/macOS :**
```bash
bash run.sh
```

**Avec Gradle :**
```bash
gradle run
```

---

## Dépannage Technique

### Problèmes de Compilation

#### Erreur: "gradle: command not found"

**Cause :** Gradle n'est pas installé ou pas dans le PATH.

**Solution :**
1. [Télécharger Gradle](https://gradle.org/releases/)
2. Extraire dans `C:\Program Files\` (Windows) ou `/usr/local/` (Linux)
3. Ajouter au PATH :
   - **Windows :** Ajouter `C:\Program Files\gradle-x.x.x\bin` aux variables d'environnement
   - **Linux/macOS :** Ajouter à `~/.bashrc` : `export PATH=$PATH:/path/to/gradle/bin`
4. Tester : `gradle -v`

#### Erreur: "Java not found"

**Cause :** Java n'est pas installé ou pas dans le PATH.

**Solution :**
1. [Télécharger Java JDK](https://www.oracle.com/java/technologies/downloads/)
2. Installer avec les chemins par défaut
3. Tester : `java -version`
4. Vérifier que `JAVA_HOME` est défini

#### Erreur: "GDX library not found"

**Cause :** Les dépendances n'ont pas été téléchargées.

**Solution :**
```bash
gradle clean build --refresh-dependencies
```

### Problèmes de Runtime

#### Le jeu se lance mais reste blanc

**Cause :** Peut être une erreur de rendu ou de chargement de ressources.

**Solution :**
1. Vérifier la console pour les erreurs
2. Vérifier que `assets/maps/` contient `GrassLandsSimple.tmx`
3. Vérifier le chemin des tilesets

#### Erreur: "FileNotFoundException: assets/maps/..."

**Cause :** Le fichier de carte n'est pas trouvé.

**Solution :**
1. Vérifier que la carte est dans `assets/maps/`
2. Vérifier l'extension du fichier (`.tmx`)
3. Vérifier le nom dans `GameScreen.java` : `currentMapName = "GrassLandsSimple"`

#### Erreur: "Invalid map file"

**Cause :** Le fichier TMX est corrompu ou mal formaté.

**Solution :**
1. Rouvrir le fichier dans Tiled
2. Vérifier que les couches sont présentes
3. Exporter à nouveau en `.tmx`
4. Placer dans `assets/maps/`

### Problèmes de Joueur

#### Le joueur ne se voit pas

**Cause :** Système de rendu de sprites non implémenté.

**Solution :** Implémenter la méthode `render()` dans `Player.java` :
```java
@Override
public void render() {
    // Temporaire : afficher un rectangle blanc
    batch.draw(Texture blanche, position.x, position.y, size.x, size.y);
}
```

#### Le joueur ne saute pas / tombe infiniment

**Cause :** Problème de collision avec le terrain.

**Solution :**
1. Vérifier que la couche "Collision" existe dans Tiled
2. Vérifier que les rectangles de collision sont bien placés
3. Vérifier la gravité dans `player_config.json`
4. Implémenter la détection de collision :
```java
// Dans GameScreen.update()
if (player.collidesWith(someEntity)) {
    player.setOnGround(true);
}
```

#### Le joueur se déplace trop vite / trop lentement

**Cause :** Paramètres de mouvement incorrects.

**Solution :** Ajuster dans `assets/config/player_config.json` :
```json
{
  "maxMovementSpeed": 280,      // Augmenter pour plus rapide
  "movementAcceleration": 20,   // Augmenter pour accélération rapide
  "gravityStrength": 1500       // Augmenter pour tomber plus vite
}
```

### Problèmes de Performance

#### FPS instables / jeu saccadé

**Cause :** Peut être des entités mal gérées ou un rendu inefficace.

**Solution :**
1. Vérifier le nombre d'entités : `world.getEntities().size()`
2. Réduire les textures de grande taille
3. Utiliser des TextureAtlas au lieu de textures individuelles
4. Profiler avec le DevTools de LibGDX

#### Fuite mémoire / utilisation RAM croissante

**Cause :** Ressources non libérées.

**Solution :**
1. Vérifier que `dispose()` est appelé sur toutes les entités
2. Vérifier que les textures sont libérées
3. Décharger les cartes avec `mapManager.unloadMap()`

### Problèmes de Configuration

#### La configuration JSON ne charge pas

**Cause :** Fichier mal formaté ou chemin incorrect.

**Solution :**
1. Valider JSON sur [jsonlint.com](https://www.jsonlint.com/)
2. Vérifier le chemin : `assets/config/player_config.json`
3. Vérifier la classe correspondante (ex: `PlayerConfig.java`)
4. Ajouter des logs pour déboguer :
```java
PlayerConfig config = configManager.loadConfig("player_config.json", PlayerConfig.class);
if (config == null) {
    System.err.println("Erreur de chargement de configuration!");
    config = new PlayerConfig(); // Valeurs par défaut
}
```

---

## Questions de Conception

### Q: Comment ajouter des ennemis?

**R:** 
1. Créer une classe `Enemy extends Entity`
2. Implémenter `IEnemyState` pour les comportements
3. Ajouter au monde : `world.addEntity(enemy)`
4. Implémenter la détection de collision avec le joueur

Voir `EXTENSION_GUIDE.md` pour un exemple complet.

### Q: Comment ajouter plusieurs niveaux?

**R:**
1. Créer les cartes dans Tiled
2. Exporter en `.tmx` dans `assets/maps/`
3. Créer un `LevelManager` :
```java
public class LevelManager {
    public static void loadNextLevel(String levelName) {
        // Charger nouvelle carte
        // Réinitialiser joueur
        // Afficher nouvel écran
    }
}
```
4. Appeler depuis GameScreen

### Q: Comment sauvegarder la progression?

**R:**
1. Créer une classe `SaveGame`
2. Sérialiser en JSON avec GSON
3. Sauvegarder dans un fichier
4. Charger au démarrage

### Q: Puis-je modifier les contrôles?

**R:** Oui! Dans `InputManager.java`, modifier `initializeDefaultMappings()` :
```java
keyMappings.put("MoveLeft", Input.Keys.LEFT);  // Flèche gauche
keyMappings.put("MoveRight", Input.Keys.RIGHT); // Flèche droite
keyMappings.put("Jump", Input.Keys.W);          // W pour sauter
```

---

## Ressources Utiles

### Documentation
- [LibGDX Wiki](https://libgdx.com/wiki/)
- [JavaDoc Gradle](https://docs.gradle.org/)
- [Google GSON Guide](https://www.baeldung.com/gson-deserialization-guide)

### Outils
- [Tiled Map Editor](https://www.mapeditor.org/)
- [JSON Validator](https://www.jsonlint.com/)
- [LibGDX Game Development](https://www.youtube.com/@gamefromscratch)

### Concepts
- [Design Patterns](https://refactoring.guru/design-patterns)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Game Development Loops](https://www.gamedev.net/)

---

## Commandes Utiles

### Gradle
```bash
# Compiler
gradle clean build

# Lancer
gradle run

# Construire un JAR
gradle jar

# Nettoyer les builds
gradle clean

# Afficher les tâches disponibles
gradle tasks
```

### Git
```bash
# Cloner
git clone <repo-url>

# Commit
git commit -m "Message"

# Push
git push origin main

# Pull
git pull origin main
```

### Java/Gradle Troubleshooting
```bash
# Vérifier la version Java
java -version

# Vérifier la version Gradle
gradle -v

# Vérifier les dépendances
gradle dependencies

# Reconstruire avec dépendances
gradle build --refresh-dependencies
```

---

## Contacter le Support

Pour les problèmes non résolus:
1. Consultez la documentation du code (JavaDoc)
2. Vérifiez la console d'erreurs (logs)
3. Relisez les fichiers de configuration
4. Consultez le README.md

---

## Changelog et Mises à Jour

### Version 1.0.0
- ✅ Architecture de base MVC
- ✅ Machine à états du joueur
- ✅ Gestion des cartes Tiled
- ✅ Configuration JSON
- ✅ Gestion des entrées

### Versions Futures
- ⏳ Système de sprites et animations
- ⏳ Ennemis avec IA
- ⏳ Système de sons
- ⏳ Menu de pause
- ⏳ Système de sauvegarde

---

Bonne chance avec votre projet! 🚀

