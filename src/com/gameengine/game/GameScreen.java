package com.gameengine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gameengine.engine.core.AbstractScreen;
import com.gameengine.engine.core.ConfigManager;
import com.gameengine.engine.input.InputManager;
import com.gameengine.engine.world.TiledMapManager;
import com.gameengine.engine.world.ProceduralLevelGenerator;
import com.gameengine.engine.world.World;
import com.gameengine.entity.player.Player;
import com.gameengine.entity.player.PlayerConfig;
import com.gameengine.engine.world.LevelConfig;
import com.gameengine.entity.Portal; 
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gameengine.engine.core.Entity;
import com.gameengine.entity.enemy.Goblin;
import com.gameengine.engine.core.GameEngine;
import java.util.HashSet;
import java.util.Set;

public class GameScreen extends AbstractScreen {

    private World world;
    private Player player;
    private InputManager inputManager;
    private TiledMapManager mapManager;
    private ConfigManager configManager;
    private ProceduralLevelGenerator levelGenerator;
    private String currentMapName;
    private Portal portal;
    private TiledMap tiledMap;
    
    private Stage uiStage;
    private Skin skin;
    private boolean isPaused = false;
    private Table pauseMenuTable;
    
    private boolean isSurvivalMode = true; 
    private float survivalTimer = 0;
    private int survivalScore = 0; 
    private com.badlogic.gdx.scenes.scene2d.ui.Label timerLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Label scoreLabel; 
    private Set<Entity> processedDeadEntities = new HashSet<>();
    private boolean gameStarted = false;
    private Table rulesTable;
    
    private com.badlogic.gdx.audio.Sound swingSound;
    private com.badlogic.gdx.audio.Sound hitSound;
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    
    private com.badlogic.gdx.audio.Music currentMusic;
    private java.util.List<String> musicPlaylist;
    private int currentTrackIndex = 0;
    private boolean isMusicFadingOut = false;
    private boolean isMusicFadingIn = false;
    private float musicFadeTimer = 0;
    private float musicFadeDuration = 2.0f; 
    private float targetMusicVolume = 0;
    
    private OrthographicCamera gameCamera;

    private Viewport viewport;
    
    private Texture backgroundTexture;
    private OrthographicCamera bgCamera; 

    private Texture heartFull, heartEmpty;
    private Texture heart1, heart2, heart3; 
    private boolean isGameOver = false;
    private Table gameOverTable;

    public GameScreen(float screenWidth, float screenHeight) {
        this(screenWidth, screenHeight, true); 
    }

    public GameScreen(float screenWidth, float screenHeight, boolean isSurvival) {
        super(screenWidth, screenHeight);
        this.isSurvivalMode = isSurvival;
        this.mapManager = new TiledMapManager();
        this.configManager = new ConfigManager();
        this.inputManager = new InputManager();
        this.currentMapName = "GrassLandsFixed.tmx"; 
    }

    @Override
    public void create() {
        
        gameCamera = new OrthographicCamera();
        viewport = new FitViewport(1200, 720, gameCamera);
        viewport.apply();
        gameCamera.position.set(1200/2f, 720/2f, 0);

        bgCamera = new OrthographicCamera();
        bgCamera.setToOrtho(false, 1200, 720);

        try {
            if (Gdx.files.internal("gfx/hp/life_04.png").exists()) {
                heartFull = new Texture(Gdx.files.internal("gfx/hp/life_04.png"));
                heartFull.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            if (Gdx.files.internal("gfx/hp/life_00.png").exists()) {
                heartEmpty = new Texture(Gdx.files.internal("gfx/hp/life_00.png"));
                heartEmpty.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            
            if (Gdx.files.internal("gfx/hp/life_01.png").exists()) {
                heart1 = new Texture(Gdx.files.internal("gfx/hp/life_01.png"));
                heart1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            if (Gdx.files.internal("gfx/hp/life_02.png").exists()) {
                heart2 = new Texture(Gdx.files.internal("gfx/hp/life_02.png"));
                heart2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            if (Gdx.files.internal("gfx/hp/life_03.png").exists()) {
                heart3 = new Texture(Gdx.files.internal("gfx/hp/life_03.png"));
                heart3.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
        try {
            if (Gdx.files.internal("sounds/swing.wav").exists()) {
                swingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/swing.wav"));
            }
        } catch (Exception e) { System.out.println("Error loading swing.wav: " + e.getMessage()); }

        try {
            if (Gdx.files.internal("sounds/player_long_hit.wav").exists()) {
                hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/player_long_hit.wav"));
            } else if (Gdx.files.internal("sounds/sword_hit.wav").exists()) {
                 hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/sword_hit.wav"));
            }
        } catch (Exception e) {
             System.out.println("Error loading player_long_hit.wav (Likely format issue). Falling back to sword_hit.wav");
             try {
                if (Gdx.files.internal("sounds/sword_hit.wav").exists()) {
                    hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/sword_hit.wav"));
                }
             } catch (Exception ex) { ex.printStackTrace(); }
        }
        
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("settings_prefs");
        masterVolume = prefs.getFloat("volume", 1.0f);
        musicVolume = prefs.getFloat("music_volume", 1.0f);
        sfxVolume = prefs.getFloat("sfx_volume", 1.0f);
        
        uiStage = new Stage(new ScreenViewport());
        createUISkin();
        createUI();
        
        musicPlaylist = new java.util.ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String track = "music/Pixel " + i + ".mp3";
            if (Gdx.files.internal(track).exists()) {
                musicPlaylist.add(track);
            }
        }
        
        playNextMusicTrack();
        
        Gdx.input.setInputProcessor(uiStage);

        showRulesOverlay();

        PlayerConfig playerConfig = configManager.loadConfig("player_config.json", PlayerConfig.class);
        if (playerConfig == null) playerConfig = new PlayerConfig();

        LevelConfig levelConfig = configManager.loadConfig("level_config.json", LevelConfig.class);
        float startX = 100;
        float startY = 300; 
        
        try {
            String[] bgOptions = {
                "gfx/background/Forest_layer5.png",
                "gfx/background/Forest_layer6.png"
            };
            
            int randomIndex = (int)(Math.random() * bgOptions.length);
            String selectedBg = bgOptions[randomIndex];
            
            if (Gdx.files.internal(selectedBg).exists()) {
                backgroundTexture = new Texture(Gdx.files.internal(selectedBg));
                
            }
        } catch (Exception e) {
             System.out.println("Background loading warning: " + e.getMessage());
        }

        if (isSurvivalMode) {
             
             levelGenerator = new ProceduralLevelGenerator();
             levelGenerator.reset();
             
             tiledMap = levelGenerator.generate(50, 40); 
        } else {
             
             currentMapName = "GrassLandsFixed";
             tiledMap = mapManager.loadMap(currentMapName);
             gameStarted = true; 
        }

        world = new World(tiledMap, 32, 32, batch, isSurvivalMode);
        
        if (world.getSpawnPoint() != null) {
            startX = world.getSpawnPoint().x;
            startY = world.getSpawnPoint().y;
            Gdx.app.log("GameScreen", "Using SpawnPoint from World: " + startX + ", " + startY);
        } else {
             
             if (levelConfig != null) {
                 startX = levelConfig.getPlayerStartX();
                 startY = levelConfig.getPlayerStartY();
             } else {
                 startX = 100; 
                 startY = 300;
             }
             Gdx.app.log("GameScreen", "Using Fallback Spawn: " + startX + ", " + startY);
        }
        
        if (!isSurvivalMode) {
             int endX = world.getWorldEndX();
             
             world.getCollisionRectangles().add(new Rectangle(endX, 0, 20, 2000));
        }

        startY = world.getSafeSpawnY(startX, startY, playerConfig.getWidth(), playerConfig.getHeight());

        player = new Player(startX, startY, batch, playerConfig, world);
        world.addEntity(player);
        world.setPlayer(player);

        setupInputs();
    }
    
    private void setupInputs() {
        inputManager.mapKeyToAction("MoveLeft", com.badlogic.gdx.Input.Keys.A);
        inputManager.mapKeyToAction("MoveRight", com.badlogic.gdx.Input.Keys.D);
        inputManager.mapKeyToAction("Jump", com.badlogic.gdx.Input.Keys.SPACE);
        
        inputManager.mapKeyToAction("AttackLight", com.badlogic.gdx.Input.Keys.J); 
        inputManager.mapKeyToAction("AttackHeavy", com.badlogic.gdx.Input.Keys.K);
    }

    @Override
    public void update(float delta) {
        inputManager.update();
        
        uiStage.act(delta);
        
        if (isPaused) return; 
        
        if (delta > 0.1f) delta = 0.1f;
        
        if (gameStarted && !isGameOver) {
            survivalTimer += delta;
            updateTimerLabel();
            checkEnemyKills();
        }
        
        updateMusic(delta);
        
        world.update(delta);
        
        if (isSurvivalMode && player.getPosition().x > world.getWorldEndX() - 1200) { 
             
             TiledMap newChunk = levelGenerator.generate(50, 40); 
             world.addChunk(newChunk, world.getWorldEndX());
             
        }
        
        float leftStrength = inputManager.isActionPressed("MoveLeft") ? 1.0f : 0.0f;
        float rightStrength = inputManager.isActionPressed("MoveRight") ? 1.0f : 0.0f;
        float direction = rightStrength - leftStrength;
        player.setDirectionX(direction);

        if (inputManager.isActionJustPressed("Jump")) {
            player.jump();
        }
        
        boolean hitUI = false;
        if (Gdx.input.isTouched()) {
            Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            uiStage.screenToStageCoordinates(touchPos);
            if (uiStage.hit(touchPos.x, touchPos.y, true) != null) {
                hitUI = true;
            }
        }

        if (!hitUI) {
            if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                if (!player.isAttacking() && !player.isDead()) {
                    if (swingSound != null) swingSound.play(masterVolume * sfxVolume);
                    player.performLightAttack();
                }
            }
            if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.RIGHT)) {
                if (!player.isAttacking() && !player.isDead()) {
                    if (hitSound != null) hitSound.play(masterVolume * sfxVolume);
                    player.performHeavyAttack();
                }
            }
        }
        
        if (inputManager.isActionJustPressed("AttackLight")) {
            if (!player.isAttacking() && !player.isDead()) {
                if (swingSound != null) swingSound.play(masterVolume * sfxVolume);
                player.performLightAttack();
            }
        }
        if (inputManager.isActionJustPressed("AttackHeavy")) {
             if (!player.isAttacking() && !player.isDead()) {
                 if (hitSound != null) hitSound.play(masterVolume * sfxVolume);
                 player.performHeavyAttack();
             }
        }

        if (player.getPosition().y < 0) {
            
        }

        Vector2 playerCenter = new Vector2(player.getPosition().x + player.getSize().x / 2, player.getPosition().y + player.getSize().y / 2);
        
        Vector2 camPos = new Vector2(playerCenter.x, playerCenter.y);
        
        float halfViewH = viewport.getWorldHeight() / 2;
        if (camPos.y < halfViewH) camPos.y = halfViewH;
        
        float halfViewW = viewport.getWorldWidth() / 2;
        float tilePadding = 64f; 

        if (camPos.x < halfViewW + tilePadding) camPos.x = halfViewW + tilePadding;
        
        float mapWidth = world.getWorldEndX();
        if (mapWidth > viewport.getWorldWidth()) { 
             if (camPos.x > mapWidth - halfViewW - tilePadding) {
                 camPos.x = mapWidth - halfViewW - tilePadding;
             }
        } else {
             
             camPos.x = mapWidth / 2f;
        }
        
        gameCamera.position.set(Math.round(camPos.x), Math.round(camPos.y), 0);
        gameCamera.update();
    }

    @Override
    public void render() {
        
        float clearR, clearG, clearB;
        float tintR, tintG, tintB;
        
        if (survivalTimer < 60) {
            
            clearR = 0.2f; clearG = 0.25f; clearB = 0.35f;
            tintR = 0.8f; tintG = 0.8f; tintB = 0.85f; 
        } else if (survivalTimer < 120) {
            
            clearR = 0.25f; clearG = 0.15f; clearB = 0.1f;
            tintR = 0.85f; tintG = 0.65f; tintB = 0.55f;
        } else {
            
            clearR = 0.05f; clearG = 0.05f; clearB = 0.12f;
            tintR = 0.35f; tintG = 0.35f; tintB = 0.55f;
        }

        Gdx.gl.glClearColor(clearR, clearG, clearB, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        bgCamera.update();
        batch.setProjectionMatrix(bgCamera.combined);
        batch.begin();
        if (backgroundTexture != null) {
            
            batch.setColor(tintR, tintG, tintB, 1f); 
            batch.draw(backgroundTexture, 0, 0, 1200, 720);
            batch.setColor(Color.WHITE); 
        }
        batch.end();
        
        world.render(gameCamera, batch);

        bgCamera.update();
        batch.setProjectionMatrix(bgCamera.combined);
        batch.begin();
        
        if (heartFull != null && heartEmpty != null) {
            float startX = 20;
            float startY = 720 - 20 - 48; 
            float size = 48; 
            
            int hp = 0;
            if (player != null) hp = player.getHealth();
            if (hp < 0) hp = 0;
            
            for (int i = 0; i < 3; i++) {
                int heartHP = hp - (i * 4);
                if (heartHP > 4) heartHP = 4;
                if (heartHP < 0) heartHP = 0;
                
                Texture toDraw = heartEmpty;
                if (heartHP == 4) toDraw = heartFull;
                else if (heartHP == 3) toDraw = heart3 != null ? heart3 : heartFull;
                else if (heartHP == 2) toDraw = heart2 != null ? heart2 : heartEmpty;
                else if (heartHP == 1) toDraw = heart1 != null ? heart1 : heartEmpty;
                
                batch.draw(toDraw, startX + (i * (size + 5)), startY, size, size);
            }
        }
        
        if (!isSurvivalMode && skin != null && !isGameOver) {
            
            BitmapFont font = skin.getFont("medium"); 
            if (font == null) font = skin.getFont("default");
            
            int m = (int)survivalTimer / 60;
            int s = (int)survivalTimer % 60;
            String timeText = String.format("TIME: %02d:%02d", m, s);
            
            GlyphLayout layout = new GlyphLayout(font, timeText);
            float fontX = (1200 - layout.width) / 2;
            float fontY = 720 - 20;

            if (survivalTimer <= 60) font.setColor(Color.GOLD);
            else if (survivalTimer <= 70) font.setColor(Color.CYAN); 
            else font.setColor(Color.WHITE);
            
            font.setColor(0, 0, 0, 0.6f);
            font.draw(batch, timeText, fontX + 2, fontY - 2);
            
            if (survivalTimer <= 60) font.setColor(Color.GOLD);
            else if (survivalTimer <= 70) font.setColor(Color.CYAN); 
            else font.setColor(Color.WHITE);
            font.draw(batch, timeText, fontX, fontY);

            int enemiesLeft = world.getEnemyCount();
            String enemyText = "ENEMIES: " + enemiesLeft; 
            if (enemiesLeft == 0) enemyText = "AREA CLEAR";
            
            GlyphLayout enemyLayout = new GlyphLayout(font, enemyText);
            
            float enemyX = 1130 - enemyLayout.width - 30; 
            float enemyY = 720 - 20;

            font.setColor(0, 0, 0, 0.6f);
            font.draw(batch, enemyText, enemyX + 2, enemyY - 2);
            
            font.setColor(enemiesLeft == 0 ? Color.GREEN : new Color(1f, 0.3f, 0.3f, 1f)); 
            font.draw(batch, enemyText, enemyX, enemyY);
            
            font.setColor(Color.WHITE); 
        }

        batch.end();

        if (!isSurvivalMode && !isGameOver && survivalTimer > 1.0f && world.getEnemyCount() == 0) {
             
             String simpleName = currentMapName.replace(".tmx", "");
             boolean hasNextLevel = "GrassLandsFixed".equals(simpleName) || "GrassLandsFixed_part2".equals(simpleName);
             
             if (world.getExitPortal() != null) {
                 
                 if (!world.getExitPortal().isActive()) {
                     world.getExitPortal().setActive(true);
                 }
                 
                 if (world.getExitPortal().isActive() && world.getExitPortal().getBounds().overlaps(player.getBounds())) {
                     if (hasNextLevel) {
                         loadNextLevel();
                     } else {
                         showVictoryMenu();
                     }
                 }
             } else {
                 
                 if (hasNextLevel) {
                     
                     loadNextLevel(); 
                 } else {
                     
                     showVictoryMenu();
                 }
             }
        }

        if (!isGameOver) { 
             
             if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                 togglePauseMenu();
             }
             
             if (player != null && !player.isDead()) {
                 
                 if (player.getPosition().y < -100) { 
                     player.takeDamage(999); 
                 }
             }
             
             if (player != null && player.isDead() && !isGameOver) {
                 showGameOverMenu();
             }
        }
        
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        viewport.update(width, height);
        
        uiStage.getViewport().update(width, height, true);
        
        if (gearButton != null) {
            gearButton.setPosition(width - 70, height - 70);
        }
    }

    @Override
    public void dispose() {
        world.dispose();
        batch.dispose();
        mapManager.dispose();
        if(backgroundTexture != null) backgroundTexture.dispose();
        if(heartFull != null) heartFull.dispose();
        if(heartEmpty != null) heartEmpty.dispose();
        if(swingSound != null) swingSound.dispose();
        if(hitSound != null) hitSound.dispose();
        if(currentMusic != null) currentMusic.dispose();
        
        if (uiStage != null) uiStage.dispose();
        if (skin != null) skin.dispose();
    }
    
    private void createUISkin() {
        skin = new Skin();
        
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        int size = 64;
        Pixmap gearMap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        gearMap.setColor(new Color(0f, 0f, 0f, 0f)); 
        gearMap.fill();
        
        gearMap.setColor(new Color(0.9f, 0.9f, 0.9f, 1f)); 
        
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int tx = (int)(size/2 + Math.cos(angle) * (size/2 - 2));
            int ty = (int)(size/2 + Math.sin(angle) * (size/2 - 2));
            gearMap.fillCircle(tx, ty, 6);
        }
        
        gearMap.fillCircle(size/2, size/2, size/2 - 6);
        
        gearMap.setBlending(Pixmap.Blending.None);
        gearMap.setColor(new Color(0f, 0f, 0f, 0f)); 
        gearMap.fillCircle(size/2, size/2, size/4 + 2);
        
        Texture gearTexture = new Texture(gearMap);
        gearTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add("gear", gearTexture);
        gearMap.dispose(); 

        com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator = null;
        try {
            
            if (Gdx.files.absolute("C:/Windows/Fonts/arialbd.ttf").exists()) {
                 generator = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(Gdx.files.absolute("C:/Windows/Fonts/arialbd.ttf"));
            } else if (Gdx.files.internal("arial.ttf").exists()) {
                 generator = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(Gdx.files.internal("arial.ttf")); 
            }
        } catch(Throwable e) { 
            System.out.println("Font loading failed (using default): " + e.getMessage()); 
        }

        BitmapFont font;
        BitmapFont titleFont;
        BitmapFont hugeFont; 
        
        if (generator != null) {
            try {
                com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 24;
                parameter.borderWidth = 1;
                parameter.color = Color.WHITE;
                parameter.borderColor = new Color(0,0,0,0.5f);
                font = generator.generateFont(parameter);
                
                parameter.size = 32;
                parameter.borderWidth = 1;
                BitmapFont mediumFont = generator.generateFont(parameter);
                mediumFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                skin.add("medium", mediumFont);
                
                parameter.size = 48;
                parameter.borderWidth = 2;
                titleFont = generator.generateFont(parameter);
                
                parameter.size = 96; 
                parameter.borderWidth = 4;
                hugeFont = generator.generateFont(parameter);
            } catch (Exception e) {
                font = new BitmapFont();
                titleFont = new BitmapFont();
                titleFont.getData().setScale(2f);
                hugeFont = new BitmapFont();
                hugeFont.getData().setScale(4f);
                skin.add("medium", font); 
            } finally {
                generator.dispose();
            }
        } else {
            font = new BitmapFont();
            titleFont = new BitmapFont();
            titleFont.getData().setScale(2f);
            hugeFont = new BitmapFont();
            hugeFont.getData().setScale(4f);
            skin.add("medium", font);
        }
        
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        hugeFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin.add("default", font);
        skin.add("title", titleFont);
        skin.add("huge", hugeFont);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0.2f, 0.4f, 0.2f, 1f));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.15f, 0.3f, 0.15f, 1f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.25f, 0.5f, 0.25f, 1f));
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);
        
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.background = skin.newDrawable("white", new Color(0.1f, 0.15f, 0.1f, 0.95f)); 
        ws.titleFont = titleFont;
        ws.titleFontColor = new Color(0.9f, 0.9f, 0.7f, 1f);
        skin.add("default", ws);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle ls = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        ls.font = font;
        ls.fontColor = Color.WHITE;
        skin.add("default", ls);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle titleLs = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        titleLs.font = titleFont;
        titleLs.fontColor = Color.WHITE;
        skin.add("title", titleLs);
        
        com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle sliderStyle = new com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        sliderStyle.background.setMinHeight(10);
        sliderStyle.knob = skin.newDrawable("white", Color.GOLD);
        sliderStyle.knob.setMinWidth(20);
        sliderStyle.knob.setMinHeight(20);
        skin.add("default-horizontal", sliderStyle);
    }

    private com.badlogic.gdx.scenes.scene2d.ui.Button gearButton; 

    private void createUI() {
        
        com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle gearStyle = new com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle();
        gearStyle.up = skin.getDrawable("gear");
        gearStyle.down = skin.newDrawable("gear", Color.GRAY);
        gearStyle.over = skin.newDrawable("gear", Color.LIGHT_GRAY);
        
        gearButton = new com.badlogic.gdx.scenes.scene2d.ui.Button(gearStyle);
        gearButton.setPosition(1200 - 70, 720 - 70); 
        gearButton.setSize(50, 50);
        
        gearButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePauseMenu();
            }
        });
        
        uiStage.addActor(gearButton);

        if (isSurvivalMode) {
             com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("title"), Color.GOLD);
             timerLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("00:00", labelStyle);
             
             scoreLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("SCORE: 0", skin, "title");
             scoreLabel.setColor(Color.CYAN);
             
             Table topTable = new Table();
             topTable.setFillParent(true);
             topTable.top();
             topTable.add(timerLabel).padTop(20).row();
             topTable.add(scoreLabel).padTop(5).row();
             uiStage.addActor(topTable);
        }
    }
    
    private void showRulesOverlay() {
        isPaused = true;
        gameStarted = false;
        
        rulesTable = new Table();
        rulesTable.setFillParent(true);
        rulesTable.setBackground(skin.newDrawable("white", new Color(0,0,0,0.8f)));
        
        Table box = new Table();
        box.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.2f, 1f)));
        
        com.badlogic.gdx.scenes.scene2d.ui.Label title;
        com.badlogic.gdx.scenes.scene2d.ui.Label rules1;
        com.badlogic.gdx.scenes.scene2d.ui.Label rules2; 
        
        com.badlogic.gdx.scenes.scene2d.ui.Label b1;
        com.badlogic.gdx.scenes.scene2d.ui.Label b2;
        com.badlogic.gdx.scenes.scene2d.ui.Label b3;
        com.badlogic.gdx.scenes.scene2d.ui.Label b1_s = null;

        if (isSurvivalMode) {
             title = new com.badlogic.gdx.scenes.scene2d.ui.Label("SURVIVAL MODE", skin, "title");
             title.setColor(Color.GOLD);
             
             rules1 = new com.badlogic.gdx.scenes.scene2d.ui.Label("Goal: Survive & Get High Score!", skin);
             rules2 = new com.badlogic.gdx.scenes.scene2d.ui.Label("Scoring:", skin);
             rules2.setColor(Color.YELLOW);
             
             b1 = new com.badlogic.gdx.scenes.scene2d.ui.Label("- Goblin Archer: +20 pts", skin);
             b2 = new com.badlogic.gdx.scenes.scene2d.ui.Label("- Goblin Scout:  +30 pts", skin);
             b3 = new com.badlogic.gdx.scenes.scene2d.ui.Label("- Goblin Tank:   +50 pts", skin);
        } else {
             int currentLevel = levelRecords.size() + 1;
             String modeTitle = "LEVEL " + currentLevel;
             
             int bonus = 0;
             if (currentLevel == 2) bonus = 5;
             if (currentLevel == 3) bonus = 10;
             
             title = new com.badlogic.gdx.scenes.scene2d.ui.Label(modeTitle, skin, "title");
             title.setColor(Color.GOLD);
             
             rules1 = new com.badlogic.gdx.scenes.scene2d.ui.Label("Goal: Kill all enemies on the map!", skin);
             rules2 = new com.badlogic.gdx.scenes.scene2d.ui.Label("Rankings:", skin);
             rules2.setColor(Color.YELLOW);
             
             b1 = new com.badlogic.gdx.scenes.scene2d.ui.Label("S+ Rank: < " + (40 + bonus) + " sec", skin);
             b1_s = new com.badlogic.gdx.scenes.scene2d.ui.Label("S  Rank: < " + (50 + bonus) + " sec", skin);
             b2 = new com.badlogic.gdx.scenes.scene2d.ui.Label("A  Rank: < " + (60 + bonus) + " sec", skin);
             b3 = new com.badlogic.gdx.scenes.scene2d.ui.Label("B  Rank: > " + (60 + bonus) + " sec", skin);
        }
        
        TextButton startBtn = new TextButton("START GAME", skin);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                gameStarted = true;
                rulesTable.remove();
            }
        });
        
        box.add(title).pad(20).row();
        box.add(rules1).pad(10).row();
        box.add(rules2).padTop(20).padBottom(10).row();
        box.add(b1).left().padLeft(40).row();
        if (!isSurvivalMode && b1_s != null) box.add(b1_s).left().padLeft(40).row();
        box.add(b2).left().padLeft(40).row();
        box.add(b3).left().padLeft(40).padBottom(30).row();
        box.add(startBtn).width(200).height(60).pad(20).row();
        
        rulesTable.add(box).width(600);
        uiStage.addActor(rulesTable);
    }
    
    private void showGameOverMenu() {
        isGameOver = true;
        
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("survival_prefs");
        
        String bestTimeKey = isSurvivalMode ? "best_time_survival" : "best_time_tutorial";
        float oldBestTime = isSurvivalMode ? prefs.getFloat(bestTimeKey, 0f) : prefs.getFloat(bestTimeKey, 99999f);
        
        boolean isNewRecord = false;
        float bestTime = oldBestTime;
        
        if (isSurvivalMode) {
             
             if (survivalTimer > oldBestTime) {
                isNewRecord = true;
                bestTime = survivalTimer;
                prefs.putFloat(bestTimeKey, bestTime);
             }
             
             int oldBestScore = prefs.getInteger("best_score_survival", 0);
             if (survivalScore > oldBestScore) {
                 if (!isNewRecord) isNewRecord = true; 
                 prefs.putInteger("best_score_survival", survivalScore);
             }
             prefs.flush();
        } else {
             
             if (bestTime == 99999f) bestTime = 0; 
        }

        gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0, 0, 0.7f))); 

        Table box = new Table();
        box.setBackground(skin.newDrawable("white", new Color(0.1f, 0.05f, 0.05f, 1f))); 
        
        com.badlogic.gdx.scenes.scene2d.ui.Label title = new com.badlogic.gdx.scenes.scene2d.ui.Label("YOU DIED", new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("title"), Color.RED));
        
        int m = (int)survivalTimer / 60;
        int s = (int)survivalTimer % 60;
        com.badlogic.gdx.scenes.scene2d.ui.Label timeLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label(String.format("Time Survived: %02d:%02d", m, s), skin);
        timeLbl.setColor(Color.WHITE);
        
        int bm = (int)bestTime / 60;
        int bs = (int)bestTime % 60;
        String bestLabelText = isSurvivalMode ? "Best Survival: " : "Best Run: ";
        com.badlogic.gdx.scenes.scene2d.ui.Label bestLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label(String.format(bestLabelText + "%02d:%02d", bm, bs), skin);
        bestLbl.setColor(Color.GOLD);
        
        box.add(title).pad(20).padBottom(20).row();
        
        if (isNewRecord) {
             com.badlogic.gdx.scenes.scene2d.ui.Label newRecordLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("NEW RECORD!", new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("title"), Color.CYAN));
             newRecordLbl.setOrigin(com.badlogic.gdx.utils.Align.center);
             newRecordLbl.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                 com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                     com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.1f, 1.1f, 0.4f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.color(Color.CYAN, 0.4f)
                     ),
                     com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(0.9f, 0.9f, 0.4f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.color(Color.GOLD, 0.4f)
                     )
                 )
             ));
             box.add(newRecordLbl).padBottom(15).row();
        }
        
        box.add(timeLbl).pad(5).row();
        box.add(bestLbl).pad(5).padBottom(10).row();
        
        if (isSurvivalMode) {
            com.badlogic.gdx.scenes.scene2d.ui.Label scoreL = new com.badlogic.gdx.scenes.scene2d.ui.Label("Final Score: " + survivalScore, new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("title"), Color.WHITE));
            int bestScoreVal = prefs.getInteger("best_score_survival", 0);
            com.badlogic.gdx.scenes.scene2d.ui.Label bestScoreL = new com.badlogic.gdx.scenes.scene2d.ui.Label("Best Score: " + bestScoreVal, skin);
            bestScoreL.setColor(Color.GOLD);
            
            box.add(scoreL).padTop(10).row();
            box.add(bestScoreL).padBottom(20).row();
        }

        com.badlogic.gdx.scenes.scene2d.ui.Label killsTitle = new com.badlogic.gdx.scenes.scene2d.ui.Label("Enemies Defeated:", skin);
        killsTitle.setColor(Color.GRAY);
        
        String s1 = isSurvivalMode ? "  Scout (+30): " : "  Scout: ";
        String s2 = isSurvivalMode ? "  Archer (+20): " : "  Archer: ";
        String s3 = isSurvivalMode ? "  Tank (+50): " : "  Tank: ";

        com.badlogic.gdx.scenes.scene2d.ui.Label k1 = new com.badlogic.gdx.scenes.scene2d.ui.Label(s1 + killedScouts, skin);
        com.badlogic.gdx.scenes.scene2d.ui.Label k2 = new com.badlogic.gdx.scenes.scene2d.ui.Label(s2 + killedArchers, skin);
        com.badlogic.gdx.scenes.scene2d.ui.Label k3 = new com.badlogic.gdx.scenes.scene2d.ui.Label(s3 + killedTanks, skin);
        
        com.badlogic.gdx.scenes.scene2d.ui.TextButton restartBtn = new com.badlogic.gdx.scenes.scene2d.ui.TextButton("TRY AGAIN", skin);
        restartBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                com.gameengine.engine.core.GameEngine.getInstance().setScreen(new GameScreen(screenWidth, screenHeight, isSurvivalMode));
            }
        });
        
        com.badlogic.gdx.scenes.scene2d.ui.TextButton exitBtn = new com.badlogic.gdx.scenes.scene2d.ui.TextButton("MAIN MENU", skin);
        exitBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                com.gameengine.engine.core.GameEngine.getInstance().setScreen(new MainMenuScreen(screenWidth, screenHeight));
            }
        });
        
        box.add(restartBtn).width(250).height(60).pad(10).row();
        box.add(exitBtn).width(250).height(60).pad(10).row();
        box.pad(50);
        
        gameOverTable.add(box).width(500);
        uiStage.addActor(gameOverTable);
    }

    private void updateTimerLabel() {
        if (timerLabel == null) return;
        
        int minutes = (int)survivalTimer / 60;
        int seconds = (int)survivalTimer % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private int killedArchers = 0;
    private int killedScouts = 0;
    private int killedTanks = 0;

    private void checkEnemyKills() {
        for (Entity e : world.getEntities()) {
            if (e instanceof Goblin) {
                if (((Goblin)e).isDead() && !processedDeadEntities.contains(e)) {
                    processedDeadEntities.add(e);
                    
                    Goblin.Type type = ((Goblin)e).getType();
                    float bonus = 0;
                    int points = 0;
                    
                    if (type == Goblin.Type.ARCHER) { 
                        points = 20; 
                        killedArchers++; 
                    }
                    else if (type == Goblin.Type.SCOUT) { 
                        points = 30; 
                        killedScouts++; 
                    }
                    else if (type == Goblin.Type.TANK) { 
                        points = 50; 
                        killedTanks++; 
                    }
                    
                    if (isSurvivalMode) {
                        survivalScore += points;
                        
                        showBonusPopup(points, e.getPosition());
                        
                        if (scoreLabel != null) {
                            scoreLabel.setText("SCORE: " + survivalScore);
                        }
                    }
                }
            }
        }
    }
    
    private void showBonusPopup(float value, Vector2 pos) {
        String tx = "+" + (int)value + (isSurvivalMode ? "" : "s"); 
        com.badlogic.gdx.scenes.scene2d.ui.Label popup = new com.badlogic.gdx.scenes.scene2d.ui.Label(tx, skin);
        popup.setColor(Color.GREEN);
        popup.setFontScale(1.5f);
        
        Vector2 screenPos = viewport.project(pos.cpy().add(0, 50)); 
        
        popup.setPosition(screenPos.x, screenPos.y);
        
        popup.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, 50, 1.0f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.5f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()
        ));
        
        uiStage.addActor(popup);
    }

    private void showSettingsWindow() {
        final Window win = new Window("SETTINGS", skin);
        win.setModal(true);
        win.setMovable(false);
        win.setSize(500, 450); 
        win.setPosition(uiStage.getWidth()/2 - 250, uiStage.getHeight()/2 - 225);
        
        Table content = new Table();
        
        final com.badlogic.gdx.scenes.scene2d.ui.Label masterLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("Master Volume: " + (int)(masterVolume * 100) + "%", skin);
        final com.badlogic.gdx.scenes.scene2d.ui.Slider masterSlider = new com.badlogic.gdx.scenes.scene2d.ui.Slider(0f, 1f, 0.05f, false, skin);
        masterSlider.setValue(masterVolume);
        
        final com.badlogic.gdx.scenes.scene2d.ui.Label musicLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("Music Volume: " + (int)(musicVolume * 100) + "%", skin);
        final com.badlogic.gdx.scenes.scene2d.ui.Slider musicSlider = new com.badlogic.gdx.scenes.scene2d.ui.Slider(0f, 1f, 0.05f, false, skin);
        musicSlider.setValue(musicVolume);
        
        final com.badlogic.gdx.scenes.scene2d.ui.Label sfxLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("SFX Volume: " + (int)(sfxVolume * 100) + "%", skin);
        final com.badlogic.gdx.scenes.scene2d.ui.Slider sfxSlider = new com.badlogic.gdx.scenes.scene2d.ui.Slider(0f, 1f, 0.05f, false, skin);
        sfxSlider.setValue(sfxVolume);
        
        com.badlogic.gdx.scenes.scene2d.utils.ChangeListener volListener = new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                masterVolume = masterSlider.getValue();
                musicVolume = musicSlider.getValue();
                sfxVolume = sfxSlider.getValue();
                
                masterLbl.setText("Master Volume: " + (int)(masterVolume * 100) + "%");
                musicLbl.setText("Music Volume: " + (int)(musicVolume * 100) + "%");
                sfxLbl.setText("SFX Volume: " + (int)(sfxVolume * 100) + "%");
                
                if (currentMusic != null) {
                    currentMusic.setVolume(masterVolume * musicVolume);
                }
                
                com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("settings_prefs");
                prefs.putFloat("volume", masterVolume);
                prefs.putFloat("music_volume", musicVolume);
                prefs.putFloat("sfx_volume", sfxVolume);
                prefs.flush();
            }
        };
        
        masterSlider.addListener(volListener);
        musicSlider.addListener(volListener);
        sfxSlider.addListener(volListener);
        
        TextButton closeBtn = new TextButton("CLOSE", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                win.remove();
            }
        });
        
        content.add(masterLbl).padTop(10).row();
        content.add(masterSlider).width(300).padBottom(20).row();
        
        content.add(sfxLbl).row();
        content.add(sfxSlider).width(300).padBottom(20).row();
        
        content.add(musicLbl).row();
        content.add(musicSlider).width(300).padBottom(20).row();
        
        content.add(closeBtn).width(150).height(50).padTop(20);
        
        win.add(content);
        uiStage.addActor(win);
    }
    
    private void showControlsWindow() {
        Window.WindowStyle scrollStyle = new Window.WindowStyle();
        scrollStyle.titleFont = skin.getFont("title");
        
        scrollStyle.background = skin.newDrawable("white", new Color(0.88f, 0.82f, 0.72f, 1f)); 
        scrollStyle.titleFontColor = new Color(0.3f, 0.2f, 0.1f, 1f);
        
        final Window win = new Window("", scrollStyle);
        win.setModal(true);
        win.setMovable(true);
        win.setSize(600, 550); 
        win.setPosition((uiStage.getWidth() - 600) / 2, (uiStage.getHeight() - 550) / 2);
        
        Table content = new Table();
        content.pad(20);
        
        Label.LabelStyle paperTitleStyle = new Label.LabelStyle(skin.getFont("title"), new Color(0.4f, 0.1f, 0.1f, 1f)); 
        Label title = new Label("CONTROLS", paperTitleStyle);
        
        Label.LabelStyle inkStyle = new Label.LabelStyle(skin.getFont("medium"), new Color(0.1f, 0.1f, 0.1f, 1f));
        
        addControlRow(content, inkStyle, "Move Left", "A");
        addControlRow(content, inkStyle, "Move Right", "D");
        addControlRow(content, inkStyle, "Jump", "Space");
        addControlRow(content, inkStyle, "Light Attack", "Left Mouse Button");
        addControlRow(content, inkStyle, "Heavy Attack", "Right Mouse Button");
        addControlRow(content, inkStyle, "Pause", "ESC");
        
        TextButton.TextButtonStyle brownBtnStyle = new TextButton.TextButtonStyle();
        brownBtnStyle.font = skin.getFont("default");
        brownBtnStyle.up = skin.newDrawable("white", new Color(0.4f, 0.3f, 0.2f, 1f));
        brownBtnStyle.down = skin.newDrawable("white", new Color(0.3f, 0.2f, 0.1f, 1f));
        brownBtnStyle.over = skin.newDrawable("white", new Color(0.5f, 0.4f, 0.3f, 1f));
        
        TextButton closeBtn = new TextButton("CLOSE", brownBtnStyle);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                win.remove();
            }
        });

        win.add(title).padBottom(30).row();
        win.add(content).growX().row(); 
        win.add(closeBtn).width(150).height(50).padBottom(20);
        
        uiStage.addActor(win);
    }
    
    private void addControlRow(Table t, Label.LabelStyle style, String action, String key) {
        Label actionLbl = new Label(action, style); 
        
        Label keyLbl = new Label(key, style);
        keyLbl.setColor(new Color(0.2f, 0.2f, 0.4f, 1f)); 
        
        t.add(actionLbl).left().expandX(); 
        t.add(keyLbl).right().expandX().row(); 
        
        com.badlogic.gdx.scenes.scene2d.ui.Image sep = new com.badlogic.gdx.scenes.scene2d.ui.Image(skin.newDrawable("white", new Color(0,0,0,0.1f)));
        t.add(sep).height(2).fillX().colspan(2).padTop(10).padBottom(10).row();
    }

    private java.util.ArrayList<String> levelRecords = new java.util.ArrayList<>();

    private void recordLevelStats(String mapName) {
        int levelNum = levelRecords.size() + 1;
        String displayMapName = "Level " + levelNum;
        
        float bonusTime = 0;
        if (levelNum == 2) bonusTime = 5;
        else if (levelNum == 3) bonusTime = 10;
        
        String rank = "B";
        if (survivalTimer < 40 + bonusTime) rank = "S+";
        else if (survivalTimer < 50 + bonusTime) rank = "S";
        else if (survivalTimer < 60 + bonusTime) rank = "A";
        
        String record = String.format("%s: %.2fs (Rank %s)", displayMapName, survivalTimer, rank);
        levelRecords.add(record);
        System.out.println("Recorded Stats: " + record);
    }

    private void loadNextLevel() {
        
        recordLevelStats(currentMapName);
        
        survivalTimer = 0f;

        String nextMap = null;
        
        if (world.getExitPortal() != null && world.getExitPortal().getNextMap() != null && !world.getExitPortal().getNextMap().isEmpty()) {
             nextMap = world.getExitPortal().getNextMap();
             System.out.println("Switching to map defined in Tiled Portal: " + nextMap);
             
             if ("WIN".equalsIgnoreCase(nextMap)) {
                 showVictoryMenu();
                 return;
             }
        }
        
        if (nextMap == null) {
            String currentSimple = currentMapName.replace(".tmx", "");
            if ("GrassLandsFixed".equals(currentSimple)) {
                nextMap = "GrassLandsFixed_part2.tmx";
            } else if ("GrassLandsFixed_part2".equals(currentSimple)) {
                nextMap = "GrassLandsFixed_part3.tmx";
            } else if ("GrassLandsFixed_part3".equals(currentSimple)) {
                showVictoryMenu();
                return;
            }
        }

        if (nextMap == null) {
            System.err.println("No next level defined for: " + currentMapName);
            
            showVictoryMenu();
            return;
        }

        System.out.println("Loading Next Level: " + nextMap);
        currentMapName = nextMap;
        
        TiledMap newMap = mapManager.loadMap(currentMapName);
        if (newMap == null) {
            System.err.println("Failed to load map: " + currentMapName);
            return;
        }
        
        this.tiledMap = newMap;

        world.dispose();
        
        boolean aggressiveAI = isSurvivalMode || !currentMapName.contains("GrassLandsFixed.tmx") || currentMapName.contains("part");

        world = new World(tiledMap, 32, 32, batch, aggressiveAI);
        
        portal = null; 
        
        float startX = 100;
        float startY = 300;
        
        if (world.getSpawnPoint() != null) {
            startX = world.getSpawnPoint().x;
            startY = world.getSpawnPoint().y;
            Gdx.app.log("GameScreen", "LoadNextLevel: Using World SpawnPoint: " + startX + ", " + startY);
        }
        
        startY = world.getSafeSpawnY(startX, startY, player.getConfig().getWidth(), player.getConfig().getHeight());
        
        player = new Player(startX, startY, batch, player.getConfig(), world);
        world.addEntity(player);
        world.setPlayer(player);
        
        gameCamera.position.set(startX, startY, 0);
        gameCamera.update();
        
        showRulesOverlay();
    }

    private void showVictoryMenu() {
        isGameOver = true; 
        
        recordLevelStats(currentMapName);

        Table overlay = new Table();
        overlay.setFillParent(true);
        
        overlay.setBackground(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.90f))); 
        
        Table content = new Table();
        
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("title"), Color.GOLD);
        Label title = new Label("VICTORY!", titleStyle);
        title.setFontScale(1.5f);
        
        Table statsTable = new Table();
        Label.LabelStyle statStyle = new Label.LabelStyle(skin.getFont("medium"), Color.WHITE);
        
        float totalTime = 0;
        
        for (String record : levelRecords) {
            Label lbl = new Label(record, statStyle);
            statsTable.add(lbl).padBottom(10).row();
            
            try {
                String val = record.split(":")[1].trim().split("s")[0].replace(",",".");
                totalTime += Float.parseFloat(val);
            } catch (Exception e) {}
        }
        
        Label totalLbl = new Label(String.format("TOTAL TIME: %.2fs", totalTime), statStyle);
        
        float averageTime = (levelRecords.size() > 0) ? totalTime / levelRecords.size() : 0;
        Label avgLbl = new Label(String.format("AVERAGE TIME: %.2fs", averageTime), statStyle);
        avgLbl.setColor(Color.CYAN);
        statsTable.add(avgLbl).padTop(20).padBottom(20).row();
        
        String gameRank = "B";
        if (averageTime < 45) gameRank = "S+"; 
        else if (averageTime < 55) gameRank = "S";
        else if (averageTime < 65) gameRank = "A";
        
        Label rankMainLbl = new Label("GAME RANK: " + gameRank, titleStyle);
        if ("S+".equals(gameRank)) rankMainLbl.setColor(Color.GOLD);
        else if ("S".equals(gameRank)) rankMainLbl.setColor(Color.YELLOW);
        else if ("A".equals(gameRank)) rankMainLbl.setColor(Color.GREEN);
        else rankMainLbl.setColor(Color.WHITE);

        TextButton restartBtn = new TextButton("RESTART GAME", skin);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                
                GameEngine.getInstance().setScreen(new GameScreen(screenWidth, screenHeight, false));
            }
        });
        
        TextButton menuBtn = new TextButton("MAIN MENU", skin);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameEngine.getInstance().setScreen(new MainMenuScreen(screenWidth, screenHeight));
            }
        });

        content.add(title).padBottom(30).row();
        content.add(statsTable).width(600).padBottom(30).row();
        content.add(rankMainLbl).padBottom(50).row();
        
        Table btns = new Table();
        btns.add(restartBtn).width(200).height(60).padRight(20);
        btns.add(menuBtn).width(200).height(60);
        
        content.add(btns);
        
        overlay.add(content);
        uiStage.addActor(overlay);
    }
    
    private void togglePauseMenu() {
        if (isGameOver) return;
        
        isPaused = !isPaused;
        
        if (isPaused) {
            
            if (pauseMenuTable == null) { createPauseMenu(); }
            pauseMenuTable.setVisible(true);
            pauseMenuTable.toFront(); 
        } else {
            
            if (pauseMenuTable != null) pauseMenuTable.setVisible(false);
        }
    }
    
    private void createPauseMenu() {
        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.8f))); 
        
        Label title = new Label("PAUSED", new Label.LabelStyle(skin.getFont("title"), Color.WHITE));
        
        TextButton resumeBtn = new TextButton("RESUME", skin);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePauseMenu();
            }
        });

        TextButton settingsBtn = new TextButton("SETTINGS", skin);
        settingsBtn.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               showSettingsWindow();
           }
        });

        TextButton controlsBtn = new TextButton("CONTROLS", skin);
        controlsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                 showControlsWindow();
            }
        });
        
        TextButton restartBtn = new TextButton("RESTART", skin);
        restartBtn.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               GameEngine.getInstance().setScreen(new GameScreen(screenWidth, screenHeight, isSurvivalMode));
           }
        });
        
        TextButton menuBtn = new TextButton("MAIN MENU", skin);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameEngine.getInstance().setScreen(new MainMenuScreen(screenWidth, screenHeight));
            }
        });
        
        pauseMenuTable.add(title).padBottom(40).row();
        pauseMenuTable.add(resumeBtn).width(200).padBottom(20).row();
        pauseMenuTable.add(settingsBtn).width(200).padBottom(20).row();
        pauseMenuTable.add(controlsBtn).width(200).padBottom(20).row();
        pauseMenuTable.add(restartBtn).width(200).padBottom(20).row();
        pauseMenuTable.add(menuBtn).width(200).row();
        
        uiStage.addActor(pauseMenuTable);
    }

    private void playNextMusicTrack() {
        if (musicPlaylist.isEmpty()) return;
        
        String trackPath = musicPlaylist.get(currentTrackIndex);
        
        try {
            if (currentMusic != null) {
                currentMusic.dispose();
            }
            
            if (Gdx.files.internal(trackPath).exists()) {
                currentMusic = Gdx.audio.newMusic(Gdx.files.internal(trackPath));
                
                targetMusicVolume = masterVolume * musicVolume;
                
                currentMusic.setVolume(0);
                currentMusic.play();
                
                isMusicFadingIn = true;
                isMusicFadingOut = false;
                musicFadeTimer = 0;
                
                currentMusic.setOnCompletionListener(music -> {
                    
                    playNextTrackSignal();
                });
            }
        } catch(Exception e) {
            System.out.println("Music load failed: " + trackPath);
            playNextTrackSignal(); 
        }
    }
    
    private void playNextTrackSignal() {
        
        currentTrackIndex++;
        if (currentTrackIndex >= musicPlaylist.size()) {
            currentTrackIndex = 0; 
        }
        playNextMusicTrack();
    }
    
    private void updateMusic(float delta) {
        if (currentMusic == null) return;
        
        float maxVol = masterVolume * musicVolume;
        
        if (isMusicFadingIn) {
            musicFadeTimer += delta;
            float alpha = musicFadeTimer / musicFadeDuration;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                isMusicFadingIn = false;
            }
            currentMusic.setVolume(alpha * maxVol);
        }
        
    }
}
