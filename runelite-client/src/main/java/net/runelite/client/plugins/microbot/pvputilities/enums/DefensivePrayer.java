package net.runelite.client.plugins.microbot.pvputilities.enums;

import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;

/**
 * Enum representing defensive prayers for PvP combat
 */
public enum DefensivePrayer
{
    PROTECT_MELEE("Protect from Melee"),
    PROTECT_RANGE("Protect from Missiles"),
    PROTECT_MAGIC("Protect from Magic");

    private final String prayerName;

    DefensivePrayer(String prayerName)
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
     * Converts this DefensivePrayer to the corresponding Rs2PrayerEnum
     */
    public Rs2PrayerEnum toPrayerEnum()
    {
        switch (this)
        {
            case PROTECT_MELEE:
                return Rs2PrayerEnum.PROTECT_MELEE;
            case PROTECT_RANGE:
                return Rs2PrayerEnum.PROTECT_RANGE;
            case PROTECT_MAGIC:
                return Rs2PrayerEnum.PROTECT_MAGIC;
            default:
                throw new IllegalArgumentException("Unknown defensive prayer: " + this);
        }
    }
}
