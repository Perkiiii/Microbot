package net.runelite.client.plugins.microbot.pvputilities.enums;

import net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells;
import net.runelite.client.plugins.microbot.util.magic.Spell;

public enum SpellOption
{
    NONE("None", null),
    WIND_STRIKE("Wind Strike", Rs2CombatSpells.WIND_STRIKE),
    WATER_STRIKE("Water Strike", Rs2CombatSpells.WATER_STRIKE),
    EARTH_STRIKE("Earth Strike", Rs2CombatSpells.EARTH_STRIKE),
    FIRE_STRIKE("Fire Strike", Rs2CombatSpells.FIRE_STRIKE),
    WIND_BOLT("Wind Bolt", Rs2CombatSpells.WIND_BOLT),
    WATER_BOLT("Water Bolt", Rs2CombatSpells.WATER_BOLT),
    EARTH_BOLT("Earth Bolt", Rs2CombatSpells.EARTH_BOLT),
    FIRE_BOLT("Fire Bolt", Rs2CombatSpells.FIRE_BOLT),
    WIND_BLAST("Wind Blast", Rs2CombatSpells.WIND_BLAST),
    WATER_BLAST("Water Blast", Rs2CombatSpells.WATER_BLAST),
    EARTH_BLAST("Earth Blast", Rs2CombatSpells.EARTH_BLAST),
    FIRE_BLAST("Fire Blast", Rs2CombatSpells.FIRE_BLAST),
    WIND_WAVE("Wind Wave", Rs2CombatSpells.WIND_WAVE),
    WATER_WAVE("Water Wave", Rs2CombatSpells.WATER_WAVE),
    EARTH_WAVE("Earth Wave", Rs2CombatSpells.EARTH_WAVE),
    FIRE_WAVE("Fire Wave", Rs2CombatSpells.FIRE_WAVE),
    WIND_SURGE("Wind Surge", Rs2CombatSpells.WIND_SURGE),
    WATER_SURGE("Water Surge", Rs2CombatSpells.WATER_SURGE),
    EARTH_SURGE("Earth Surge", Rs2CombatSpells.EARTH_SURGE),
    FIRE_SURGE("Fire Surge", Rs2CombatSpells.FIRE_SURGE),
    ICE_RUSH("Ice Rush", Rs2CombatSpells.ICE_RUSH),
    ICE_BURST("Ice Burst", Rs2CombatSpells.ICE_BURST),
    ICE_BLITZ("Ice Blitz", Rs2CombatSpells.ICE_BLITZ),
    ICE_BARRAGE("Ice Barrage", Rs2CombatSpells.ICE_BARRAGE),
    BLOOD_RUSH("Blood Rush", Rs2CombatSpells.BLOOD_RUSH),
    BLOOD_BURST("Blood Burst", Rs2CombatSpells.BLOOD_BURST),
    BLOOD_BLITZ("Blood Blitz", Rs2CombatSpells.BLOOD_BLITZ),
    BLOOD_BARRAGE("Blood Barrage", Rs2CombatSpells.BLOOD_BARRAGE),
    SHADOW_RUSH("Shadow Rush", Rs2CombatSpells.SHADOW_RUSH),
    SHADOW_BURST("Shadow Burst", Rs2CombatSpells.SHADOW_BURST),
    SHADOW_BLITZ("Shadow Blitz", Rs2CombatSpells.SHADOW_BLITZ),
    SHADOW_BARRAGE("Shadow Barrage", Rs2CombatSpells.SHADOW_BARRAGE),
    SMOKE_RUSH("Smoke Rush", Rs2CombatSpells.SMOKE_RUSH),
    SMOKE_BURST("Smoke Burst", Rs2CombatSpells.SMOKE_BURST),
    SMOKE_BLITZ("Smoke Blitz", Rs2CombatSpells.SMOKE_BLITZ),
    SMOKE_BARRAGE("Smoke Barrage", Rs2CombatSpells.SMOKE_BARRAGE);

    private final String spellName;
    private final Rs2CombatSpells spell;

    SpellOption(String spellName, Rs2CombatSpells spell) {
        this.spellName = spellName;
        this.spell = spell;
    }
    SpellOption(String spellName) {
        this(spellName, null);
    }
    public Spell getSpell() {
        return spell;
    }

    public String getSpellName()
    {
        return spellName;
    }

    @Override
    public String toString()
    {
        return spellName;
    }
}
