package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.pvputilities.enums.SpellType;

@ConfigGroup("pvputilities")
public interface PvPUtilitiesConfig extends Config
{
    @ConfigItem(
        keyName = "enablePlugin",
        name = "Enable PvP Utilities",
        description = "Master switch for PvP Utilities"
    )
    default boolean enablePlugin() { return true; }

    @ConfigSection(name = "Hotkey Equipment", description = "Simple hotkey equipment", position = 1)
    String equipment = "equipment";

    @ConfigItem(keyName="hotkey1", name="Hotkey 1", description = "Hotkey for equipment set 1", section=equipment)
    default Keybind hotkey1() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear1", name="Item IDs 1", description = "Comma-separated item IDs to equip (e.g., 4151,1333)", section=equipment)
    default String gear1() { return ""; }

    @ConfigItem(keyName="hotkey2", name="Hotkey 2", description = "Hotkey for equipment set 2", section=equipment)
    default Keybind hotkey2() { return Keybind.NOT_SET; }

    @ConfigItem(keyName="gear2", name="Item IDs 2", description = "Comma-separated item IDs to equip", section=equipment)
    default String gear2() { return ""; }
}
