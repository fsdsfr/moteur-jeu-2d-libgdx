package com.gameengine.engine.world;

public class LevelConfig {
    private String name;
    private String description;
    private int tileWidth;
    private int tileHeight;
    private float playerStartX;
    private float playerStartY;
    private String nextLevel;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }
    public float getPlayerStartX() { return playerStartX; }
    public float getPlayerStartY() { return playerStartY; }
    public String getNextLevel() { return nextLevel; }
}
