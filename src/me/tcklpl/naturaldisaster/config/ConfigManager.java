package me.tcklpl.naturaldisaster.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager {

    private Set<Class<?>> allConfigClasses;
    private final Set<ConfigContainer<?>> allConfigContainers = new HashSet<>();
    private final JavaPlugin main;

    public ConfigManager(JavaPlugin main) {
        this.main = main;
        loadAllConfigs();
    }

    private Set<Class<?>> getAllConfigClasses() {
        if (allConfigClasses == null) {
            Reflections reflections = new Reflections("me.tcklpl.naturaldisaster", new TypeAnnotationsScanner(), new SubTypesScanner());
            allConfigClasses = reflections.getTypesAnnotatedWith(Configuration.class);
        }
        return allConfigClasses;
    }

    private void loadAllConfigs() {
        for (Class<?> clazz : getAllConfigClasses()) {
            Configuration clazzConfig = clazz.getAnnotation(Configuration.class);
            File configFile = new File(String.valueOf(Paths.get(main.getDataFolder().getAbsolutePath(), "configs", clazzConfig.file() + ".yml")));
            FileConfiguration config = null;
            NDConfig ndConfig = null;

            try {
                ndConfig = (NDConfig) clazz.getConstructor().newInstance();
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            assert ndConfig != null;

            if (!configFile.exists()) {
                config = new YamlConfiguration();
                FileConfiguration finalConfig = config;
                NDConfig finalNdConfig = ndConfig;
                Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConfigField.class)).forEach(field -> {
                    field.setAccessible(true);
                    ConfigField configField = field.getAnnotation(ConfigField.class);
                    try {
                        finalConfig.set(configField.name(), field.get(finalNdConfig));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    field.setAccessible(false);
                });
            } else {
                config = YamlConfiguration.loadConfiguration(configFile);

                FileConfiguration finalConfig = config;
                NDConfig finalNdConfig1 = ndConfig;
                Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConfigField.class)).forEach(field -> {
                    field.setAccessible(true);
                    ConfigField configField = field.getAnnotation(ConfigField.class);
                    try {
                        field.set(finalNdConfig1, finalConfig.get(configField.name()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    field.setAccessible(false);
                });
            }
            allConfigContainers.add(new ConfigContainer<>(configFile, config, ndConfig));
        }
    }

    public Set<ConfigContainer<?>> getAllConfigContainers() {
        return allConfigContainers;
    }

    public void saveAllConfigs() {
        for (ConfigContainer<?> container : allConfigContainers) {
            if (!container.getDiskFile().exists() || container.isModified()) {
                FileConfiguration cfg = container.getConfigFile();

                Arrays.stream(container.getConfig().getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConfigField.class)).forEach(field -> {
                    field.setAccessible(true);
                    ConfigField cfgField = field.getAnnotation(ConfigField.class);
                    try {
                        cfg.set(cfgField.name(), field.get(container.getConfig()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    field.setAccessible(false);
                });

                if (container.getDiskFile().getParentFile().mkdirs())
                    Bukkit.getLogger().info("Criando a pasta configs");

                try {
                    cfg.save(container.getDiskFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public <T extends NDConfig> T requestConfig(Class<T> config) {
        for (ConfigContainer<?> container: allConfigContainers) {
            if (config.isInstance(container.getConfig()))
                return config.cast(container.getConfig());
        }
        return null;
    }
}
