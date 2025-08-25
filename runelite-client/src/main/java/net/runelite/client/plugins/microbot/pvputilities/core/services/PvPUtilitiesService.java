package net.runelite.client.plugins.microbot.pvputilities.core.services;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.pvputilities.PvPUtilitiesConfig;
import net.runelite.client.plugins.microbot.pvputilities.core.managers.PrayerManager;
import net.runelite.client.plugins.microbot.pvputilities.core.managers.HotkeyManager;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;

import java.util.concurrent.TimeUnit;

/**
 * Main script service for PvP Utilities
 * Handles background tasks and periodic updates
 */
@Slf4j
public class PvPUtilitiesService extends Script {
    public static double version = 1.0;

    private PvPUtilitiesConfig config;
    private PrayerManager prayerManager;

    public PvPUtilitiesService(PrayerManager prayerManager) {
        this.prayerManager = prayerManager;
    }

    public boolean run(PvPUtilitiesConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;

        // Run every game tick (600ms) to check for weapon changes and other periodic tasks
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) return;

            try {
                // Handle automatic prayer switching if enabled
                prayerManager.handleAutomaticPrayerSwitching(config);

                // Process one pending prayer action per tick
                if (!HotkeyManager.pendingPrayerActions.isEmpty()) {
                    Rs2PrayerEnum prayerEnum = HotkeyManager.pendingPrayerActions.peek();
                    if (!Rs2Prayer.isPrayerActive(prayerEnum)) {
                        Rs2Prayer.toggle(prayerEnum, true);
                    }
                    // Remove from queue regardless (if it failed, it will be re-enqueued on next profile)
                    HotkeyManager.pendingPrayerActions.poll();
                }

                // Improved gear switching: equip first item instantly, then others with a short delay (human-like)
                if (!HotkeyManager.pendingGearActions.isEmpty()) {
                    equipPendingGearActionsHumanLike();
                }

                // Future: Add other periodic tasks here (special attack bar, etc.)

            } catch (Exception ex) {
                log.error("PvP Utilities Service Error: {}", ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    // Helper method for human-like gear switching
    // This method must only be called from a background thread (never the client thread)
    private void equipPendingGearActionsHumanLike() {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("[PvP Utilities] ERROR: equipPendingGearActionsHumanLike called from client thread! This will freeze the client.");
            return;
        }
        int delayBetweenItemsMs = 75; // Fast, but human-like
        boolean first = true;
        while (!HotkeyManager.pendingGearActions.isEmpty()) {
            String item = HotkeyManager.pendingGearActions.peek();
            boolean equipped;
            try {
                int itemId = Integer.parseInt(item);
                equipped = net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment.all().anyMatch(eq -> eq.getId() == itemId);
                if (!equipped) {
                    Rs2Inventory.wield(itemId);
                }
            } catch (NumberFormatException e) {
                equipped = net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment.all().anyMatch(eq -> eq.getName().equalsIgnoreCase(item));
                if (!equipped) {
                    Rs2Inventory.wield(item);
                }
            }
            HotkeyManager.pendingGearActions.poll();
            if (first) {
                first = false;
            } else {
                try { Thread.sleep(delayBetweenItemsMs); } catch (InterruptedException ignored) {}
            }
        }
    }
}
