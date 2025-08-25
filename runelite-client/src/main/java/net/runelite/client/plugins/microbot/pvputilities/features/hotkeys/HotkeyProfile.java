package net.runelite.client.plugins.microbot.pvputilities.features.hotkeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.client.plugins.microbot.pvputilities.enums.SpellOption;

/**
 * Data class representing a hotkey profile configuration
 */
@Data
@AllArgsConstructor
public class HotkeyProfile {
    private final String gearToEquip;
    private final String prayersToEnable;
    private final SpellOption spellToCast;
    private final boolean activateSpecialAttack;
    private final boolean attackTarget;
}
