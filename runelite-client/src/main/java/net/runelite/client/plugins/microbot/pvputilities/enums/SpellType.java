package net.runelite.client.plugins.microbot.pvputilities.enums;

public enum SpellType {
    NONE("None"),
    ICE_BARRAGE("Ice Barrage"),
    BLOOD_BARRAGE("Blood Barrage"),
    ICE_BLITZ("Ice Blitz"),
    SMOKE_BARRAGE("Smoke Barrage");

    private final String name;
    SpellType(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}
