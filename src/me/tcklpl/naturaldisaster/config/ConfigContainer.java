package me.tcklpl.naturaldisaster.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Set;

public class ConfigContainer {

    private File configFile;
    private FileConfiguration config;
    private Set<ConfigFieldContainer<?>> fields;

}
