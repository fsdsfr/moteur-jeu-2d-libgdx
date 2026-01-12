package com.gameengine.engine.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    
    private Gson gson;
    private static final String CONFIG_PATH = "assets/config/";
    
    public ConfigManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public <T> T loadConfig(String filename, Class<T> configClass) {
        try (FileReader reader = new FileReader(CONFIG_PATH + filename)) {
            return gson.fromJson(reader, configClass);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la configuration: " + filename);
            e.printStackTrace();
            return null;
        }
    }
    
    public void saveConfig(String filename, Object config) {
        try (FileWriter writer = new FileWriter(CONFIG_PATH + filename)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la configuration: " + filename);
            e.printStackTrace();
        }
    }
    
    public String toJson(Object object) {
        return gson.toJson(object);
    }
    
    public <T> T fromJson(String json, Class<T> configClass) {
        return gson.fromJson(json, configClass);
    }
}
