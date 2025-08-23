package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells;

@ConfigGroup("pvputilities")
public interface PvPUtilitiesConfig extends Config
{
    // ===========================================
    // PVP UTILITIES CONFIG SECTION
    // ===========================================
    @ConfigSection(
            name = "PvP Utilities Config",
            description = "Main PvP utilities configuration",
            position = 1
    )
    String pvpUtilitiesConfig = "pvpUtilitiesConfig";

    @ConfigItem(
            keyName = "attackAfterAction",
            name = "Attack After an Action",
            description = "Automatically attack the target again after performing an action.",
            position = 1,
            section = pvpUtilitiesConfig
    )
    default boolean attackAfterAction()
    {
        return false;
    }

    @ConfigItem(
            keyName = "disableUnusedPrayers",
            name = "Disable Unused Prayers",
            description = "Automatically disable prayers when not required.",
            position = 2,
            section = pvpUtilitiesConfig
    )
    default boolean disableUnusedPrayers()
    {
        return false;
    }

    @ConfigItem(
            keyName = "showToggleMessages",
            name = "Show Toggle Feature Messages",
            description = "Display messages when features are toggled on/off.",
            position = 3,
            section = pvpUtilitiesConfig
    )
    default boolean showToggleMessages()
    {
        return true;
    }

    @ConfigItem(
            keyName = "alwaysShowSpecBar",
            name = "Always Show Spec Bar",
            description = "Keep the special attack bar visible at all times.",
            position = 4,
            section = pvpUtilitiesConfig
    )
    default boolean alwaysShowSpecBar()
    {
        return false;
    }

    @ConfigItem(
            keyName = "safeCastSpells",
            name = "Safe Cast Spells",
            description = "Enable safe casting of spells to prevent misclicks.",
            position = 5,
            section = pvpUtilitiesConfig
    )
    default boolean safeCastSpells()
    {
        return false;
    }

    @ConfigItem(
            keyName = "fastGearSwitching",
            name = "Fast Gear Switching",
            description = "Enable faster gear switching with minimal delays (1-3ms instead of 8-18ms). Better for competitive PvP.",
            position = 7,
            section = pvpUtilitiesConfig
    )
    default boolean fastGearSwitching()
    {
        return true;
    }

    @ConfigItem(
            keyName = "walkUnderTarget",
            name = "Walk Under Target",
            description = "Automatically walk under the target.",
            position = 8,
            section = pvpUtilitiesConfig
    )
    default boolean walkUnderTarget()
    {
        return false;
    }

    @ConfigItem(
            keyName = "walkUnderTargetHotkey",
            name = "Walk Under Target Hotkey",
            description = "Hotkey to toggle walk under target functionality.",
            position = 9,
            section = pvpUtilitiesConfig
    )
    default Keybind walkUnderTargetHotkey()
    {
        return Keybind.NOT_SET;
    }


    // ===========================================
    // TARGET VISIBILITY SECTION
    // ===========================================
    @ConfigSection(
            name = "Target Visibility",
            description = "Automatic target highlighting using the advanced target management system",
            position = 2,
            closedByDefault = true
    )
    String targetVisibility = "targetVisibility";

    @ConfigItem(
            keyName = "highlightTarget",
            name = "Highlight Target",
            description = "Automatically highlight your current target detected by the advanced target management system.",
            position = 1,
            section = targetVisibility
    )
    default boolean highlightTarget()
    {
        return true;
    }

    @ConfigItem(
            keyName = "targetHighlightColor",
            name = "Target Highlight Color",
            description = "Color to use for target highlighting.",
            position = 2,
            section = targetVisibility
    )
    default java.awt.Color targetHighlightColor()
    {
        return java.awt.Color.RED;
    }


    // ===========================================
    // DEFENSIVE PRAYER SWITCHING SECTION
    // ===========================================
    @ConfigSection(
            name = "Defensive Prayer Switching",
            description = "Hotkeys for defensive prayer switching",
            position = 3,
            closedByDefault = true
    )
    String defensivePrayerSwitching = "defensivePrayerSwitching";

    @ConfigItem(
            keyName = "protectFromMagicHotkey",
            name = "Protect from Magic",
            description = "Hotkey to activate Protect from Magic prayer.",
            position = 1,
            section = defensivePrayerSwitching
    )
    default Keybind protectFromMagicHotkey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "protectFromMissilesHotkey",
            name = "Protect from Missiles",
            description = "Hotkey to activate Protect from Missiles prayer.",
            position = 2,
            section = defensivePrayerSwitching
    )
    default Keybind protectFromMissilesHotkey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "protectFromMeleeHotkey",
            name = "Protect from Melee",
            description = "Hotkey to activate Protect from Melee prayer.",
            position = 3,
            section = defensivePrayerSwitching
    )
    default Keybind protectFromMeleeHotkey()
    {
        return Keybind.NOT_SET;
    }


    // ===========================================
    // OFFENSIVE PRAYER SWITCHING SECTION
    // ===========================================
    @ConfigSection(
            name = "Offensive Prayer Switching",
            description = "Automatic prayer switching based on gear equipped",
            position = 4,
            closedByDefault = true
    )
    String offensivePrayerSwitching = "offensivePrayerSwitching";

    @ConfigItem(
            keyName = "prayerSwitchingEnabled",
            name = "Enabled by Default",
            description = "If checked, prayer switching is enabled by default.",
            position = 1,
            section = offensivePrayerSwitching
    )
    default boolean prayerSwitchingEnabled()
    {
        return false;
    }

    @ConfigItem(
            keyName = "prayerSwitchingToggleKey",
            name = "Toggle Key",
            description = "Hotkey to activate this prayer switching profile.",
            position = 2,
            section = offensivePrayerSwitching
    )
    default Keybind prayerSwitchingToggleKey()
    {
        return Keybind.NOT_SET;
    }

    // Melee Setup
    @ConfigItem(
            keyName = "meleePrayer",
            name = "Melee Prayer",
            description = "Select offensive prayer(s) to enable when melee gear is equipped.",
            position = 3,
            section = offensivePrayerSwitching
    )
    default OffensivePrayer meleePrayer()
    {
        return OffensivePrayer.ULTIMATE_STRENGTH;
    }

    @ConfigItem(
            keyName = "meleeGear",
            name = "Melee Gear",
            description = "List of item IDs for the melee gear setup.",
            position = 4,
            section = offensivePrayerSwitching
    )
    default String meleeGear()
    {
        return "";
    }

    // Range Setup
    @ConfigItem(
            keyName = "rangePrayer",
            name = "Range Prayer",
            description = "Select offensive prayer(s) to enable when range gear is equipped.",
            position = 5,
            section = offensivePrayerSwitching
    )
    default OffensivePrayer rangePrayer()
    {
        return OffensivePrayer.EAGLE_EYE;
    }

    @ConfigItem(
            keyName = "rangeGear",
            name = "Range Gear",
            description = "List of item IDs for the range gear setup.",
            position = 6,
            section = offensivePrayerSwitching
    )
    default String rangeGear()
    {
        return "";
    }

    // Magic Setup
    @ConfigItem(
            keyName = "magePrayer",
            name = "Mage Prayer",
            description = "Select offensive prayer(s) to enable when magic gear is equipped.",
            position = 7,
            section = offensivePrayerSwitching
    )
    default OffensivePrayer magePrayer()
    {
        return OffensivePrayer.MYSTIC_MIGHT;
    }

    @ConfigItem(
            keyName = "mageGear",
            name = "Mage Gear",
            description = "List of item IDs for the magic gear setup.",
            position = 8,
            section = offensivePrayerSwitching
    )
    default String mageGear()
    {
        return "";
    }

    // ===========================================
    // HOTKEY PROFILE ONE SECTION
    // ===========================================
    @ConfigSection(
            name = "Hotkey Profile One",
            description = "First hotkey profile configuration",
            position = 5,
            closedByDefault = true
    )
    String hotkeyProfile1 = "hotkeyProfile1";

    @ConfigItem(
            keyName = "enablePvPOne",
            name = "Enable PvP One",
            description = "Allows execution of this config with a hotkey.",
            position = 1,
            section = hotkeyProfile1
    )
    default boolean enablePvPOne()
    {
        return false;
    }

    @ConfigItem(
            keyName = "toggleKey1",
            name = "Toggle Key",
            description = "Hotkey to activate this hotkey profile.",
            position = 2,
            section = hotkeyProfile1
    )
    default Keybind toggleKey1()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "gearToEquip1",
            name = "Gear to Equip",
            description = "List of item IDs to be equipped. Right-click plugin to fill with current gear.",
            position = 3,
            section = hotkeyProfile1
    )
    default String gearToEquip1()
    {
        return "";
    }

    @ConfigItem(
            keyName = "prayersToEnable1",
            name = "Prayers to Enable",
            description = "List of prayers to be enabled.",
            position = 5,
            section = hotkeyProfile1
    )
    default String prayersToEnable1()
    {
        return "";
    }

    @ConfigItem(
            keyName = "spellToCast1",
            name = "Spell to Cast",
            description = "Select the spell you wish to cast.",
            position = 6,
            section = hotkeyProfile1
    )
    default SpellOption spellToCast1()
    {
        return SpellOption.NONE;
    }

    @ConfigItem(
            keyName = "activateSpecialAttack1",
            name = "Activate Special Attack",
            description = "Automatically activate special attack when this profile is used.",
            position = 7,
            section = hotkeyProfile1
    )
    default boolean activateSpecialAttack1()
    {
        return false;
    }

    @ConfigItem(
            keyName = "attackTarget1",
            name = "Attack Target",
            description = "Attack the target or last target.",
            position = 8,
            section = hotkeyProfile1
    )
    default boolean attackTarget1()
    {
        return false;
    }

    // ===========================================
    // HOTKEY PROFILE TWO SECTION
    // ===========================================
    @ConfigSection(
            name = "Hotkey Profile Two",
            description = "Second hotkey profile configuration",
            position = 6,
            closedByDefault = true
    )
    String hotkeyProfile2 = "hotkeyProfile2";

    @ConfigItem(
            keyName = "enablePvPTwo",
            name = "Enable PvP Two",
            description = "Allows execution of this config with a hotkey.",
            position = 1,
            section = hotkeyProfile2
    )
    default boolean enablePvPTwo()
    {
        return false;
    }

    @ConfigItem(
            keyName = "toggleKey2",
            name = "Toggle Key",
            description = "Hotkey to activate this hotkey profile.",
            position = 2,
            section = hotkeyProfile2
    )
    default Keybind toggleKey2()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "gearToEquip2",
            name = "Gear to Equip",
            description = "List of item IDs to be equipped.",
            position = 3,
            section = hotkeyProfile2
    )
    default String gearToEquip2()
    {
        return "";
    }

    @ConfigItem(
            keyName = "prayersToEnable2",
            name = "Prayers to Enable",
            description = "List of prayers to be enabled.",
            position = 5,
            section = hotkeyProfile2
    )
    default String prayersToEnable2()
    {
        return "";
    }

    @ConfigItem(
            keyName = "spellToCast2",
            name = "Spell to Cast",
            description = "Select the spell you wish to cast.",
            position = 6,
            section = hotkeyProfile2
    )
    default SpellOption spellToCast2()
    {
        return SpellOption.NONE;
    }

    @ConfigItem(
            keyName = "activateSpecialAttack2",
            name = "Activate Special Attack",
            description = "Automatically activate special attack when this profile is used.",
            position = 7,
            section = hotkeyProfile2
    )
    default boolean activateSpecialAttack2()
    {
        return false;
    }

    @ConfigItem(
            keyName = "attackTarget2",
            name = "Attack Target",
            description = "Attack the target or last target.",
            position = 8,
            section = hotkeyProfile2
    )
    default boolean attackTarget2()
    {
        return false;
    }

    // ===========================================
    // HOTKEY PROFILE THREE SECTION
    // ===========================================
    @ConfigSection(
            name = "Hotkey Profile Three",
            description = "Third hotkey profile configuration",
            position = 7,
            closedByDefault = true
    )
    String hotkeyProfile3 = "hotkeyProfile3";

    @ConfigItem(
            keyName = "enablePvPThree",
            name = "Enable PvP Three",
            description = "Allows execution of this config with a hotkey.",
            position = 1,
            section = hotkeyProfile3
    )
    default boolean enablePvPThree()
    {
        return false;
    }

    @ConfigItem(
            keyName = "toggleKey3",
            name = "Toggle Key",
            description = "Hotkey to activate this hotkey profile.",
            position = 2,
            section = hotkeyProfile3
    )
    default Keybind toggleKey3()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "gearToEquip3",
            name = "Gear to Equip",
            description = "List of item IDs to be equipped.",
            position = 3,
            section = hotkeyProfile3
    )
    default String gearToEquip3()
    {
        return "";
    }


    @ConfigItem(
            keyName = "prayersToEnable3",
            name = "Prayers to Enable",
            description = "List of prayers to be enabled.",
            position = 5,
            section = hotkeyProfile3
    )
    default String prayersToEnable3()
    {
        return "";
    }

    @ConfigItem(
            keyName = "spellToCast3",
            name = "Spell to Cast",
            description = "Select the spell you wish to cast.",
            position = 6,
            section = hotkeyProfile3
    )
    default SpellOption spellToCast3()
    {
        return SpellOption.NONE;
    }

    @ConfigItem(
            keyName = "activateSpecialAttack3",
            name = "Activate Special Attack",
            description = "Automatically activate special attack when this profile is used.",
            position = 7,
            section = hotkeyProfile3
    )
    default boolean activateSpecialAttack3()
    {
        return false;
    }

    @ConfigItem(
            keyName = "attackTarget3",
            name = "Attack Target",
            description = "Attack the target or last target.",
            position = 8,
            section = hotkeyProfile3
    )
    default boolean attackTarget3()
    {
        return false;
    }

    // ===========================================
    // HOTKEY PROFILE FOUR SECTION
    // ===========================================
    @ConfigSection(
            name = "Hotkey Profile Four",
            description = "Fourth hotkey profile configuration",
            position = 8,
            closedByDefault = true
    )
    String hotkeyProfile4 = "hotkeyProfile4";

    @ConfigItem(
            keyName = "enablePvPFour",
            name = "Enable PvP Four",
            description = "Allows execution of this config with a hotkey.",
            position = 1,
            section = hotkeyProfile4
    )
    default boolean enablePvPFour()
    {
        return false;
    }

    @ConfigItem(
            keyName = "toggleKey4",
            name = "Toggle Key",
            description = "Hotkey to activate this hotkey profile.",
            position = 2,
            section = hotkeyProfile4
    )
    default Keybind toggleKey4()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "gearToEquip4",
            name = "Gear to Equip",
            description = "List of item IDs to be equipped.",
            position = 3,
            section = hotkeyProfile4
    )
    default String gearToEquip4()
    {
        return "";
    }

    @ConfigItem(
            keyName = "prayersToEnable4",
            name = "Prayers to Enable",
            description = "List of prayers to be enabled.",
            position = 5,
            section = hotkeyProfile4
    )
    default String prayersToEnable4()
    {
        return "";
    }

    @ConfigItem(
            keyName = "spellToCast4",
            name = "Spell to Cast",
            description = "Select the spell you wish to cast.",
            position = 6,
            section = hotkeyProfile4
    )
    default SpellOption spellToCast4()
    {
        return SpellOption.NONE;
    }

    @ConfigItem(
            keyName = "activateSpecialAttack4",
            name = "Activate Special Attack",
            description = "Automatically activate special attack when this profile is used.",
            position = 7,
            section = hotkeyProfile4
    )
    default boolean activateSpecialAttack4()
    {
        return false;
    }

    @ConfigItem(
            keyName = "attackTarget4",
            name = "Attack Target",
            description = "Attack the target or last target.",
            position = 8,
            section = hotkeyProfile4
    )
    default boolean attackTarget4()
    {
        return false;
    }

    // ===========================================
    // HOTKEY PROFILE FIVE SECTION
    // ===========================================
    @ConfigSection(
            name = "Hotkey Profile Five",
            description = "Fifth hotkey profile configuration",
            position = 9,
            closedByDefault = true
    )
    String hotkeyProfile5 = "hotkeyProfile5";

    @ConfigItem(
            keyName = "enablePvPFive",
            name = "Enable PvP Five",
            description = "Allows execution of this config with a hotkey.",
            position = 1,
            section = hotkeyProfile5
    )
    default boolean enablePvPFive()
    {
        return false;
    }

    @ConfigItem(
            keyName = "toggleKey5",
            name = "Toggle Key",
            description = "Hotkey to activate this hotkey profile.",
            position = 2,
            section = hotkeyProfile5
    )
    default Keybind toggleKey5()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "gearToEquip5",
            name = "Gear to Equip",
            description = "List of item IDs to be equipped.",
            position = 3,
            section = hotkeyProfile5
    )
    default String gearToEquip5()
    {
        return "";
    }

    @ConfigItem(
            keyName = "prayersToEnable5",
            name = "Prayers to Enable",
            description = "List of prayers to be enabled.",
            position = 5,
            section = hotkeyProfile5
    )
    default String prayersToEnable5()
    {
        return "";
    }

    @ConfigItem(
            keyName = "spellToCast5",
            name = "Spell to Cast",
            description = "Select the spell you wish to cast.",
            position = 6,
            section = hotkeyProfile5
    )
    default SpellOption spellToCast5()
    {
        return SpellOption.NONE;
    }

    @ConfigItem(
            keyName = "activateSpecialAttack5",
            name = "Activate Special Attack",
            description = "Automatically activate special attack when this profile is used.",
            position = 7,
            section = hotkeyProfile5
    )
    default boolean activateSpecialAttack5()
    {
        return false;
    }

    @ConfigItem(
            keyName = "attackTarget5",
            name = "Attack Target",
            description = "Attack the target or last target.",
            position = 8,
            section = hotkeyProfile5
    )
    default boolean attackTarget5()
    {
        return false;
    }

    // ===========================================
    // EXTENDED ACTION DELAYS SECTION
    // ===========================================
    @ConfigSection(
            name = "Extended Action Delays",
            description = "Configure random delays for action execution",
            position = 10,
            closedByDefault = true
    )
    String extendedActionDelays = "extendedActionDelays";

    @ConfigItem(
            keyName = "minimumDelay",
            name = "Minimum Delay",
            description = "Minimum random delay for action execution (milliseconds).",
            position = 1,
            section = extendedActionDelays
    )
    @Range(min = 0, max = 5000)
    default int minimumDelay()
    {
        return 50;
    }

    @ConfigItem(
            keyName = "maximumDelay",
            name = "Maximum Delay",
            description = "Maximum random delay for action execution (milliseconds).",
            position = 2,
            section = extendedActionDelays
    )
    @Range(min = 0, max = 5000)
    default int maximumDelay()
    {
        return 150;
    }

    // ===========================================
    // GENERAL SETTINGS SECTION
    // ===========================================
    @ConfigSection(
            name = "General Settings",
            description = "General plugin settings",
            position = 11,
            closedByDefault = true
    )
    String generalSettings = "generalSettings";

    @ConfigItem(
            keyName = "enableLogging",
            name = "Enable Detailed Logging",
            description = "Show detailed logs in console for debugging",
            position = 1,
            section = generalSettings
    )
    default boolean enableLogging()
    {
        return true;
    }

    // ===========================================
    // SPELL OPTION ENUM (WITH NONE)
    // ===========================================
    enum SpellOption
    {
        NONE("None"),
        WIND_STRIKE("Wind Strike"),
        WATER_STRIKE("Water Strike"),
        EARTH_STRIKE("Earth Strike"),
        FIRE_STRIKE("Fire Strike"),
        WIND_BOLT("Wind Bolt"),
        WATER_BOLT("Water Bolt"),
        EARTH_BOLT("Earth Bolt"),
        FIRE_BOLT("Fire Bolt"),
        WIND_BLAST("Wind Blast"),
        WATER_BLAST("Water Blast"),
        EARTH_BLAST("Earth Blast"),
        FIRE_BLAST("Fire Blast"),
        WIND_WAVE("Wind Wave"),
        WATER_WAVE("Water Wave"),
        EARTH_WAVE("Earth Wave"),
        FIRE_WAVE("Fire Wave"),
        WIND_SURGE("Wind Surge"),
        WATER_SURGE("Water Surge"),
        EARTH_SURGE("Earth Surge"),
        FIRE_SURGE("Fire Surge"),
        ICE_RUSH("Ice Rush"),
        ICE_BURST("Ice Burst"),
        ICE_BLITZ("Ice Blitz"),
        ICE_BARRAGE("Ice Barrage"),
        BLOOD_RUSH("Blood Rush"),
        BLOOD_BURST("Blood Burst"),
        BLOOD_BLITZ("Blood Blitz"),
        BLOOD_BARRAGE("Blood Barrage"),
        SHADOW_RUSH("Shadow Rush"),
        SHADOW_BURST("Shadow Burst"),
        SHADOW_BLITZ("Shadow Blitz"),
        SHADOW_BARRAGE("Shadow Barrage"),
        SMOKE_RUSH("Smoke Rush"),
        SMOKE_BURST("Smoke Burst"),
        SMOKE_BLITZ("Smoke Blitz"),
        SMOKE_BARRAGE("Smoke Barrage");

        private final String spellName;

        SpellOption(String spellName)
        {
            this.spellName = spellName;
        }

        public String getSpellName()
        {
            return spellName;
        }

        public Rs2CombatSpells toCombatSpell()
        {
            if (this == NONE) return null;

            try {
                return Rs2CombatSpells.valueOf(this.name());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public String toString()
        {
            return spellName;
        }
    }

    // ===========================================
    // OFFENSIVE PRAYER ENUM
    // ===========================================
    enum OffensivePrayer
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
    }
}
