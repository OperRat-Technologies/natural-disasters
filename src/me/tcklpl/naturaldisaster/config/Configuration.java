package me.tcklpl.naturaldisaster.config;

import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
    String file();
    ConfigAccess access() default ConfigAccess.OP;
    ConfigScope scope();
    Material icon();
}
