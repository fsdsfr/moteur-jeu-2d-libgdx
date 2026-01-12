package com.gameengine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gameengine.engine.core.AbstractScreen;
import com.gameengine.engine.core.GameEngine;

public class SettingsScreen extends AbstractScreen {

    private Stage stage;
    private Skin skin;
    private AbstractScreen previousScreen;

    public SettingsScreen(float screenWidth, float screenHeight, AbstractScreen previousScreen) {
        super(screenWidth, screenHeight);
        this.previousScreen = previousScreen;
    }

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createBasicSkin();

        Table table = new Table();
        table.setFillParent(true);
        
        table.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 1f)));
        stage.addActor(table);

        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("title"), Color.WHITE);
        Label titleLabel = new Label("SETTINGS", labelStyle);

        Label.LabelStyle infoStyle = new Label.LabelStyle(skin.getFont("default"), Color.LIGHT_GRAY);
        Label infoLabel = new Label("(Settings are empty for now)", infoStyle);

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                
                if (previousScreen != null) {
                    GameEngine.getInstance().setScreen(previousScreen);
                } else {
                    GameEngine.getInstance().setScreen(new MainMenuScreen(screenWidth, screenHeight));
                }
            }
        });

        table.add(titleLabel).padBottom(20).row();
        table.add(infoLabel).padBottom(50).row();
        table.add(backButton).width(200).height(50);
    }

    private void createBasicSkin() {
        skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.5f);
        skin.add("default", font);
        
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        skin.add("title", titleFont);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
        stage.act();
    }

    @Override
    public void update(float delta) {}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
