# LibGDX 2D Platformer Game Engine

**Dépôt Git :** (https://github.com/fsdsfr/moteur-jeu-2d-libgdx)

Moteur de jeu 2D (Java + LibGDX) avec niveaux Tiled, entités, menus et configuration JSON.

## Caractéristiques

### Gameplay & moteur
- Système d'entités (`IEntity` / `Entity`) et monde (`World`) qui gère collisions + entités.
- Joueur avec machine à états (`IPlayerState` + états Idle/Run/Jump/Fall + attaques).
- Entrées via `InputManager` (polling) + bindings dans `GameScreen`.
- Menus et UI (pause, settings, contrôles) via Scene2D.

### Support Tiled
- Chargement de cartes `.tmx` et tilesets `.tsx`.
- Collisions principalement calculées depuis une couche de tuiles (voir section Tiled).
- Placement d'entités via une couche d'objets `Entities` (goblins, portails, spawn).

### Modes de jeu
- Story Mode : niveaux chaînés via des objets `Portal` dans Tiled.
- Survival / Open World : logique côté `GameScreen` / `ProceduralLevelGenerator`.

## Prérequis

- Java 11+ (le projet cible Java 11, cf. `build.gradle`, et il est compatible avec des JDK plus récents comme Java 21).
  - Sur Windows, Gradle peut dépendre de `JAVA_HOME` : il doit pointer vers un JDK valide (`...\bin\java.exe`).
  - Recommandé : JDK 21.
- Gradle :
  - soit déjà installé et disponible dans le `PATH`,
  - soit téléchargé localement par `setup_and_run.ps1` (Windows, nécessite internet).

## Lancer le projet

### Windows (recommandé)

- Double-cliquez sur `run.bat`.
- Si Gradle n'est pas disponible, lancez `setup_and_run.ps1` (télécharge Gradle 8.5 dans `.gradle_local/`).
  - PowerShell :
    - `powershell -ExecutionPolicy Bypass -File setup_and_run.ps1`
- Si `run.bat` échoue avec une erreur liée à Java, vérifiez `JAVA_HOME` (ex: `setx JAVA_HOME "C:\Program Files\Java\jdk-21"`) et relancez.

### Linux / macOS

- `chmod +x run.sh`
- `./run.sh`

### Manuel (Gradle)

- Build : `gradle build`
- Run : `gradle run`


#### Windows (Gradle local du projet)

Si Gradle n'est pas installé globalement, vous pouvez aussi utiliser la distribution locale (si présente) :

- Build : `.\.gradle_local\gradle-8.5\bin\gradle.bat clean build`
- Run : `.\.gradle_local\gradle-8.5\bin\gradle.bat run`

## Structure du projet (résumé)

```
LibGDXGameEngine/
├── src/
│   └── com/gameengine/
│       ├── engine/
│       │   ├── core/        # Engine core (Entity, ConfigManager, AbstractScreen, ...)
│       │   ├── input/       # InputManager + actions
│       │   └── world/       # World, TiledMapManager, ProceduralLevelGenerator, ...
│       ├── entity/
│       │   ├── player/      # Player + states (Idle/Run/Jump/Fall/AttackLight/AttackHeavy)
│       │   ├── enemy/       # Enemy + Goblin (SCOUT/ARCHER/TANK)
│       │   ├── item/        # Pickups (ex: HeartPickup)
│       │   └── projectile/  # Projectiles (ex: Arrow)
│       └── game/            # Screens (MainMenuScreen, GameScreen, SettingsScreen, ...)
├── assets/
│   ├── config/              # JSON (player_config.json, level_config.json)
│   ├── maps/                # Tiled maps (.tmx)
│   ├── tilesets/            # Tilesets (.tsx + images)
│   ├── gfx/ music/ sounds/  # Ressources
├── build.gradle
└── README.md
```

## Configuration (JSON)

### Joueur

Fichier : `assets/config/player_config.json`

Exemple (valeurs actuelles du projet) :

```json
{
  "width": 20,
  "height": 60,
  "maxMovementSpeed": 300,
  "movementAcceleration": 1500,
  "gravityStrength": 1000,
  "maxFallSpeed": 600,
  "jumpForce": 450,
  "friction": 0.9
}
```

## Édition des niveaux (Tiled)

Le jeu charge les cartes depuis `assets/maps/`.

### Couches attendues

- Couche de tuiles `Level` : utilisée comme base pour les collisions (souvent cachée dans Tiled, `visible="0"`).
- Couche d'objets `Entities` : placement des entités.
  - Objets supportés (selon `World`) :
    - `Goblin` (propriété `type` = `SCOUT` | `ARCHER` | `TANK`, propriété optionnelle `hp`)
    - `Heart`
    - `SpawnPoint`
    - `Portal` (propriété `nextMap` = nom du `.tmx` cible)
    - `StartPortal`

Note : le code supporte aussi une couche d'objets nommée `Collision` (rectangles), mais les cartes fournies utilisent surtout la couche de tuiles `Level`.

## Contrôles

| Touche / Souris | Action |
|---|---|
| `A` | Aller à gauche |
| `D` | Aller à droite |
| `SPACE` | Saut |
| Clic gauche | Attaque légère |
| Clic droit | Attaque lourde |
| `J` | Attaque légère (clavier, backup) |
| `K` | Attaque lourde (clavier, backup) |
| `ESC` | Pause |

## Dépannage

### Erreur de chargement de carte

- Vérifier que le fichier `.tmx` est dans `assets/maps/`.
- Vérifier l'orthographe exacte dans la propriété `nextMap`.

### Le joueur traverse le sol

- Vérifier que la carte contient une couche de tuiles `Level`.
- Vérifier que les tuiles utilisées n'ont pas la propriété `no_collision`.

## Crédits & Assets

Certains assets graphiques et sonores proviennent de cours Udemy acquis légalement. D'autres effets sonores, musiques et ressources graphiques proviennent de sources en libre accès.

Ces assets sont utilisés strictement à des fins éducatives et de démonstration (projet universitaire), sans but commercial. Les licences d'origine doivent être respectées.

## Architecture (repères)

- `GameEngine` : singleton (gestion globale).
- `AbstractScreen` : template de base pour les écrans.
- `InputManager` : pattern Command (actions déclenchées par mapping).
- `Player` + `IPlayerState` : pattern State.
