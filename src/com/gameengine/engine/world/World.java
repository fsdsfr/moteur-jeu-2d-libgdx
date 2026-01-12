package com.gameengine.engine.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gameengine.engine.core.Entity;
import com.gameengine.entity.player.Player;
import com.gameengine.entity.enemy.Enemy;
import com.gameengine.entity.enemy.Goblin;
import com.gameengine.entity.item.HeartPickup;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import java.util.*;

public class World {
    private List<TiledMap> activeChunks;
    private List<OrthogonalTiledMapRenderer> renderers;
    private List<Integer> chunkOffsets;
    private int totalMapWidth;
    
    private List<Entity> entities;
    private List<Rectangle> collisionRectangles;
    private List<Rectangle> slopeUpRects;
    private List<Rectangle> slopeDownRects;
    
    private int mapHeight;
    private int tileWidth;
    private int tileHeight;
    private boolean isSurvivalMode = false;
    
    private SpriteBatch batch;
    private Player player;
    private com.gameengine.entity.Portal exitPortal;
    
    private ShapeRenderer shapeRenderer;
    private boolean debugEnabled = false;
    
    private List<Entity> pendingEntities = new ArrayList<>();
    private List<Entity> deadEntities = new ArrayList<>();
    
    private Vector2 spawnPoint; 

    public World(TiledMap map, int tileWidth, int tileHeight, SpriteBatch batch) {
        this(map, tileWidth, tileHeight, batch, false);
    }
    
    public World(TiledMap map, int tileWidth, int tileHeight, SpriteBatch batch, boolean isSurvivalMode) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.batch = batch;
        this.isSurvivalMode = isSurvivalMode;
        
        this.activeChunks = new ArrayList<>();
        this.renderers = new ArrayList<>();
        this.chunkOffsets = new ArrayList<>();
        
        this.entities = new ArrayList<>();
        this.collisionRectangles = new ArrayList<>();
        this.slopeUpRects = new ArrayList<>();
        this.slopeDownRects = new ArrayList<>();
        
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        if (layer != null) {
            this.mapHeight = layer.getHeight() * tileHeight;
        }
        
        collisionRectangles.add(new Rectangle(-20, 0, 20, mapHeight * 2));
        
        this.shapeRenderer = new ShapeRenderer();

        addChunk(map, 0); 
    }
    
    public float getSafeSpawnY(float x, float y, float w, float h) {
        Rectangle pRect = new Rectangle(x, y, w, h);
        float highestY = y;
        boolean stuck = false;
        
        for (Rectangle wall : collisionRectangles) {
            if (pRect.overlaps(wall)) {
                
                float top = wall.y + wall.height;
                if (top > highestY) {
                    highestY = top;
                    stuck = true;
                }
            }
        }
        
        return stuck ? highestY + 1.0f : y; 
    }

    public void addChunk(TiledMap map, int xOffsetWorld) {
        activeChunks.add(map);
        renderers.add(new OrthogonalTiledMapRenderer(map));
        chunkOffsets.add(xOffsetWorld);
        
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Level");
        if (layer == null && map.getLayers().getCount() > 0 && map.getLayers().get(0) instanceof TiledMapTileLayer) {
             layer = (TiledMapTileLayer) map.getLayers().get(0);
        }
        
        if (layer != null) {
            
            MapLayer entLayer = map.getLayers().get("Entities");
            if (entLayer == null) {
                
                for (MapLayer l : map.getLayers()) {
                     if (l.getName().equalsIgnoreCase("Entities")) {
                         entLayer = l;
                         break;
                     }
                }
            }
            
            if (entLayer != null) {
                System.out.println("Processing Entities Layer: " + entLayer.getName() + " with " + entLayer.getObjects().getCount() + " objects.");
                for (MapObject object : entLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                         RectangleMapObject rObj = (RectangleMapObject) object;
                         float x = rObj.getRectangle().x + xOffsetWorld;
                         float y = rObj.getRectangle().y;
                         
                         if ("Enemy".equals(rObj.getName()) || "Goblin".equals(rObj.getName())) {
                             String t = null;
                             if (rObj.getProperties().containsKey("type")) {
                                 t = String.valueOf(rObj.getProperties().get("type"));
                             }
                             
                             int hp = -1;
                             if (rObj.getProperties().containsKey("hp")) {
                                 try {
                                     hp = Integer.parseInt(String.valueOf(rObj.getProperties().get("hp")));
                                 } catch (NumberFormatException e) {
                                     System.err.println("Invalid hp property for enemy: " + rObj.getName());
                                 }
                             }
                             
                             System.out.println("Found Enemy Object: " + rObj.getName() + " Type: " + t + " HP: " + hp);
                             
                             try {
                                 if (t != null) {
                                     
                                     Goblin.Type gt = Goblin.Type.valueOf(t);
                                     Goblin g = new Goblin(x, y, this, batch, gt, hp);
                                     entities.add(g);
                                     System.out.println("Spawned Goblin: " + gt);
                                 } else {
                                     
                                     Enemy e = new Enemy(x, y, this, batch);
                                     entities.add(e);
                                     System.out.println("Spawned Basic Enemy");
                                 }
                             } catch (Exception ex) {
                                 System.err.println("Failed to spawn enemy type: " + t + " Error: " + ex.getMessage());
                                 ex.printStackTrace();
                             }
                         } else if ("Heart".equals(rObj.getName())) {
                             entities.add(new HeartPickup(x, y, this, batch));
                         } else if ("SpawnPoint".equals(rObj.getName())) {
                             spawnPoint = new Vector2(x, y);
                             System.out.println("Set SpawnPoint from World loading: " + spawnPoint);
                         } else if ("Portal".equals(rObj.getName())) {
                             com.gameengine.entity.Portal p = new com.gameengine.entity.Portal(x, y, 64, 128, batch, com.gameengine.entity.Portal.PortalType.ENTRANCE);
                             
                             if (rObj.getProperties().containsKey("nextMap")) {
                                 String nextMap = (String)rObj.getProperties().get("nextMap");
                                 p.setNextMap(nextMap);
                                 System.out.println("Portal target set to: " + nextMap);
                             }

                             entities.add(p);
                             exitPortal = p; 
                             System.out.println("Spawned Portal from World loading at " + x + ", " + y);
                         } else if ("StartPortal".equals(rObj.getName())) {
                             com.gameengine.entity.Portal p = new com.gameengine.entity.Portal(x, y, 64, 128, batch, com.gameengine.entity.Portal.PortalType.EXIT);
                             entities.add(p);
                             
                             spawnPoint = new Vector2(x + 16, y); 
                             System.out.println("Set SpawnPoint to StartPortal: " + spawnPoint);
                             System.out.println("Spawned StartPortal from World loading at " + x + ", " + y);
                         }
                    }
                }
            } else {
                System.out.println("Layer 'Entities' NOT FOUND in map.");
            }

            MapLayer objLayer = map.getLayers().get("Collision");
            if (objLayer != null) {
                for (MapObject object : objLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        
                        Rectangle finalRect = new Rectangle(rect.x + xOffsetWorld, rect.y, rect.width, rect.height);
                        collisionRectangles.add(finalRect);
                    }
                }
            } 
            
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        if (cell.getTile().getProperties().containsKey("no_collision")) {
                            continue;
                        }

                        float worldX = x * tileWidth + xOffsetWorld;
                        float worldY = y * tileHeight;
                        
                        Rectangle rect = new Rectangle(worldX, worldY, tileWidth, tileHeight);
                        Object slopeProp = cell.getTile().getProperties().get("slope");
                        
                        if (slopeProp != null) {
                            String type = slopeProp.toString();
                            if ("up".equals(type) || "left".equals(type)) { 
                                slopeUpRects.add(rect);
                            } else if ("down".equals(type) || "right".equals(type)) {
                                slopeDownRects.add(rect);
                            } else {
                                collisionRectangles.add(rect);
                            }
                        } else {
                            collisionRectangles.add(rect);
                        }
                    }
                }
            }
            
            int chunkWidthPx = layer.getWidth() * tileWidth;
            if (xOffsetWorld + chunkWidthPx > totalMapWidth) {
                totalMapWidth = xOffsetWorld + chunkWidthPx;
            }
        }
    }
    
    public int getWorldEndX() {
        return totalMapWidth;
    }

    public void addEntity(Entity entity) {
        pendingEntities.add(entity);
    }
    
    public void removeEntity(Entity entity) {
        deadEntities.add(entity);
    }
    
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }
    
    public void setPlayer(Player p) { this.player = p; }
    public Player getPlayer() { return player; }
    
    public Vector2 getSpawnPoint() { return spawnPoint; }
    
    public com.gameengine.entity.Portal getExitPortal() { return exitPortal; }

    public List<Rectangle> getCollisionRectangles() {
        return collisionRectangles;
    }

    public List<Rectangle> getSlopeUpRects() { return slopeUpRects; }
    public List<Rectangle> getSlopeDownRects() { return slopeDownRects; }
    
    public boolean isSurvivalMode() { return isSurvivalMode; }
    
    public void update(float deltaTime) {
        
        if (!pendingEntities.isEmpty()) {
            entities.addAll(pendingEntities);
            pendingEntities.clear();
        }
        
        if (!deadEntities.isEmpty()) {
            entities.removeAll(deadEntities);
            deadEntities.clear();
        }

        for (Entity entity : entities) {
            if (entity.isActive()) {
                entity.update(deltaTime);
            }
        }
    }
    
    public List<Entity> getEntities(Class<? extends Entity> type) {
        List<Entity> result = new ArrayList<>();
        for (Entity e : entities) {
            if (type.isInstance(e) && e.isActive()) {
                result.add(e);
            }
        }
        return result;
    }

    public int getEnemyCount() {
        int count = 0;
        for (Entity e : entities) {
             if (e.isActive()) {
                 
                 if (e instanceof com.gameengine.entity.enemy.Goblin) {
                     if (!((com.gameengine.entity.enemy.Goblin)e).isDead()) {
                         count++;
                     }
                 }
                 
                 else if (e instanceof com.gameengine.entity.enemy.Enemy) {
                     count++;
                 }
             }
        }
        return count;
    }

    public void render(OrthographicCamera camera, SpriteBatch batch) {
        
        for (int i=0; i < renderers.size(); i++) {
             OrthogonalTiledMapRenderer r = renderers.get(i);
             int offsetX = chunkOffsets.get(i);
             
             camera.position.x -= offsetX;
             camera.update();
             r.setView(camera);
             r.render();
             
             camera.position.x += offsetX;
             camera.update();
        }
        
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Entity entity : entities) {
            if (entity.isActive()) {
                entity.render();
            }
        }
        batch.end();
        
        if (debugEnabled) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            
            shapeRenderer.setColor(Color.RED);
            for (Rectangle rect : collisionRectangles) {
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
            }
            
            shapeRenderer.setColor(Color.YELLOW);
            for (Rectangle rect : slopeUpRects) {
                 
                 shapeRenderer.line(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
                 shapeRenderer.line(rect.x, rect.y, rect.x + rect.width, rect.y);
                 shapeRenderer.line(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
            }
            
            shapeRenderer.setColor(Color.ORANGE);
            for (Rectangle rect : slopeDownRects) {
                 
                 shapeRenderer.line(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y);
                 shapeRenderer.line(rect.x, rect.y, rect.x + rect.width, rect.y);
                 shapeRenderer.line(rect.x, rect.y, rect.x, rect.y + rect.height);
            }
            
            if (player != null) {
                shapeRenderer.setColor(Color.GREEN);
                Rectangle b = player.getBounds();
                shapeRenderer.rect(b.x, b.y, b.width, b.height);
            }

            shapeRenderer.end();
        }
    }
    
    public void dispose() {
        for(OrthogonalTiledMapRenderer r : renderers) r.dispose();
        for (Entity entity : entities) {
            entity.dispose();
        }
        entities.clear();
        for(TiledMap m : activeChunks) m.dispose();
    }
    
    public TiledMap getMap() {
        return activeChunks.isEmpty() ? null : activeChunks.get(0);
    }
    
    public int getMapWidth() {
        return totalMapWidth;
    }
    
    public int getMapHeight() {
        return mapHeight;
    }
    
    public int getTileWidth() {
        return tileWidth;
    }
    
    public int getTileHeight() {
        return tileHeight;
    }
}
