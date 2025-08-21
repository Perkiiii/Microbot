package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.Microbot;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PvPUtilitiesScript extends Script {
    public static double version = 1.0;

    @Inject
    private Client client;

    private PvPUtilitiesConfig config;

    public boolean run(PvPUtilitiesConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;

        // Simple main loop - mostly idle
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !config.enablePlugin()) return;
                if (!Microbot.isLoggedIn()) return;
                // Main loop does nothing - everything is hotkey driven
            } catch (Exception ex) {
                Microbot.log("PvP error: " + ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        return true;
    }

    // Execute gear set 1
    public void executeGearSet1() {
        Microbot.log("[PvP] Hotkey 1 pressed - attempting gear set 1");
        equipGearSet(config.gear1());
    }

    // Execute gear set 2
    public void executeGearSet2() {
        Microbot.log("[PvP] Hotkey 2 pressed - attempting gear set 2");
        equipGearSet(config.gear2());
    }

    // Direct equipment using RuneLite API instead of broken Microbot system
    private void equipGearSet(String gearString) {
        if (gearString.isEmpty()) {
            Microbot.log("[PvP] No gear configured for this hotkey");
            return;
        }

        Microbot.log("[PvP] Attempting to equip: " + gearString);

        try {
            // Get inventory directly from RuneLite API
            ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
            if (inventory == null) {
                Microbot.log("[PvP] ERROR: Cannot access inventory");
                return;
            }

            Item[] items = inventory.getItems();
            if (items == null) {
                Microbot.log("[PvP] ERROR: Inventory items are null");
                return;
            }

            // Parse item IDs and attempt to equip each one
            Arrays.stream(gearString.split(","))
                .map(String::trim)
                .forEach(itemIdString -> {
                    try {
                        int itemId = Integer.parseInt(itemIdString);
                        equipItemDirectly(itemId, items);
                    } catch (NumberFormatException e) {
                        Microbot.log("[PvP] Invalid item ID: " + itemIdString);
                    }
                });

        } catch (Exception e) {
            Microbot.log("[PvP] Equipment error: " + e.getMessage());
        }
    }

    // Direct equipment bypassing Microbot entirely
    private void equipItemDirectly(int itemId, Item[] inventoryItems) {
        try {
            // Find the item in inventory
            for (int i = 0; i < inventoryItems.length; i++) {
                Item item = inventoryItems[i];
                if (item.getId() == itemId) {
                    Microbot.log("[PvP] Found item " + itemId + " at slot " + i + ", attempting direct equip");

                    // Use direct RuneLite menu action instead of Microbot
                    client.invokeMenuAction(
                        "Wield",
                        "item",
                        itemId,
                        MenuAction.CC_OP,
                        i,
                        WidgetInfo.INVENTORY.getId()
                    );

                    Microbot.log("[PvP] Equipment command sent for item " + itemId);
                    return;
                }
            }
            Microbot.log("[PvP] Item " + itemId + " not found in inventory");
        } catch (Exception e) {
            Microbot.log("[PvP] Direct equip failed for item " + itemId + ": " + e.getMessage());
        }
    }
}
