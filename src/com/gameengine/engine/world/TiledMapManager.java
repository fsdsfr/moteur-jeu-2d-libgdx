package com.gameengine.engine.world;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class TiledMapManager {
    private AssetManager assetManager;
    private static final String MAP_PATH = "maps/";
    
    public TiledMapManager() {
        this.assetManager = new AssetManager();
        this.assetManager.setLoader(TiledMap.class, new TmxMapLoader());
    }
    
    public TiledMap loadMap(String mapName) {
        String path;
        
        if (mapName.toLowerCase().endsWith(".tmx")) {
            path = MAP_PATH + mapName;
        } else {
             path = MAP_PATH + mapName + ".tmx";
        }

        assetManager.load(path, TiledMap.class);
        assetManager.finishLoading();
        return assetManager.get(path, TiledMap.class);
    }
    
    public boolean loadMapAsync(String mapName) {
        if (!assetManager.isLoaded(MAP_PATH + mapName + ".tmx")) {
            assetManager.load(MAP_PATH + mapName + ".tmx", TiledMap.class);
            return true;
        }
        return false;
    }
    
    public boolean isMapLoaded(String mapName) {
        return assetManager.isLoaded(MAP_PATH + mapName + ".tmx");
    }
    
    public boolean updateAsyncLoad() {
        return assetManager.update();
    }
    
    public void unloadMap(String mapName) {
        String path = MAP_PATH + mapName + ".tmx";
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }
    
    public void dispose() {
        assetManager.dispose();
    }
}
