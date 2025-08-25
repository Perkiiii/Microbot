package net.runelite.client.plugins.microbot.pvputilities.enums;

import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;

public enum OffensivePrayer
{
    BURST_STRENGTH("Burst of Strength"),
    SUPERHUMAN_STRENGTH("Superhuman Strength"),
    ULTIMATE_STRENGTH("Ultimate Strength"),
    SHARP_EYE("Sharp Eye"),
    HAWK_EYE("Hawk Eye"),
    EAGLE_EYE("Eagle Eye"),
    MYSTIC_WILL("Mystic Will"),
    MYSTIC_LORE("Mystic Lore"),
    MYSTIC_MIGHT("Mystic Might"),
    CHIVALRY("Chivalry"),
    PIETY("Piety"),
    RIGOUR("Rigour"),
    AUGURY("Augury");

    private final String prayerName;

    OffensivePrayer(String prayerName)
    {
        this.prayerName = prayerName;
    }

    public String getPrayerName()
    {
        return prayerName;
    }

    @Override
    public String toString()
    {
        return prayerName;
    }

    /**
     * Converts this OffensivePrayer to the corresponding Rs2PrayerEnum
     */
    public Rs2PrayerEnum toPrayerEnum()
    {
        switch (this)
        {
            case BURST_STRENGTH:
                return Rs2PrayerEnum.BURST_STRENGTH;
            case SUPERHUMAN_STRENGTH:
                return Rs2PrayerEnum.SUPERHUMAN_STRENGTH;
            case ULTIMATE_STRENGTH:
                return Rs2PrayerEnum.ULTIMATE_STRENGTH;
            case SHARP_EYE:
                return Rs2PrayerEnum.SHARP_EYE;
            case HAWK_EYE:
                return Rs2PrayerEnum.HAWK_EYE;
            case EAGLE_EYE:
                return Rs2PrayerEnum.EAGLE_EYE;
            case MYSTIC_WILL:
                return Rs2PrayerEnum.MYSTIC_WILL;
            case MYSTIC_LORE:
                return Rs2PrayerEnum.MYSTIC_LORE;
            case MYSTIC_MIGHT:
                return Rs2PrayerEnum.MYSTIC_MIGHT;
            case CHIVALRY:
                return Rs2PrayerEnum.CHIVALRY;
            case PIETY:
                return Rs2PrayerEnum.PIETY;
            case RIGOUR:
                return Rs2PrayerEnum.RIGOUR;
            case AUGURY:
                return Rs2PrayerEnum.AUGURY;
            default:
                return null;
        }
    }
}
