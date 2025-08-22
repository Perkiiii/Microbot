package net.runelite.client.plugins.microbot.pvputilities.enums;

import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

public enum SpellType {
    NONE("None", null),
    ICE_BARRAGE("Ice Barrage", MagicAction.ICE_BARRAGE),
    BLOOD_BARRAGE("Blood Barrage", MagicAction.BLOOD_BARRAGE),
    ICE_BLITZ("Ice Blitz", MagicAction.ICE_BLITZ),
    SMOKE_BARRAGE("Smoke Barrage", MagicAction.SMOKE_BARRAGE);

    private final String name;
    private final MagicAction magicAction;

    SpellType(String name, MagicAction magicAction) {
        this.name = name;
        this.magicAction = magicAction;
    }

    public String getName() { return name; }
    public MagicAction getMagicAction() { return magicAction; }

    @Override
    public String toString() { return name; }
}
