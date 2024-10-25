package me.tcklpl.naturaldisaster.player.skins;

import java.sql.Timestamp;
import java.util.Objects;

public class CustomSkin {

    private final String name;
    private final String value;
    private final String signature;
    private final Timestamp timestamp;

    public CustomSkin(String name, String value, String signature, Timestamp timestamp) {
        this.name = name;
        this.value = value;
        this.signature = signature;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomSkin that = (CustomSkin) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
