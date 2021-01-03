package me.tcklpl.naturaldisaster.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class ConfigManager {

    private Set<Class<?>> allConfigClasses;
    private Set<NDConfig> allConfigInstances;
    private final JavaPlugin main;

    public ConfigManager(JavaPlugin main) {
        this.main = main;
        loadAllConfigs();
    }

    private Set<Class<?>> getAllConfigClasses() {
        if (allConfigClasses == null) {
            Reflections reflections = new Reflections("me.tcklpl.naturaldisaster", new TypeAnnotationsScanner());
            allConfigClasses = reflections.getTypesAnnotatedWith(Configuration.class);
        }
        return allConfigClasses;
    }

    private void loadAllConfigs() {
        for (Class<?> clazz : getAllConfigClasses()) {
            Configuration clazzConfig = clazz.getAnnotation(Configuration.class);
            File configFile = new File(String.valueOf(Paths.get(main.getDataFolder().getAbsolutePath(), "configs", clazzConfig.file() + ".yml")));
            if (!configFile.exists()) {
                try {
                    NDConfig cfg = (NDConfig) clazzConfig.getClass().getConstructor().newInstance();
                    cfg.fillDefaults();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
