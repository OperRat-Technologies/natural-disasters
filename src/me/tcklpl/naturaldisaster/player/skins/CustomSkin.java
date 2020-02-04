package me.tcklpl.naturaldisaster.player.skins;

public class CustomSkin {

    private String name, value, signature;

    public CustomSkin(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
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
}
