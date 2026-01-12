package com.gameengine.engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.*;

public class InputManager {
    private Map<String, Integer> keyMappings;
    private Map<String, IInputAction> actions;
    
    public InputManager() {
        this.keyMappings = new HashMap<>();
        this.actions = new HashMap<>();
        initializeDefaultMappings();
    }
    
    private void initializeDefaultMappings() {
        keyMappings.put("MoveLeft", Input.Keys.A);
        keyMappings.put("MoveRight", Input.Keys.D);
        keyMappings.put("Jump", Input.Keys.SPACE);
        keyMappings.put("Pause", Input.Keys.P);
    }
    
    public void registerAction(String actionName, IInputAction action) {
        actions.put(actionName, action);
    }
    
    public void mapKeyToAction(String actionName, int keyCode) {
        keyMappings.put(actionName, keyCode);
    }
    
    public void update() {
        for (Map.Entry<String, Integer> entry : keyMappings.entrySet()) {
            String actionName = entry.getKey();
            int keyCode = entry.getValue();
            
            if (Gdx.input.isKeyPressed(keyCode)) {
                IInputAction action = actions.get(actionName);
                if (action != null) {
                    action.execute();
                }
            }
        }
    }
    
    public boolean isActionPressed(String actionName) {
        Integer keyCode = keyMappings.get(actionName);
        if (keyCode != null) {
            return Gdx.input.isKeyPressed(keyCode);
        }
        return false;
    }
    
    public boolean isActionJustPressed(String actionName) {
        Integer keyCode = keyMappings.get(actionName);
        if (keyCode != null) {
            return Gdx.input.isKeyJustPressed(keyCode);
        }
        return false;
    }
    
    public float getActionStrength(String actionName) {
        return isActionPressed(actionName) ? 1.0f : 0.0f;
    }
    
    public int[] getMousePosition() {
        return new int[]{Gdx.input.getX(), Gdx.input.getY()};
    }
}
