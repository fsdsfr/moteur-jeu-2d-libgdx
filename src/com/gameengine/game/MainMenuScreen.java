package com.gameengine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gameengine.engine.core.AbstractScreen;
import com.gameengine.engine.core.GameEngine;

public class MainMenuScreen extends AbstractScreen {

    private Stage stage;
    private Skin skin;
    private Texture[] backgroundLayers;
    private float stateTime = 0f;
    
    private com.badlogic.gdx.audio.Music menuMusic;
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;

    public MainMenuScreen(float screenWidth, float screenHeight) {
        super(screenWidth, screenHeight);
    }

    @Override
    public void create() {
        
        backgroundLayers = new Texture[6];
        try {
            for(int i=1; i<=6; i++) {
                if (Gdx.files.internal("gfx/background/Forest_layer" + i + ".png").exists()) {
                    Texture t = new Texture(Gdx.files.internal("gfx/background/Forest_layer" + i + ".png"));
                    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    backgroundLayers[i-1] = t;
                }
            }
        } catch (Exception e) {
             System.out.println("Background loading warning: " + e.getMessage());
        }
        
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("settings_prefs");
        masterVolume = prefs.getFloat("volume", 1.0f);
        musicVolume = prefs.getFloat("music_volume", 1.0f);
        sfxVolume = prefs.getFloat("sfx_volume", 1.0f);
        
        try {
            if (Gdx.files.internal("music/Pixel 12.mp3").exists()) { 
                menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Pixel 12.mp3"));
                menuMusic.setLooping(true);
                menuMusic.setVolume(masterVolume * musicVolume);
                menuMusic.play();
            }
        } catch(Exception e) {
             System.out.println("Menu music error: " + e.getMessage());
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createBasicSkin();
        
        Table table = new Table();
        table.setFillParent(true);
        
        table.setBackground(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.4f)));
        stage.addActor(table);

        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = skin.getFont("logo");
        labelStyle.fontColor = new Color(0.95f, 0.95f, 0.8f, 1f); 
        
        com.badlogic.gdx.scenes.scene2d.ui.Label titleLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("FOREST ADVENTURE", labelStyle);
        
        TextButton openWorldButton = new TextButton("Open World", skin);
        openWorldButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                
                GameEngine.getInstance().setScreen(new GameScreen(screenWidth, screenHeight, true));
            }
        });

        TextButton tutorialButton = new TextButton("Story Mode", skin);
        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                 
                 GameEngine.getInstance().setScreen(new GameScreen(screenWidth, screenHeight, false));
            }
        });

        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSettingsWindow();
            }
        });

        TextButton guideButton = new TextButton("Game Guide", skin);
        guideButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showGuideWindow();
            }
        });

        TextButton controlsButton = new TextButton("Controls", skin);

        controlsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showControlsWindow();
            }
        });

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        table.add(titleLabel).padBottom(60).row();
        table.add(openWorldButton).width(350).height(50).padBottom(15).row();
        table.add(tutorialButton).width(350).height(50).padBottom(15).row();
        table.add(guideButton).width(350).height(50).padBottom(15).row();
        table.add(settingsButton).width(350).height(50).padBottom(15).row();
        table.add(controlsButton).width(350).height(50).padBottom(15).row();
        table.add(exitButton).width(350).height(50).padBottom(15).row();
    }

    private void createBasicSkin() {
        skin = new Skin();

        Pixmap pWhite = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pWhite.setColor(Color.WHITE);
        pWhite.fill();
        skin.add("white", new Texture(pWhite));

        com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator = null;
        try {
            
            if (Gdx.files.absolute("C:/Windows/Fonts/taileb.ttf").exists()) {
                 
                 generator = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(Gdx.files.absolute("C:/Windows/Fonts/taileb.ttf"));
            } else if (Gdx.files.absolute("C:/Windows/Fonts/arialbd.ttf").exists()) {
                 generator = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(Gdx.files.absolute("C:/Windows/Fonts/arialbd.ttf"));
            } else {
                 
                 System.out.println("No system font found, using default.");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        BitmapFont logoFont;
        BitmapFont titleFont;
        BitmapFont defaultFont;

        if (generator != null) {
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();
            
            parameter.size = 72; 
            parameter.borderWidth = 3;
            parameter.borderColor = new Color(0.2f, 0.1f, 0f, 1f); 
            parameter.shadowOffsetX = 3;
            parameter.shadowOffsetY = 3;
            parameter.shadowColor = new Color(0,0,0,0.5f);
            logoFont = generator.generateFont(parameter);
            
            parameter.size = 48; 
            parameter.borderWidth = 2;
            parameter.shadowOffsetX = 2;
            parameter.shadowOffsetY = 2;
            titleFont = generator.generateFont(parameter);
            
            parameter.size = 28;
            parameter.borderWidth = 1;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = new Color(0,0,0,0.5f); 
            parameter.borderColor = Color.BLACK; 
            defaultFont = generator.generateFont(parameter);
            
            parameter.size = 32;
            parameter.borderWidth = 1;
            BitmapFont mediumFont = generator.generateFont(parameter);
            mediumFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            skin.add("medium", mediumFont);
            
            generator.dispose();
        } else {
            
            defaultFont = new BitmapFont();
            titleFont = new BitmapFont();
            titleFont.getData().setScale(2f); 
            logoFont = new BitmapFont();
            logoFont.getData().setScale(4f);
            skin.add("medium", defaultFont);
        }

        if (logoFont != null) logoFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin.add("default", defaultFont);
        skin.add("title", titleFont);
        skin.add("logo", logoFont);

        Color btnUp = new Color(0.18f, 0.45f, 0.18f, 1f);    
        Color btnDown = new Color(0.35f, 0.25f, 0.15f, 1f);  
        Color btnOver = new Color(0.25f, 0.55f, 0.25f, 1f);  
        
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", btnUp);
        textButtonStyle.down = skin.newDrawable("white", btnDown);
        textButtonStyle.checked = skin.newDrawable("white", btnDown);
        textButtonStyle.over = skin.newDrawable("white", btnOver);
        textButtonStyle.font = skin.getFont("default");
        textButtonStyle.fontColor = new Color(1f, 1f, 0.9f, 1f); 
        
        skin.add("default", textButtonStyle);

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle ws = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        ws.titleFont = skin.getFont("default"); 
        ws.titleFontColor = Color.GOLD; 
        
        Pixmap bgPix = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        bgPix.setColor(new Color(0.6f, 0.45f, 0.2f, 1f)); 
        bgPix.fill();
        bgPix.setColor(new Color(0.15f, 0.15f, 0.15f, 1f)); 
        bgPix.fillRectangle(3, 3, 58, 58); 
        Texture bgTex = new Texture(bgPix);
        bgPix.dispose();
        
        ws.background = new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
            new com.badlogic.gdx.graphics.g2d.NinePatch(bgTex, 3, 3, 3, 3)
        );
        skin.add("default", ws);

        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle ls = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        ls.font = skin.getFont("default");
        ls.fontColor = Color.WHITE;
        skin.add("default", ls);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        stage.act(delta);
    }

    @Override
    public void render() {
        
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if(backgroundLayers != null) {
            
            for(int i=0; i<backgroundLayers.length; i++) {
                Texture t = backgroundLayers[i];
                if(t != null) {
                    
                    float factor = (i + 1) * 10f; 
                    float offset = -(stateTime * factor) % t.getWidth(); 
                    
                    batch.draw(t, offset, 0, t.getWidth(), screenHeight);
                    batch.draw(t, offset + t.getWidth(), 0, t.getWidth(), screenHeight);
                    if (screenWidth > t.getWidth()) {
                         
                         batch.draw(t, offset + 2*t.getWidth(), 0, t.getWidth(), screenHeight);
                    }
                }
            }
        }
        batch.end();

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.setToOrtho(false, width, height);
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (batch != null) batch.dispose();
        if (menuMusic != null) menuMusic.dispose();
        if (backgroundLayers != null) {
            for(Texture t : backgroundLayers) if(t!=null) t.dispose();
        }
    }

    private void showGuideWindow() {
        final com.badlogic.gdx.scenes.scene2d.ui.Window win = new com.badlogic.gdx.scenes.scene2d.ui.Window("", skin);
        win.setModal(true);
        win.setMovable(false);
        win.setSize(700, 600);
        win.setPosition(stage.getWidth()/2 - 350, stage.getHeight()/2 - 300);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label titleLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("GAME GUIDE", skin);
        titleLbl.setColor(Color.GOLD);
        titleLbl.setAlignment(com.badlogic.gdx.utils.Align.center);
        win.add(titleLbl).padTop(30).padBottom(20).row();
        
        Table content = new Table();
        
        com.badlogic.gdx.scenes.scene2d.ui.Label header1 = new com.badlogic.gdx.scenes.scene2d.ui.Label("- OPEN WORLD -", skin);
        header1.setColor(Color.ORANGE);
        content.add(header1).padBottom(10).row();
        
        String txt1 = "Procedural Generation Mode.\nInfinite terrain created algorithmically.\nEach run is unique. Survival focus.";
        com.badlogic.gdx.scenes.scene2d.ui.Label desc1 = new com.badlogic.gdx.scenes.scene2d.ui.Label(txt1, skin);
        desc1.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(desc1).padBottom(30).row();
        
        com.badlogic.gdx.scenes.scene2d.ui.Label header2 = new com.badlogic.gdx.scenes.scene2d.ui.Label("- STORY MODE -", skin);
        header2.setColor(Color.CYAN);
        content.add(header2).padBottom(10).row();
        
        String txt2 = "Hand-crafted Tiled Levels.\nMaps are loaded directly from TMX files.\nUses 'Portal' linking system for progression.";
        com.badlogic.gdx.scenes.scene2d.ui.Label desc2 = new com.badlogic.gdx.scenes.scene2d.ui.Label(txt2, skin);
        desc2.setAlignment(com.badlogic.gdx.utils.Align.center);
        content.add(desc2).padBottom(20).row();
        
        win.add(content).row();
        
        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                win.remove();
            }
        });
        
        win.add(closeBtn).width(200).height(40).padTop(20);
        
        stage.addActor(win);
    }

    private void showSettingsWindow() {
        
        final com.badlogic.gdx.scenes.scene2d.ui.Window win = new com.badlogic.gdx.scenes.scene2d.ui.Window("", skin);
        win.setModal(true);
        win.setMovable(false);
        win.setSize(500, 450);
        win.setPosition(stage.getWidth()/2 - 250, stage.getHeight()/2 - 225);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label titleLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("SETTINGS", skin);
        titleLbl.setColor(Color.GOLD);
        titleLbl.setAlignment(com.badlogic.gdx.utils.Align.center);
        
        win.add(titleLbl).padTop(30).padBottom(20).row();
        
        Table content = new Table();
        
        if (!skin.has("default-horizontal", com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle.class)) {
            com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle sliderStyle = new com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle();
            sliderStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
            sliderStyle.background.setMinHeight(10);
            sliderStyle.knob = skin.newDrawable("white", Color.GOLD);
            sliderStyle.knob.setMinWidth(20);
            sliderStyle.knob.setMinHeight(20);
            skin.add("default-horizontal", sliderStyle);
        }
        
        final com.badlogic.gdx.scenes.scene2d.ui.Label masterLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("Master Volume: " + (int)(masterVolume * 100) + "%", new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("default"), Color.WHITE));
        final com.badlogic.gdx.scenes.scene2d.ui.Slider masterSlider = new com.badlogic.gdx.scenes.scene2d.ui.Slider(0f, 1f, 0.05f, false, skin);
        masterSlider.setValue(masterVolume);
        
        final com.badlogic.gdx.scenes.scene2d.ui.Label musicLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("Music Volume: " + (int)(musicVolume * 100) + "%", new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("default"), Color.WHITE));
        final com.badlogic.gdx.scenes.scene2d.ui.Slider musicSlider = new com.badlogic.gdx.scenes.scene2d.ui.Slider(0f, 1f, 0.05f, false, skin);
        musicSlider.setValue(musicVolume);
        
        final com.badlogic.gdx.scenes.scene2d.ui.Label sfxLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label("SFX Volume: " + (int)(sfxVolume * 100) + "%", new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("default"), Color.WHITE));
        final com.badlogic.gdx.scenes.scene2d.ui.Slider sfxSlider = new com.badlogic.gdx.scenes.scene2d.ui.Slider(0f, 1f, 0.05f, false, skin);
        sfxSlider.setValue(sfxVolume);
        
        com.badlogic.gdx.scenes.scene2d.utils.ChangeListener volListener = new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                masterVolume = masterSlider.getValue();
                musicVolume = musicSlider.getValue();
                sfxVolume = sfxSlider.getValue();
                
                masterLbl.setText("Master Volume: " + (int)(masterVolume * 100) + "%");
                musicLbl.setText("Music Volume: " + (int)(musicVolume * 100) + "%");
                sfxLbl.setText("SFX Volume: " + (int)(sfxVolume * 100) + "%");
                
                if (menuMusic != null) {
                    menuMusic.setVolume(masterVolume * musicVolume);
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
        stage.addActor(win);
    }

    private void showControlsWindow() {
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle scrollStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        scrollStyle.titleFont = skin.getFont("title");
        
        scrollStyle.background = skin.newDrawable("white", new Color(0.88f, 0.82f, 0.72f, 1f)); 
        scrollStyle.titleFontColor = new Color(0.3f, 0.2f, 0.1f, 1f);
        
        final com.badlogic.gdx.scenes.scene2d.ui.Window win = new com.badlogic.gdx.scenes.scene2d.ui.Window("", scrollStyle);
        win.setModal(true);
        win.setMovable(true);
        win.setSize(750, 700); 
        
        win.setPosition((stage.getWidth() - 750) / 2, (stage.getHeight() - 700) / 2);
        
        Table content = new Table();
        content.pad(20);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle paperTitleStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("title"), new Color(0.4f, 0.1f, 0.1f, 1f)); 
        com.badlogic.gdx.scenes.scene2d.ui.Label title = new com.badlogic.gdx.scenes.scene2d.ui.Label("CONTROLS", paperTitleStyle);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle inkStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(skin.getFont("medium"), new Color(0.1f, 0.1f, 0.1f, 1f));
        
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

        win.add(title).padTop(20).padBottom(30).row();
        win.add(content).growX().row();
        win.add(closeBtn).width(150).height(50).padBottom(30);
        
        stage.addActor(win);
    }
    
    private void addControlRow(Table t, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle style, String action, String key) {
        com.badlogic.gdx.scenes.scene2d.ui.Label actionLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label(action, style);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label keyLbl = new com.badlogic.gdx.scenes.scene2d.ui.Label(key, style);
        keyLbl.setColor(new Color(0.2f, 0.2f, 0.4f, 1f)); 
        
        t.add(actionLbl).left().expandX(); 
        t.add(keyLbl).right().expandX().row(); 
        
        com.badlogic.gdx.scenes.scene2d.ui.Image sep = new com.badlogic.gdx.scenes.scene2d.ui.Image(skin.newDrawable("white", new Color(0,0,0,0.1f)));
        t.add(sep).height(2).fillX().colspan(2).padTop(10).padBottom(10).row();
    }
}
