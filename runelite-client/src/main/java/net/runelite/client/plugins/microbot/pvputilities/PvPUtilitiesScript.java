package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.api.EquipmentInventorySlot;

import java.util.concurrent.TimeUnit;

public class PvPUtilitiesScript extends Script {
    public static double version = 1.0;

    private boolean prayerSwitchingEnabled = false;
    private int lastWeaponId = -1;
    private PvPUtilitiesConfig config;
    private long lastPrayerActivationTime = 0;
    private static final long PRAYER_ACTIVATION_COOLDOWN = 1000; // 1 second cooldown

    public boolean run(PvPUtilitiesConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;

        // Run every game tick (600ms) to check for weapon changes
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) return;

            try {
                // Handle automatic prayer switching if enabled
                if (prayerSwitchingEnabled && config.prayerSwitchingEnabled()) {
                    handleAutomaticPrayerSwitching();
                }
            } catch (Exception ex) {
                Microbot.log("PvP Utilities Script Error: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    public void togglePrayerSwitching() {
        prayerSwitchingEnabled = !prayerSwitchingEnabled;

        if (prayerSwitchingEnabled) {
            Microbot.log("[PvP] Automatic prayer switching ENABLED");
            // Reset to force prayer check, but don't call handleAutomaticPrayerSwitching directly
            // Let the scheduled executor handle it on the next tick
            lastWeaponId = -1;
        } else {
            Microbot.log("[PvP] Automatic prayer switching DISABLED");
            // Turn off all offensive prayers when disabled (also run this safely)
            Microbot.getClientThread().runOnSeperateThread(() -> {
                turnOffAllOffensivePrayers();
                return null;
            });
            lastWeaponId = -1; // Reset weapon tracking
        }
    }

    /**
     * Returns whether prayer switching is currently active
     * @return true if prayer switching is active, false otherwise
     */
    public boolean isPrayerSwitchingActive() {
        return prayerSwitchingEnabled;
    }

    private void handleAutomaticPrayerSwitching() {
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
        PvPUtilitiesConfig.OffensivePrayer prayerToActivate = null;
        String combatStyle = null;

        // Debug logging for gear lists
        logMessage("Checking weapon ID " + weaponIdStr + " against gear lists:");
        logMessage("  Melee gear: '" + config.meleeGear() + "'");
        logMessage("  Range gear: '" + config.rangeGear() + "'");
        logMessage("  Mage gear: '" + config.mageGear() + "'");

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

    private boolean isWeaponInGearList(String weaponId, String gearListConfig) {
        if (gearListConfig == null || gearListConfig.trim().isEmpty()) {
            return false;
        }

        String[] configuredIds = gearListConfig.split("\\s*,\\s*");
        for (String configuredId : configuredIds) {
            configuredId = configuredId.trim();

            // Check for fuzzy match (items with charges)
            if (configuredId.endsWith("*")) {
                // Remove the * and check if weapon ID starts with the base ID
                String baseId = configuredId.substring(0, configuredId.length() - 1);
                if (weaponId.startsWith(baseId)) {
                    logMessage("Fuzzy match found: weapon " + weaponId + " matches pattern " + configuredId);
                    return true;
                }
            } else {
                // Exact match for regular items
                if (configuredId.equals(weaponId)) {
                    logMessage("Exact match found: weapon " + weaponId);
                    return true;
                }
            }
        }
        return false;
    }

    private void activateOffensivePrayer(PvPUtilitiesConfig.OffensivePrayer offensivePrayer) {
        Rs2PrayerEnum prayer = convertOffensivePrayer(offensivePrayer);
        if (prayer != null) {
            logMessage("Attempting to activate prayer: " + prayer.getName());

            // Check current prayer status
            boolean isCurrentlyActive = Rs2Prayer.isPrayerActive(prayer);
            logMessage("Prayer " + prayer.getName() + " current status: " + (isCurrentlyActive ? "active" : "inactive"));

            // Only activate if not already active
            if (!isCurrentlyActive) {
                // Check cooldown
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPrayerActivationTime >= PRAYER_ACTIVATION_COOLDOWN) {
                    logMessage("Activating prayer: " + prayer.getName());
                    // Directly activate the prayer - this will automatically turn off conflicting prayers
                    Rs2Prayer.toggle(prayer, true);
                    logMessage("Prayer activation command sent for: " + prayer.getName());
                    lastPrayerActivationTime = currentTime; // Update last activation time
                } else {
                    logMessage("Prayer activation on cooldown. Please wait.");
                }
            } else {
                logMessage("Prayer " + prayer.getName() + " is already active - no action needed");
            }
        } else {
            logMessage("Could not find matching prayer for: " + offensivePrayer.getPrayerName());
        }
    }

    private Rs2PrayerEnum convertOffensivePrayer(PvPUtilitiesConfig.OffensivePrayer offensivePrayer) {
        switch (offensivePrayer) {
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

    private void logMessage(String message) {
        if (config != null && config.enableLogging()) {
            Microbot.log("[PvP Prayer] " + message);
        }
    }

    private void turnOffAllOffensivePrayers() {
        // List of all offensive prayers that should be turned off
        Rs2PrayerEnum[] allOffensivePrayers = {
            Rs2PrayerEnum.BURST_STRENGTH,
            Rs2PrayerEnum.SUPERHUMAN_STRENGTH,
            Rs2PrayerEnum.ULTIMATE_STRENGTH,
            Rs2PrayerEnum.SHARP_EYE,
            Rs2PrayerEnum.HAWK_EYE,
            Rs2PrayerEnum.EAGLE_EYE,
            Rs2PrayerEnum.MYSTIC_WILL,
            Rs2PrayerEnum.MYSTIC_LORE,
            Rs2PrayerEnum.MYSTIC_MIGHT,
            Rs2PrayerEnum.CHIVALRY,
            Rs2PrayerEnum.PIETY,
            Rs2PrayerEnum.RIGOUR,
            Rs2PrayerEnum.AUGURY
        };

        for (Rs2PrayerEnum prayer : allOffensivePrayers) {
            if (Rs2Prayer.isPrayerActive(prayer)) {
                Rs2Prayer.toggle(prayer, false);
                logMessage("Turned off offensive prayer: " + prayer.getName());
            }
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
