package me.tcklpl.naturaldisaster.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigContainer<T extends NDConfig> {

    private final File diskFile;
    private final FileConfiguration configFile;
    private final T config;
    private boolean modified;

    public ConfigContainer(File diskFile, FileConfiguration configFile, T config) {
        this.diskFile = diskFile;
        this.configFile = configFile;
        this.config = config;
    }

    @Override
    public String toString() {
        return "ConfigContainer{" +
                "diskFile=" + diskFile +
                ", configFile=" + configFile +
                ", config=" + config +
                ", modified=" + modified +
                '}';
    }

    public File getDiskFile() {
        return diskFile;
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public T getConfig() {
        return config;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
