package net.runelite.client.plugins.microbot.pvputilities.core.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.plugins.microbot.pvputilities.PvPUtilitiesConfig;
import net.runelite.client.plugins.microbot.pvputilities.enums.OffensivePrayer;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;

/**
 * Manages prayer-related functionality for PvP utilities
 * Handles automatic offensive prayer switching based on equipped gear
 * Note: Hotkey handling is now managed by HotkeyManager
 */
@Slf4j
public class PrayerManager {

    private boolean prayerSwitchingEnabled = false;
    private int lastWeaponId = -1;
    private long lastPrayerActivationTime = 0;
    private static final long PRAYER_ACTIVATION_COOLDOWN = 1000; // 1 second cooldown

    /**
     * Toggles prayer switching on/off
     */
    public void togglePrayerSwitching() {
        prayerSwitchingEnabled = !prayerSwitchingEnabled;

        if (prayerSwitchingEnabled) {
            logMessage("Automatic prayer switching ENABLED");
            lastWeaponId = -1; // Reset to force prayer check
        } else {
            logMessage("Automatic prayer switching DISABLED");
            turnOffAllOffensivePrayers();
            lastWeaponId = -1; // Reset weapon tracking
        }
    }

    /**
     * Returns whether prayer switching is currently active
     */
    public boolean isPrayerSwitchingActive() {
        return prayerSwitchingEnabled;
    }

    /**
     * Handles automatic prayer switching based on equipped gear
     */
    public void handleAutomaticPrayerSwitching(PvPUtilitiesConfig config) {
        // Only run if prayer switching is enabled
        if (!prayerSwitchingEnabled || !config.prayerSwitchingEnabled()) {
            return;
        }

        // Get currently equipped weapon
        Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        int currentWeaponId = (equippedWeapon != null) ? equippedWeapon.getId() : -1;

        // Only process if weapon changed (optimization)
        if (currentWeaponId == lastWeaponId) {
            return;
        }

        lastWeaponId = currentWeaponId;

        if (currentWeaponId == -1) {
            logMessage("No weapon equipped");
            return;
        }

        logMessage("Weapon changed to ID: " + currentWeaponId);

        // Check which combat style this weapon belongs to
        String weaponIdStr = String.valueOf(currentWeaponId);
        OffensivePrayer prayerToActivate = null;
        String combatStyle = null;

        // Check melee weapons
        if (isWeaponInGearList(weaponIdStr, config.meleeGear())) {
            prayerToActivate = config.meleePrayer();
            combatStyle = "Melee";
        }
        // Check range weapons
        else if (isWeaponInGearList(weaponIdStr, config.rangeGear())) {
            prayerToActivate = config.rangePrayer();
            combatStyle = "Range";
        }
        // Check magic weapons
        else if (isWeaponInGearList(weaponIdStr, config.mageGear())) {
            prayerToActivate = config.magePrayer();
            combatStyle = "Magic";
        }

        if (prayerToActivate != null && combatStyle != null) {
            logMessage("Detected " + combatStyle + " weapon - activating " + prayerToActivate.getPrayerName());
            activateOffensivePrayer(prayerToActivate);
        } else {
            logMessage("Weapon ID " + currentWeaponId + " not found in any configured gear lists");
        }
    }

    /**
     * Checks if a weapon ID is in the specified gear list
     */
    private boolean isWeaponInGearList(String weaponId, String gearListConfig) {
        if (gearListConfig == null || gearListConfig.trim().isEmpty()) {
            return false;
        }

        String[] configuredIds = gearListConfig.split("\\s*,\\s*");
        for (String configuredId : configuredIds) {
            configuredId = configuredId.trim();

            // Check for fuzzy match (items with charges)
            if (configuredId.endsWith("*")) {
                String baseId = configuredId.substring(0, configuredId.length() - 1);
                if (weaponId.startsWith(baseId)) {
                    return true;
                }
            }
            // Check for exact match
            else if (weaponId.equals(configuredId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Activates the specified offensive prayer
     */
    private void activateOffensivePrayer(OffensivePrayer offensivePrayer) {
        if (offensivePrayer == null) {
            return;
        }

        // Check cooldown to prevent spam
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPrayerActivationTime < PRAYER_ACTIVATION_COOLDOWN) {
            return;
        }

        try {
            // Turn off all other offensive prayers first to avoid conflicts
            turnOffAllOffensivePrayers();

            // Activate the new prayer
            Rs2PrayerEnum prayerEnum = offensivePrayer.toPrayerEnum();
            if (prayerEnum != null) {
                Rs2Prayer.toggle(prayerEnum, true);
                lastPrayerActivationTime = currentTime;
                logMessage("Activated offensive prayer: " + offensivePrayer.getPrayerName());
            }

        } catch (Exception e) {
            logMessage("Error activating offensive prayer: " + e.getMessage());
        }
    }

    /**
     * Turns off all offensive prayers to prevent conflicts
     */
    private void turnOffAllOffensivePrayers() {
        try {
            // Turn off all major offensive prayers using correct constant names
            Rs2Prayer.toggle(Rs2PrayerEnum.BURST_STRENGTH, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.SUPERHUMAN_STRENGTH, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.ULTIMATE_STRENGTH, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.CHIVALRY, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.PIETY, false);

            Rs2Prayer.toggle(Rs2PrayerEnum.SHARP_EYE, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.HAWK_EYE, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.EAGLE_EYE, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.RIGOUR, false);

            Rs2Prayer.toggle(Rs2PrayerEnum.MYSTIC_WILL, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.MYSTIC_LORE, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.MYSTIC_MIGHT, false);
            Rs2Prayer.toggle(Rs2PrayerEnum.AUGURY, false);

        } catch (Exception e) {
            logMessage("Error turning off offensive prayers: " + e.getMessage());
        }
    }

    /**
     * Gets the current weapon ID from equipment
     */
    public int getCurrentWeaponId() {
        try {
            Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
            return (equippedWeapon != null) ? equippedWeapon.getId() : -1;
        } catch (Exception e) {
            logMessage("Error getting current weapon ID: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Forces a prayer check regardless of weapon change
     */
    public void forcePrayerCheck(PvPUtilitiesConfig config) {
        lastWeaponId = -1; // Reset to force check
        handleAutomaticPrayerSwitching(config);
    }

    /**
     * Logs a message if logging is enabled
     */
    private void logMessage(String message) {
        log.info("[PvP Utilities] {}", message);
    }
}
