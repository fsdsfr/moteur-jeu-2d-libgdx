package com.gameengine.engine.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.Gdx;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class ProceduralLevelGenerator {

    private Texture simpleTileset;
    private Texture complexTileset;
    
    private StaticTiledMapTile tileSlopeUp, tileSlopeDown;
    private StaticTiledMapTile tileFlat, tileMid, tileBot;
    private StaticTiledMapTile tileCornerLeft, tileCornerRight;
    
    private TextureRegion[][] treeAsset; 
    private TextureRegion[][] treeAsset2; 
    private TextureRegion[] rocks;
    private TextureRegion tPlant;

    private Random random;

    private enum SectionType {
        FLAT,
        SLOPE_UP,
        SLOPE_DOWN,
        GAP
    }

    private class Section {
        SectionType type;
        int length;
        int startHeight;

        public Section(SectionType type, int length, int startH) {
            this.type = type;
            this.length = length;
            this.startHeight = startH;
        }
    }
    
    private int lastEndHeight = 12; 
    
    private int distanceSinceLastHeart = 0;

    public ProceduralLevelGenerator() {
        this.random = new Random();
    }
    
    public void reset() {
        lastEndHeight = 12; 
        isFirstChunk = true; 
    }
    
    private boolean isFirstChunk = true;

    private void loadAssets() {
        if (simpleTileset == null) {
            simpleTileset = new Texture(Gdx.files.internal("tilesets/GrassLandsSimpleTileset.png"));
            TextureRegion[][] grid = TextureRegion.split(simpleTileset, 32, 32);
            
            TextureRegion tTopLeft = grid[0][0]; TextureRegion tTopMid = grid[0][1]; TextureRegion tTopRight = grid[0][2];
            TextureRegion tMidLeft = grid[1][0]; TextureRegion tMidMid = grid[1][1]; TextureRegion tMidRight = grid[1][2];
            TextureRegion tBotLeft = grid[2][0]; TextureRegion tBotMid = grid[2][1]; TextureRegion tBotRight = grid[2][2];
            
            tileFlat = new StaticTiledMapTile(tTopMid);
            tileMid = new StaticTiledMapTile(tMidMid);
            tileBot = new StaticTiledMapTile(tBotMid);
            
            tileCornerLeft = new StaticTiledMapTile(grid[4][7]);
            tileCornerLeft.getProperties().put("no_collision", true);

            tileCornerRight = new StaticTiledMapTile(grid[4][6]);
            tileCornerRight.getProperties().put("no_collision", true);

            TextureRegion tSlopeRight = grid[3][7]; 
            TextureRegion tSlopeLeft = grid[3][6];  
            
            tileSlopeUp = new StaticTiledMapTile(tSlopeRight);
            tileSlopeUp.getProperties().put("slope", "up");
            
            tileSlopeDown = new StaticTiledMapTile(tSlopeLeft);
            tileSlopeDown.getProperties().put("slope", "down");
            
            tPlant = grid[3][1]; 
        }

        if (complexTileset == null) {
            complexTileset = new Texture(Gdx.files.internal("tilesets/GrassLands32x32.png"));
            TextureRegion[][] grid = TextureRegion.split(complexTileset, 32, 32);
            treeAsset = new TextureRegion[5][2];
            int startRow = 50; 
            int startCol = 53; 
            for(int r=0; r<5; r++) {
                for(int c=0; c<2; c++) {
                    if (startRow+r < grid.length && startCol+c < grid[0].length) {
                        treeAsset[r][c] = grid[startRow + r][startCol + c];
                    }
                }
            }

            treeAsset2 = new TextureRegion[6][6];
            
            int t2StartRow = 49;
            int t2StartCol = 56;
            
            for(int r=0; r<6; r++) {
                for(int c=0; c<6; c++) {
                    if (t2StartRow+r < grid.length && t2StartCol+c < grid[0].length) {
                         treeAsset2[r][c] = grid[t2StartRow + r][t2StartCol + c];
                    }
                }
            }

            rocks = new TextureRegion[4];
            
            rocks[0] = grid[50][51]; 
            
            rocks[1] = grid[51][51];
            rocks[2] = grid[51][52];
            
            rocks[3] = grid[50][50]; 
        }
    }

    public TiledMap generate(int width, int height) {
        loadAssets();
        
        TiledMap map = new TiledMap();
        TiledMapTileLayer mainLayer = new TiledMapTileLayer(width, height, 32, 32);
        mainLayer.setName("Level");
        TiledMapTileLayer decoLayer = new TiledMapTileLayer(width, height, 32, 32);
        decoLayer.setName("Decoration");

        MapLayer objectLayer = new MapLayer();
        objectLayer.setName("Entities");
        
        List<Section> sections = new ArrayList<>();
        int currentX = 0;
        int currentH = lastEndHeight; 
        
        if (isFirstChunk) { 
             currentH = 12; 
             
             sections.add(new Section(SectionType.FLAT, 15, currentH));
             currentX += 15;
             isFirstChunk = false; 
        } else {
             
             sections.add(new Section(SectionType.FLAT, 2, currentH));
             currentX += 2;
        }
        
        while (currentX < width - 15) {
            Section last = sections.get(sections.size()-1);
            float r = random.nextFloat();
            
            if (currentH < 5) {
                 r = 0.6f; 
            }
            
            if (currentH > height - 8) {
                 
            }

            if (last.type == SectionType.GAP) {
                
                int len = 7 + random.nextInt(4);
                
                int delta = random.nextInt(3) - 1; 
                currentH = Math.max(4, Math.min(height-10, last.startHeight + delta));
                
                sections.add(new Section(SectionType.FLAT, len, currentH));
                currentX += len;
            } 
            else if (last.type == SectionType.SLOPE_UP || last.type == SectionType.SLOPE_DOWN) {
                
                int len = 4 + random.nextInt(4);
                
                sections.add(new Section(SectionType.FLAT, len, currentH));
                currentX += len;
            }
            else {
                
                if (r < 0.45f) { 
                    int len = 4 + random.nextInt(6);
                    sections.add(new Section(SectionType.FLAT, len, currentH));
                    currentX += len;
                } 
                else if (r < 0.75f) { 
                    
                    int slopeLen = 4 + random.nextInt(5); 
                    
                    boolean goUp;
                    if (currentH < 8) goUp = true; 
                    else if (currentH > height - 12) goUp = false; 
                    else goUp = random.nextBoolean();
                    
                    if (!goUp && (currentH - slopeLen < 4)) {
                         goUp = true; 
                         if (currentH + slopeLen > height - 5) {
                             
                             sections.add(new Section(SectionType.FLAT, slopeLen, currentH));
                             currentX += slopeLen;
                             continue;
                         }
                    }
                    
                    if (goUp) {
                        sections.add(new Section(SectionType.SLOPE_UP, slopeLen, currentH));
                        currentH += slopeLen; 
                    } else {
                        sections.add(new Section(SectionType.SLOPE_DOWN, slopeLen, currentH));
                        currentH -= slopeLen;
                    }
                    currentX += slopeLen;
                }
                else { 
                    int gapW = 3 + random.nextInt(2);
                    sections.add(new Section(SectionType.GAP, gapW, currentH));
                    
                    currentX += gapW;
                }
            }
        }
        
        sections.add(new Section(SectionType.FLAT, width - currentX, currentH));

        int renderX = 0;
        int lastTreeX = -999;
        
        for (Section sec : sections) {
            if (sec.type == SectionType.GAP) {
                renderX += sec.length;
                continue;
            }
            
            else if (sec.type == SectionType.FLAT) {
                for(int i=0; i<sec.length; i++) {
                     int x = renderX + i;
                     fillColumn(mainLayer, x, sec.startHeight, tileFlat, tileMid, tileBot);
                     
                     boolean hasTree = false;

                     distanceSinceLastHeart++;

                     if (i > 1 && i < sec.length - 2 && (x - lastTreeX > 4)) { 
                          if (distanceSinceLastHeart >= 100) {
                              
                              RectangleMapObject heartObj = new RectangleMapObject(x*32, (sec.startHeight + 1)*32, 32, 32);
                              heartObj.setName("Heart");
                              objectLayer.getObjects().add(heartObj);
                              
                              distanceSinceLastHeart = 0;
                              lastTreeX = x; 
                              hasTree = true;
                          }
                          else if (random.nextFloat() < 0.40f) { 
                               int tType = random.nextBoolean() ? 1 : 2; 
                               drawTreeOverlay(decoLayer, x, sec.startHeight + 1, tType);
                               lastTreeX = x;
                               hasTree = true;
                          }
                     }
                     
                     if (!hasTree && random.nextFloat() < 0.25f) { 
                           Cell c = new Cell();
                           TextureRegion region = rocks[random.nextInt(rocks.length)];
                           
                           StaticTiledMapTile tile = new StaticTiledMapTile(region);
                           
                           c.setTile(tile);
                           
                           if (decoLayer.getCell(x, sec.startHeight+1) == null) {
                               decoLayer.setCell(x, sec.startHeight+1, c);
                           }
                     }

                     else if (false && !hasTree && random.nextFloat() < 0.25f) {
                         if (decoLayer.getCell(x, sec.startHeight+1) == null) {
                             Cell c = new Cell();
                             c.setTile(new StaticTiledMapTile(tPlant));
                             decoLayer.setCell(x, sec.startHeight+1, c);
                         }
                     }

                     if (i > 1 && i < sec.length - 2 && random.nextFloat() < 0.20f) { 
                          
                          RectangleMapObject enemyObj = new RectangleMapObject(x * 32, (sec.startHeight + 1) * 32 + 60, 32, 48); 
                          enemyObj.setName("Goblin");
                          String typeName = "SCOUT";
                          float roll = random.nextFloat();
                          if (roll < 0.33f) typeName = "ARCHER";
                          else if (roll < 0.66f) typeName = "TANK";
                          
                          enemyObj.getProperties().put("type", typeName);
                          objectLayer.getObjects().add(enemyObj);
                          
                          lastTreeX = x; 
                     }
                }
                renderX += sec.length;
            }

            else if (sec.type == SectionType.SLOPE_UP) {
                for(int i=0; i<sec.length; i++) {
                    int x = renderX + i;
                    
                    int surfaceY = sec.startHeight + 1 + i;
                    fillColumn(mainLayer, x, surfaceY, tileSlopeUp, tileMid, tileBot);
                    
                    if (surfaceY - 1 >= 0) {
                         Cell c = new Cell();
                         c.setTile(tileCornerLeft); 
                         mainLayer.setCell(x, surfaceY - 1, c);
                    }
                }
                renderX += sec.length;
            }
            
            else if (sec.type == SectionType.SLOPE_DOWN) {
                for(int i=0; i<sec.length; i++) {
                    int x = renderX + i;
                    
                    int surfaceY = sec.startHeight - i;
                    fillColumn(mainLayer, x, surfaceY, tileSlopeDown, tileMid, tileBot);

                    if (surfaceY - 1 >= 0) {
                         Cell c = new Cell();
                         c.setTile(tileCornerRight); 
                         mainLayer.setCell(x, surfaceY - 1, c);
                    }
                }
                renderX += sec.length;
            }
            
            else if (sec.type == SectionType.GAP) {
                
                renderX += sec.length;
            }
            
        }
        
        map.getLayers().add(mainLayer);
        map.getLayers().add(decoLayer);
        map.getLayers().add(objectLayer);
        
        if (!sections.isEmpty()) {
             lastEndHeight = sections.get(sections.size()-1).startHeight;
        }
        
        return map;
    }
    
    private void fillColumn(TiledMapTileLayer layer, int x, int surfaceY, 
                            StaticTiledMapTile top, StaticTiledMapTile mid, StaticTiledMapTile bot) {
        if (x < 0 || x >= layer.getWidth()) return;
        
        Cell cSurf = new Cell();
        cSurf.setTile(top);
        layer.setCell(x, surfaceY, cSurf);
        
        for(int y=0; y<surfaceY; y++) {
             Cell c = new Cell();
             if (y == 0) c.setTile(bot); 
             else c.setTile(mid);        
             layer.setCell(x, y, c);
        }
    }
    
    private void drawTreeOverlay(TiledMapTileLayer deco, int x, int y, int type) {
        if (type == 1) {
            
            int offsetAdjustment = 1; 
            for (int c=0; c<2; c++) {
                for (int r=0; r<5; r++) {
                     int assetRow = 4 - r;
                     int worldX = x + c;
                     int worldY = y + r - offsetAdjustment; 
                     
                     if (worldY < deco.getHeight() && worldY >= 0) {
                         Cell cell = new Cell();
                         cell.setTile(new StaticTiledMapTile(treeAsset[assetRow][c]));
                         deco.setCell(worldX, worldY, cell);
                     }
                }
            }
        } else {
            
            int startX = x - 2; 
            
            int offsetAdjustment = 1;

            for (int c=0; c<6; c++) {
                for (int r=0; r<6; r++) {
                     
                     int assetRow = 5 - r;
                     
                     int worldX = startX + c;
                     int worldY = y + r - offsetAdjustment;
                     
                     if (worldX >= 0 && worldX < deco.getWidth() && worldY < deco.getHeight() && worldY >= 0) {
                         
                         Cell cell = new Cell();
                         cell.setTile(new StaticTiledMapTile(treeAsset2[assetRow][c]));
                         deco.setCell(worldX, worldY, cell);
                     }
                }
            }
        }
    }
}
