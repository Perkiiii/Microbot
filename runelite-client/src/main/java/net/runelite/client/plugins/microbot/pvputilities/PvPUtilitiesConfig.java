package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.pvputilities.enums.SpellType;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;

@ConfigGroup("pvputilities")
public interface PvPUtilitiesConfig extends Config
{
    @ConfigItem(
        keyName = "enablePlugin",
        name = "Enable PvP Utilities",
        description = "Master switch for PvP Utilities"
    )
    default boolean enablePlugin() { return true; }

    // ================= Profiles 1–5 =================

    @ConfigSection(name = "Profile 1", description = "PvP Profile 1", position = 1)
    String profile1 = "profile1";

    @ConfigItem(keyName = "enableProfile1", name = "Enable", description = "Enable Profile 1", section = profile1)
    default boolean enableProfile1() { return false; }

    @ConfigItem(keyName="hotkey1", name="Hotkey", description = "Hotkey for Profile 1", section=profile1)
    default Keybind hotkey1() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear1", name="Gear (IDs/names)", description = "Comma-separated list of item IDs or names", section=profile1)
    default String gear1() { return ""; }

    @ConfigItem(keyName="prayers1", name="Prayers", description = "Prayers to activate", section=profile1)
    default Rs2PrayerEnum[] prayers1() { return new Rs2PrayerEnum[]{}; }

    @ConfigItem(keyName="spell1", name="Spell", description = "Spell to cast", section=profile1)
    default SpellType spell1() { return SpellType.NONE; }

    @ConfigItem(keyName="spec1", name="Use Spec", description = "Use special attack", section=profile1)
    default boolean useSpec1() { return false; }

    @ConfigItem(keyName="specThreshold1", name="Spec Threshold (250/500/1000)", description = "Minimum spec energy required", section=profile1)
    default int specThreshold1() { return 500; }

    @ConfigItem(keyName="attack1", name="Attack After", description = "Attack target after actions", section=profile1)
    default boolean attack1() { return false; }

    // -------- Profile 2 --------
    @ConfigSection(name = "Profile 2", description = "PvP Profile 2", position = 2)
    String profile2 = "profile2";

    @ConfigItem(keyName="enableProfile2", name="Enable", description = "Enable Profile 2", section=profile2)
    default boolean enableProfile2() { return false; }

    @ConfigItem(keyName="hotkey2", name="Hotkey", description = "Hotkey for Profile 2", section=profile2)
    default Keybind hotkey2() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear2", name="Gear (IDs/names)", description = "Comma-separated list of item IDs or names", section=profile2)
    default String gear2() { return ""; }

    @ConfigItem(keyName="prayers2", name="Prayers", description = "Prayers to activate", section=profile2)
    default Rs2PrayerEnum[] prayers2() { return new Rs2PrayerEnum[]{}; }

    @ConfigItem(keyName="spell2", name="Spell", description = "Spell to cast", section=profile2)
    default SpellType spell2() { return SpellType.NONE; }

    @ConfigItem(keyName="spec2", name="Use Spec", description = "Use special attack", section=profile2)
    default boolean useSpec2() { return false; }

    @ConfigItem(keyName="specThreshold2", name="Spec Threshold", description = "Minimum spec energy required", section=profile2)
    default int specThreshold2() { return 500; }

    @ConfigItem(keyName="attack2", name="Attack After", description = "Attack target after actions", section=profile2)
    default boolean attack2() { return false; }

    // -------- Profile 3 --------
    @ConfigSection(name = "Profile 3", description = "PvP Profile 3", position = 3)
    String profile3 = "profile3";

    @ConfigItem(keyName="enableProfile3", name="Enable", description = "Enable Profile 3", section=profile3)
    default boolean enableProfile3() { return false; }

    @ConfigItem(keyName="hotkey3", name="Hotkey", description = "Hotkey for Profile 3", section=profile3)
    default Keybind hotkey3() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear3", name="Gear (IDs/names)", description = "Comma-separated list of item IDs or names", section=profile3)
    default String gear3() { return ""; }

    @ConfigItem(keyName="prayers3", name="Prayers", description = "Prayers to activate", section=profile3)
    default Rs2PrayerEnum[] prayers3() { return new Rs2PrayerEnum[]{}; }

    @ConfigItem(keyName="spell3", name="Spell", description = "Spell to cast", section=profile3)
    default SpellType spell3() { return SpellType.NONE; }

    @ConfigItem(keyName="spec3", name="Use Spec", description = "Use special attack", section=profile3)
    default boolean useSpec3() { return false; }

    @ConfigItem(keyName="specThreshold3", name="Spec Threshold", description = "Minimum spec energy required", section=profile3)
    default int specThreshold3() { return 500; }

    @ConfigItem(keyName="attack3", name="Attack After", description = "Attack target after actions", section=profile3)
    default boolean attack3() { return false; }

    // -------- Profile 4 --------
    @ConfigSection(name = "Profile 4", description = "PvP Profile 4", position = 4)
    String profile4 = "profile4";

    @ConfigItem(keyName="enableProfile4", name="Enable", description = "Enable Profile 4", section=profile4)
    default boolean enableProfile4() { return false; }

    @ConfigItem(keyName="hotkey4", name="Hotkey", description = "Hotkey for Profile 4", section=profile4)
    default Keybind hotkey4() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear4", name="Gear (IDs/names)", description = "Comma-separated list of item IDs or names", section=profile4)
    default String gear4() { return ""; }

    @ConfigItem(keyName="prayers4", name="Prayers", description = "Prayers to activate", section=profile4)
    default Rs2PrayerEnum[] prayers4() { return new Rs2PrayerEnum[]{}; }

    @ConfigItem(keyName="spell4", name="Spell", description = "Spell to cast", section=profile4)
    default SpellType spell4() { return SpellType.NONE; }

    @ConfigItem(keyName="spec4", name="Use Spec", description = "Use special attack", section=profile4)
    default boolean useSpec4() { return false; }

    @ConfigItem(keyName="specThreshold4", name="Spec Threshold", description = "Minimum spec energy required", section=profile4)
    default int specThreshold4() { return 500; }

    @ConfigItem(keyName="attack4", name="Attack After", description = "Attack target after actions", section=profile4)
    default boolean attack4() { return false; }

    // -------- Profile 5 --------
    @ConfigSection(name = "Profile 5", description = "PvP Profile 5", position = 5)
    String profile5 = "profile5";

    @ConfigItem(keyName="enableProfile5", name="Enable", description = "Enable Profile 5", section=profile5)
    default boolean enableProfile5() { return false; }

    @ConfigItem(keyName="hotkey5", name="Hotkey", description = "Hotkey for Profile 5", section=profile5)
    default Keybind hotkey5() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear5", name="Gear (IDs/names)", description = "Comma-separated list of item IDs or names", section=profile5)
    default String gear5() { return ""; }

    @ConfigItem(keyName="prayers5", name="Prayers", description = "Prayers to activate", section=profile5)
    default Rs2PrayerEnum[] prayers5() { return new Rs2PrayerEnum[]{}; }

    @ConfigItem(keyName="spell5", name="Spell", description = "Spell to cast", section=profile5)
    default SpellType spell5() { return SpellType.NONE; }

    @ConfigItem(keyName="spec5", name="Use Spec", description = "Use special attack", section=profile5)
    default boolean useSpec5() { return false; }

    @ConfigItem(keyName="specThreshold5", name="Spec Threshold", description = "Minimum spec energy required", section=profile5)
    default int specThreshold5() { return 500; }

    @ConfigItem(keyName="attack5", name="Attack After", description = "Attack target after actions", section=profile5)
    default boolean attack5() { return false; }
}
