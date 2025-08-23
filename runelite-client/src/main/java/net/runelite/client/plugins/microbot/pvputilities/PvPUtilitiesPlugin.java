package net.runelite.client.plugins.microbot.pvputilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

@PluginDescriptor(
        name = PluginDescriptor.Cicire + "PvP Utilities",
        description = "Advanced PvP utilities with hotkey profiles, prayer switching, and target attacking",
        tags = {"pvp", "hotkeys", "gear", "prayers", "spells", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class PvPUtilitiesPlugin extends Plugin implements KeyListener {

    @Inject
    private PvPUtilitiesConfig config;

    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvPUtilitiesOverlay overlay;

    @Inject
    private PvPUtilitiesScript script;

    private final Random random = new Random();

    // ===== ADVANCED TARGET MANAGEMENT SYSTEM =====
    // Event-driven target detection with smart persistence
    private static Actor currentTarget = null;
    private static Actor lastKnownOpponent = null;
    private static long lastInteractionTime = 0;
    private static boolean inCombat = false;
    private static final long TARGET_PERSISTENCE_MS = 10000; // 10 seconds

    @Provides
    PvPUtilitiesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPUtilitiesConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        keyManager.registerKeyListener(this);

        if (overlayManager != null) {
            overlayManager.add(overlay);
        }

        script.run(config);
        Microbot.log("[PvP Utilities] Advanced Target Management System initialized!");
        Microbot.log("[PvP Utilities] Plugin started successfully!");
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        keyManager.unregisterKeyListener(this);
        overlayManager.remove(overlay);

        // Clear target management state
        clearAllTargetState();
        Microbot.log("[PvP Utilities] Plugin stopped.");
    }

    // ===== EVENT-DRIVEN TARGET DETECTION =====
    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        // Defensive checks to prevent issues with other plugins
        if (!Microbot.isLoggedIn() || event == null) {
            return;
        }

        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return;
            }

            // Additional null checks for event data
            Actor source = event.getSource();
            Actor target = event.getTarget();

            if (source == null) {
                return;
            }

            // Clean up stale targets before processing new interaction
            cleanupStaleTargets();

            // Case 1: Local player starts interacting with someone
            if (source.equals(localPlayer) && target instanceof Player) {
                Player newTarget = (Player) target;
                if (isValidTarget(newTarget)) {
                    setTargetWithPersistence(newTarget, "Player interaction");
                    inCombat = true;
                }
            }
            // Case 2: Someone starts interacting with local player
            else if (target != null && target.equals(localPlayer) && source instanceof Player) {
                Player newTarget = (Player) source;
                if (isValidTarget(newTarget)) {
                    setTargetWithPersistence(newTarget, "Being attacked");
                    inCombat = true;
                }
            }
            // Case 3: Interaction ends (target becomes null)
            else if (source.equals(localPlayer) && target == null) {
                if (currentTarget != null) {
                    logMessage("Interaction ended with " + currentTarget.getName() + " - maintaining persistence");
                    lastInteractionTime = System.currentTimeMillis();
                    inCombat = false;
                }
            }
        } catch (Exception e) {
            // Enhanced error logging to help debug issues
            logMessage("Error in interaction handler: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // Don't let exceptions in our event handler crash other plugins
        }
    }

    // ===== SMART TARGET PERSISTENCE METHODS =====
    private void setTargetWithPersistence(Actor target, String reason) {
        if (isValidTarget(target)) {
            currentTarget = target;
            lastKnownOpponent = target;
            lastInteractionTime = System.currentTimeMillis();
            logMessage("Target set: " + target.getName() + " (" + reason + ")");
        }
    }

    private void cleanupStaleTargets() {
        long currentTime = System.currentTimeMillis();

        // Remove current target if it's no longer valid
        if (currentTarget != null && !isValidTarget(currentTarget)) {
            logMessage("Removing invalid current target: " + currentTarget.getName());
            currentTarget = null;
        }

        // Clear persistence if target has been inactive too long
        if (lastInteractionTime > 0 && (currentTime - lastInteractionTime) > TARGET_PERSISTENCE_MS) {
            if (lastKnownOpponent != null) {
                logMessage("Target persistence expired for: " + lastKnownOpponent.getName());
                lastKnownOpponent = null;
                lastInteractionTime = 0;
                inCombat = false;
            }
        }

        // Remove last known opponent if no longer valid
        if (lastKnownOpponent != null && !isValidTarget(lastKnownOpponent)) {
            logMessage("Removing invalid last known opponent: " + lastKnownOpponent.getName());
            lastKnownOpponent = null;
            lastInteractionTime = 0;
        }
    }

    private void clearAllTargetState() {
        currentTarget = null;
        lastKnownOpponent = null;
        lastInteractionTime = 0;
        inCombat = false;
    }

    // ===== ENHANCED TARGET MANAGEMENT METHODS =====
    /**
     * Multi-priority target selection logic:
     * 1. Current interacting target
     * 2. Current target (if still valid)
     * 3. Last known opponent (within persistence window)
     * 4. Find new target nearby
     */
    private void updateCurrentTarget() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return;
            }

            // Clean up stale targets first
            cleanupStaleTargets();

            // Priority 1: Current interacting target
            if (localPlayer.getInteracting() instanceof Player) {
                Player interactingTarget = (Player) localPlayer.getInteracting();
                if (isValidTarget(interactingTarget)) {
                    setTargetWithPersistence(interactingTarget, "Interacting target");
                    return;
                }
            }

            // Priority 2: Current target (if still valid)
            if (currentTarget != null && isValidTarget(currentTarget)) {
                logMessage("Maintaining current target: " + currentTarget.getName());
                return;
            }

            // Priority 3: Last known opponent (within persistence window)
            if (lastKnownOpponent != null && isValidTarget(lastKnownOpponent)) {
                long timeSinceInteraction = System.currentTimeMillis() - lastInteractionTime;
                if (timeSinceInteraction <= TARGET_PERSISTENCE_MS) {
                    currentTarget = lastKnownOpponent;
                    logMessage("Restored target from persistence: " + lastKnownOpponent.getName() +
                              " (" + (TARGET_PERSISTENCE_MS - timeSinceInteraction) + "ms remaining)");
                    return;
                }
            }

            // Priority 4: Find new target nearby
            Actor newTarget = findBestTarget();
            if (newTarget != null) {
                setTargetWithPersistence(newTarget, "Auto-detected nearby");
            }

        } catch (Exception e) {
            logMessage("Failed to update current target: " + e.getMessage());
        }
    }

    // ===== ENHANCED TARGET MANAGEMENT HOTKEY METHODS =====
    private void executeSetTarget() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return;
            }

            // Try to set from current interacting target first
            if (localPlayer.getInteracting() instanceof Player) {
                Player interactingTarget = (Player) localPlayer.getInteracting();
                if (isValidTarget(interactingTarget)) {
                    setTargetWithPersistence(interactingTarget, "Manual set (interacting)");
                    return;
                }
            }

            // If no interacting target, find nearest valid target
            Actor nearestTarget = findBestTarget();
            if (nearestTarget != null) {
                setTargetWithPersistence(nearestTarget, "Manual set (nearest)");
            } else {
                logMessage("No valid target found to set");
            }

        } catch (Exception e) {
            logMessage("Failed to set target: " + e.getMessage());
        }
    }

    private void executeClearTarget() {
        if (currentTarget != null || lastKnownOpponent != null) {
            String message = "Cleared target state";
            if (currentTarget != null) {
                message += " (current: " + currentTarget.getName() + ")";
            }
            if (lastKnownOpponent != null) {
                message += " (last known: " + lastKnownOpponent.getName() + ")";
            }

            clearAllTargetState();
            logMessage(message);
        } else {
            logMessage("No target to clear");
        }
    }

    // ===== IMPROVED WALK UNDER WITH ADVANCED TARGET SYSTEM =====
    private void executeWalkUnder() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                logMessage("Local player not found");
                return;
            }

            // Priority 1: Use current interacting target (most reliable)
            Player targetPlayer = null;
            if (localPlayer.getInteracting() instanceof Player) {
                targetPlayer = (Player) localPlayer.getInteracting();
                logMessage("Using interacting target: " + targetPlayer.getName());
            }
            // Priority 2: Use advanced target management system as fallback
            else {
                updateCurrentTarget();
                if (currentTarget == null) {
                    currentTarget = findBestTarget();
                }

                if (currentTarget instanceof Player) {
                    targetPlayer = (Player) currentTarget;
                    logMessage("Using current target: " + targetPlayer.getName());
                } else {
                    logMessage("No valid player target available to walk under");
                    return;
                }
            }

            if (targetPlayer == null || !isValidTarget(targetPlayer)) {
                logMessage("Target is invalid or not found");
                return;
            }

            // Get target's world location
            WorldPoint targetLocation = targetPlayer.getWorldLocation();
            if (targetLocation == null) {
                logMessage("Could not get target's world location");
                return;
            }

            // Distance check - should be reasonably close for walk under
            int distance = targetLocation.distanceTo(localPlayer.getWorldLocation());
            if (distance > 10) {
                logMessage("Target too far away for walk under (distance: " + distance + " tiles)");
                return;
            }

            if (distance == 0) {
                logMessage("Already on same tile as target: " + targetPlayer.getName());
                return;
            }

            // Use the simple and reliable Rs2Walker.walkFastCanvas method
            logMessage("Walking under " + targetPlayer.getName() + " at " + targetLocation);
            Rs2Walker.walkFastCanvas(targetLocation);
            logMessage("Walk under command executed successfully");

        } catch (Exception e) {
            logMessage("Error in walk under execution: " + e.getMessage());
        }
    }

    /**
     * Enhanced spell casting with retry logic to handle intermittent failures
     */
    private boolean castSpellWithRetry(PvPUtilitiesConfig.SpellOption spellOption, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                boolean result = castSpell(spellOption);
                if (result) {
                    return true;
                }

                if (attempt < maxRetries) {
                    logMessage("Spell cast attempt " + attempt + " failed, retrying...");
                    Thread.sleep(50); // Brief pause before retry
                }
            } catch (Exception e) {
                logMessage("Spell cast attempt " + attempt + " exception: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(100); // Longer pause after exception
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        logMessage("Spell casting failed after " + maxRetries + " attempts");
        return false;
    }

    /**
     * Enhanced special attack activation with retry logic and state verification
     */
    private boolean activateSpecialAttackWithRetry(int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Check if special attack is already enabled
                if (Rs2Combat.getSpecState()) {
                    logMessage("Special attack already enabled");
                    return true;
                }

                // Improved energy check - some weapons need different amounts
                int specEnergy = Rs2Combat.getSpecEnergy();
                int requiredEnergy = 250; // Default 25%

                // Could be enhanced to check weapon type and adjust required energy
                if (specEnergy < requiredEnergy) {
                    logMessage("Not enough special attack energy (need " + (requiredEnergy/10) + "%+, have " + (specEnergy/10) + "%)");
                    return false;
                }

                // Attempt to activate special attack
                boolean activated = Rs2Combat.setSpecState(true, requiredEnergy);

                if (activated) {
                    // Verify activation with a brief delay
                    try {
                        Thread.sleep(50);
                        if (Rs2Combat.getSpecState()) {
                            logMessage("Special attack activated");
                            return true;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }

                if (attempt < maxRetries) {
                    logMessage("Special attack activation attempt " + attempt + " failed, retrying...");
                    Thread.sleep(75); // Brief pause before retry
                }

            } catch (Exception e) {
                logMessage("Special attack attempt " + attempt + " exception: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        logMessage("Special attack activation failed after " + maxRetries + " attempts");
        return false;
    }

    private boolean equipGear(String gearListConfig) {
        String[] itemIDs = gearListConfig.split("\\s*,\\s*");

        // Pre-fetch inventory contents to avoid repeated lookups
        List<Rs2ItemModel> inventoryItems = Rs2Inventory.items().collect(Collectors.toList());

        // Prepare all valid items first (human-like: mental preparation before rapid clicking)
        List<Integer> validItemIds = new ArrayList<>();
        List<String> validPatterns = new ArrayList<>();

        // Count missing items to reduce log spam
        int missingItemCount = 0;

        for (String itemIdStr : itemIDs) {
            itemIdStr = itemIdStr.trim();
            if (itemIdStr.isEmpty()) {
                continue;
            }

            try {
                // Check for fuzzy match (items with charges)
                if (itemIdStr.endsWith("*")) {
                    String baseId = itemIdStr.substring(0, itemIdStr.length() - 1);
                    for (Rs2ItemModel item : inventoryItems) {
                        if (item != null && String.valueOf(item.getId()).startsWith(baseId)) {
                            validItemIds.add(item.getId());
                            validPatterns.add(itemIdStr);
                            logMessage("Found fuzzy match: " + item.getId() + " for pattern " + itemIdStr);
                            break; // Only take the first match
                        }
                    }
                } else {
                    // Exact match for regular items
                    int itemId = Integer.parseInt(itemIdStr);
                    boolean found = inventoryItems.stream().anyMatch(item -> item != null && item.getId() == itemId);

                    if (found) {
                        validItemIds.add(itemId);
                        validPatterns.add(itemIdStr);
                    } else {
                        missingItemCount++;
                    }
                }
            } catch (NumberFormatException e) {
                logMessage("Invalid item ID: " + itemIdStr);
            }
        }

        // Log missing items summary instead of individual messages
        if (missingItemCount > 0) {
            logMessage("Equipping " + validItemIds.size() + " items (" + missingItemCount + " items not in inventory)");
        } else {
            logMessage("Equipping " + validItemIds.size() + " items");
        }

        // Now execute rapid equipping with human-like timing
        if (!validItemIds.isEmpty()) {
            equipItemsWithHumanTiming(validItemIds, validPatterns);
            return true; // Return true if we equipped any items
        }

        return false; // Return false if no items were equipped
    }

    private void equipItemsWithHumanTiming(List<Integer> itemIds, List<String> patterns) {
        // Human-like: First item is equipped immediately (muscle memory reaction)
        if (!itemIds.isEmpty()) {
            Rs2Inventory.equip(itemIds.get(0));
            logMessage("Equipped item ID: " + itemIds.get(0) + " (pattern: " + patterns.get(0) + ")");
        }

        // Subsequent items with timing based on fast gear switching setting
        for (int i = 1; i < itemIds.size(); i++) {
            try {
                int delay;
                if (config.fastGearSwitching()) {
                    // Fast competitive timing: 1-3ms between rapid clicks
                    delay = 1 + random.nextInt(3); // 1-3ms range
                } else {
                    // Human click timing: 8-18ms between rapid clicks (realistic for skilled PvP players)
                    delay = 8 + random.nextInt(11); // 8-18ms range
                }
                
                Thread.sleep(delay);

                Rs2Inventory.equip(itemIds.get(i));
                logMessage("Equipped item ID: " + itemIds.get(i) + " (pattern: " + patterns.get(i) + ") after " + delay + "ms");

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void activatePrayers(String prayerNames) {
        if (prayerNames == null || prayerNames.trim().isEmpty()) {
            return;
        }

        String[] prayers = prayerNames.split("\\s*,\\s*");
        logMessage("Activating " + prayers.length + " prayers");

        // Prepare all valid prayers first (human-like: mental preparation before rapid clicking)
        List<Rs2PrayerEnum> validPrayers = new ArrayList<>();

        for (String prayerName : prayers) {
            prayerName = prayerName.trim();
            if (prayerName.isEmpty()) {
                continue;
            }

            Rs2PrayerEnum prayer = findPrayerByName(prayerName);
            if (prayer != null) {
                validPrayers.add(prayer);
            } else {
                logMessage("Unknown prayer: " + prayerName);
            }
        }

        // Now execute rapid prayer activation with human-like timing
        if (!validPrayers.isEmpty()) {
            activatePrayersWithHumanTiming(validPrayers);
        }
    }

    private void activatePrayersWithHumanTiming(List<Rs2PrayerEnum> prayers) {
        // Human-like: First prayer is activated immediately (muscle memory reaction)
        if (!prayers.isEmpty()) {
            Rs2PrayerEnum firstPrayer = prayers.get(0);
            // Always activate - let RuneScape handle conflicts automatically
            Rs2Prayer.toggle(firstPrayer);
            logMessage("Activated prayer: " + firstPrayer.getName());
        }

        // Subsequent prayers with timing based on fast gear switching setting
        for (int i = 1; i < prayers.size(); i++) {
            try {
                int delay;
                if (config.fastGearSwitching()) {
                    // Fast competitive timing: 1-3ms between rapid clicks
                    delay = 1 + random.nextInt(3); // 1-3ms range
                } else {
                    // Human click timing: 8-18ms between rapid clicks (realistic for skilled PvP players)
                    delay = 8 + random.nextInt(11); // 8-18ms range
                }

                Thread.sleep(delay);

                Rs2PrayerEnum prayer = prayers.get(i);
                // Always activate - let RuneScape handle conflicts automatically
                Rs2Prayer.toggle(prayer);
                logMessage("Activated prayer: " + prayer.getName() + " after " + delay + "ms");

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean castSpell(PvPUtilitiesConfig.SpellOption spellOption) {
        if (spellOption == null || spellOption == PvPUtilitiesConfig.SpellOption.NONE) {
            logMessage("Spell option is null or NONE, skipping cast");
            return false;
        }

        logMessage("Attempting to cast spell: " + spellOption.getSpellName());

        try {
            Rs2CombatSpells combatSpell = spellOption.toCombatSpell();
            if (combatSpell == null) {
                logMessage("ERROR: Could not convert spell option to combat spell: " + spellOption.getSpellName());
                return false;
            }

            logMessage("Successfully converted to combat spell: " + combatSpell.name());

            // Enhanced spell selection with null checks and error handling
            boolean spellSelected = false;
            try {
                // Ensure magic tab is open first
                if (!Rs2Tab.switchToMagicTab()) {
                    logMessage("Failed to switch to magic tab");
                    return false;
                }

                // Small delay to ensure tab switch completes
                Thread.sleep(50);

                // Attempt to cast the spell with proper error handling
                spellSelected = Rs2Magic.cast(combatSpell);

            } catch (NullPointerException e) {
                logMessage("NULL POINTER in spell casting - spell interface may not be ready");
                return false;
            } catch (Exception e) {
                logMessage("EXCEPTION in castSpell: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                return false;
            }

            if (!spellSelected) {
                logMessage("Failed to select spell in magic interface");
                return false;
            }

            logMessage("Spell selected in magic interface: " + spellOption.getSpellName());

            // Add delay to ensure spell selection is processed
            try {
                Thread.sleep(75); // Slightly longer delay for spell readiness
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            // Now find target and cast on them
            updateCurrentTarget();
            if (currentTarget == null) {
                currentTarget = findBestTarget();
            }

            logMessage("Current target: " + (currentTarget != null ? currentTarget.getName() : "NONE"));

            // If we have a valid target, cast spell on target
            if (currentTarget != null && isValidTarget(currentTarget)) {
                if (currentTarget instanceof Player) {
                    Player playerTarget = (Player) currentTarget;
                    logMessage("Casting " + spellOption.getSpellName() + " on player target: " + playerTarget.getName());

                    // Enhanced target validation
                    if (playerTarget.getWorldLocation() != null &&
                        playerTarget.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) <= 15) {

                        try {
                            // Use Rs2Player.cast() for player targets with error handling
                            boolean castSuccess = Rs2Player.cast(new Rs2PlayerModel(playerTarget));

                            if (castSuccess) {
                                logMessage("Successfully cast spell: " + spellOption.getSpellName() + " on player: " + playerTarget.getName());
                                return true;
                            } else {
                                logMessage("Failed to cast spell on player - Rs2Player.cast returned false");
                                // Try alternative method - attack directly
                                Thread.sleep(100);
                                boolean directClick = Rs2Player.attack(new Rs2PlayerModel(playerTarget));
                                if (directClick) {
                                    logMessage("Successfully targeted player with direct attack: " + playerTarget.getName());
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            logMessage("Exception during player spell casting: " + e.getMessage());
                        }
                    } else {
                        logMessage("Player target is out of range or invalid location");
                    }
                } else {
                    // For NPCs, use the existing Rs2Magic.castOn method
                    logMessage("Casting " + spellOption.getSpellName() + " on NPC target: " + currentTarget.getName());
                    try {
                        boolean castSuccess = Rs2Magic.castOn(combatSpell, currentTarget);

                        if (castSuccess) {
                            logMessage("Successfully cast spell: " + spellOption.getSpellName() + " on NPC: " + currentTarget.getName());
                            return true;
                        } else {
                            logMessage("Failed to cast spell on NPC - Rs2Magic.castOn returned false");
                        }
                    } catch (Exception e) {
                        logMessage("Exception during NPC spell casting: " + e.getMessage());
                    }
                }
            } else {
                logMessage("No valid target found - spell is selected and ready for manual targeting");
                logMessage("You can now click on a target to cast " + spellOption.getSpellName());
                return true; // Consider this success since spell is selected
            }
        } catch (Exception e) {
            logMessage("OUTER EXCEPTION in castSpell: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            if (e instanceof NullPointerException) {
                logMessage("NullPointerException - likely magic interface not ready");
            }
        }

        logMessage("Spell casting failed for: " + spellOption.getSpellName());
        return false;
    }

    private boolean activateSpecialAttack() {
        try {
            // Check if special attack is already enabled
            if (Rs2Combat.getSpecState()) {
                logMessage("Special attack already enabled");
                return true;
            }

            // Check if we have enough special attack energy (25% minimum for most weapons)
            if (Rs2Combat.getSpecEnergy() >= 250) {
                Rs2Combat.setSpecState(true, 250);
                logMessage("Special attack activated");
                return true;
            } else {
                logMessage("Not enough special attack energy (need 25%+)");
            }
        } catch (Exception e) {
            logMessage("Failed to activate special attack: " + e.getMessage());
        }
        return false;
    }

    private void attackLastTarget() {
        // Try to update current target from interacting target first
        updateCurrentTarget();

        // If no current target, try to find the most suitable target nearby
        if (currentTarget == null) {
            currentTarget = findBestTarget();
        }

        if (currentTarget != null && isValidTarget(currentTarget)) {
            try {
                // Check if target is a player or NPC and attack accordingly
                if (currentTarget instanceof Player) {
                    Player playerTarget = (Player) currentTarget;

                    // Enhanced player targeting with multiple fallback methods
                    boolean attackSuccess = attackPlayerWithRetry(playerTarget);

                    if (attackSuccess) {
                        logMessage("Attacked player target: " + currentTarget.getName());
                      } else {
                        logMessage("Failed to attack player after all retry attempts: " + currentTarget.getName());
                      }
                } else {
                    Rs2Npc.attack(currentTarget.getName());
                    logMessage("Attacked NPC target: " + currentTarget.getName());
                }
            } catch (Exception e) {
                logMessage("Failed to attack target: " + e.getMessage());
            }
        } else {
            logMessage("No target available to attack");
        }
    }

    /**
     * Enhanced player attack method with improved accuracy and retry logic
     */
    private boolean attackPlayerWithRetry(Player playerTarget) {
        if (playerTarget == null || !isValidTarget(playerTarget)) {
            return false;
        }

        Rs2PlayerModel playerModel = new Rs2PlayerModel(playerTarget);

        // Method 1: Standard Rs2Player.attack
        try {
            if (Rs2Player.attack(playerModel)) {
                return true;
            }
        } catch (Exception e) {
            logMessage("Standard attack method failed: " + e.getMessage());
        }

        // Method 2: Direct menu interaction with more precise targeting
        try {
            if (attackPlayerDirect(playerTarget)) {
                return true;
            }
        } catch (Exception e) {
            logMessage("Direct attack method failed: " + e.getMessage());
        }

        // Method 3: Camera adjustment + retry
        try {
            // Ensure player is visible on screen
            Rs2Camera.turnTo(playerTarget.getLocalLocation());
            sleep(50, 100); // Brief pause for camera adjustment

            if (Rs2Player.attack(playerModel)) {
                return true;
            }
        } catch (Exception e) {
            logMessage("Camera-adjusted attack failed: " + e.getMessage());
        }

        return false;
    }

    /**
     * Direct player attack using menu interaction for improved accuracy
     */
    private boolean attackPlayerDirect(Player playerTarget) {
        try {
            if (playerTarget == null || playerTarget.getName() == null) {
                return false;
            }

            // Simplified approach - just use Rs2Player.attack as direct method
            // The enhanced retry logic above will handle any failures
            return Rs2Player.attack(new Rs2PlayerModel(playerTarget));

        } catch (Exception e) {
            logMessage("Direct player attack error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get more accurate screen bounds for a player
     */
    private Rectangle getPlayerScreenBounds(Player player) {
        try {
            if (player == null || player.getLocalLocation() == null) {
                return null;
            }

            LocalPoint localPoint = player.getLocalLocation();
            if (localPoint == null) {
                return null;
            }

            // Get the player's screen coordinate
            Point screenPoint = Perspective.localToCanvas(Microbot.getClient(), localPoint,
                Microbot.getClient().getPlane());

            if (screenPoint != null) {
                // Create a reasonable click area around the player (32x32 pixels)
                int size = 32;
                return new Rectangle(
                    screenPoint.getX() - size/2,
                    screenPoint.getY() - size/2,
                    size,
                    size
                );
            }
        } catch (Exception e) {
            logMessage("Error getting player screen bounds: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds the best target to attack based on proximity and attackability
     */
    private Actor findBestTarget() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return null;
            }

            // Look for nearby players that can be attacked using the client's player list
            List<Player> allPlayers = Microbot.getClient().getPlayers();
            if (allPlayers == null || allPlayers.isEmpty()) {
                return null;
            }

            List<Player> nearbyPlayers = allPlayers.stream()
                    .filter(player -> player != null
                            && !player.equals(localPlayer)
                            && isValidTarget(player)
                            && player.getWorldLocation().distanceTo(localPlayer.getWorldLocation()) <= 15)
                    .collect(Collectors.toList());

            if (!nearbyPlayers.isEmpty()) {
                // Return the closest attackable player
                Player closestPlayer = nearbyPlayers.stream()
                        .min((p1, p2) -> Integer.compare(
                                p1.getWorldLocation().distanceTo(localPlayer.getWorldLocation()),
                                p2.getWorldLocation().distanceTo(localPlayer.getWorldLocation())
                        ))
                        .orElse(null);

                if (closestPlayer != null) {
                    logMessage("Found nearby target: " + closestPlayer.getName());
                    return closestPlayer;
                }
            }
        } catch (Exception e) {
            logMessage("Error finding best target: " + e.getMessage());
        }

        return null;
    }

    /**
     * Checks if a target is valid for attacking
     * During persistence window, we're more lenient to maintain targets
     */
    private boolean isValidTarget(Actor target) {
        if (target == null) {
            return false;
        }

        // For players, check if they're attackable
        if (target instanceof Player) {
            Player player = (Player) target;
            Player localPlayer = Microbot.getClient().getLocalPlayer();

            // Basic checks - not ourselves, not null name
            if (localPlayer == null || player.equals(localPlayer) ||
                player.getName() == null || player.getName().trim().isEmpty()) {
                return false;
            }

            // If this is our current target and we're in persistence window, be more lenient
            boolean isCurrentTarget = player.equals(currentTarget) || player.equals(lastKnownOpponent);
            boolean inPersistenceWindow = (System.currentTimeMillis() - lastInteractionTime) <= TARGET_PERSISTENCE_MS;

            if (isCurrentTarget && inPersistenceWindow) {
                // During persistence, only check if player still exists and has a name
                // Don't check health ratio or other strict conditions
                return true;
            }

            // For new targets or outside persistence window, apply stricter checks
            return player.getHealthRatio() > 0 && // Not dead
                   player.getWorldLocation() != null; // Has a valid location
        }

        return true; // For NPCs, basic check is enough
    }

    private Rs2PrayerEnum findPrayerByName(String name) {
        String normalizedName = name.toLowerCase().trim();

        // Direct name matching first
        for (Rs2PrayerEnum prayer : Rs2PrayerEnum.values()) {
            if (prayer.getName().toLowerCase().equals(normalizedName)) {
                return prayer;
            }
        }

        // Partial name matching (for convenience)
        for (Rs2PrayerEnum prayer : Rs2PrayerEnum.values()) {
            if (prayer.getName().toLowerCase().contains(normalizedName)) {
                return prayer;
            }
        }

        return null;
    }

    /**
     * Helper method for logging messages with [PvP] prefix
     */
    private void logMessage(String message) {
        Microbot.log("[PvP] " + message);
    }

    /**
     * Helper method for sleep with random variance
     */
    private void sleep(int minMs, int maxMs) {
        try {
            int sleepTime = minMs + random.nextInt(maxMs - minMs + 1);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Activates a specific defensive prayer, turning off conflicting defensive prayers first.
     * If the prayer is already active, it will be turned off (toggle behavior).
     */
    private void activateDefensivePrayer(Rs2PrayerEnum defensivePrayer) {
        try {
            logMessage("Activating defensive prayer: " + defensivePrayer.getName());

            // Check if the requested prayer is already active
            if (Rs2Prayer.isPrayerActive(defensivePrayer)) {
                // If it's already active, turn it off (toggle behavior)
                Rs2Prayer.toggle(defensivePrayer);
                logMessage("Turned off: " + defensivePrayer.getName());
                return;
            }

            // Turn off other defensive prayers first to avoid conflicts
            Rs2PrayerEnum[] defensivePrayers = {
                Rs2PrayerEnum.PROTECT_MAGIC,
                Rs2PrayerEnum.PROTECT_RANGE,
                Rs2PrayerEnum.PROTECT_MELEE
            };

            for (Rs2PrayerEnum prayer : defensivePrayers) {
                if (prayer != defensivePrayer && Rs2Prayer.isPrayerActive(prayer)) {
                    Rs2Prayer.toggle(prayer);
                    logMessage("Turned off: " + prayer.getName());
                }
            }

            // Small delay to ensure prayers are turned off before activating new one
            Thread.sleep(10);

            // Activate the desired defensive prayer
            Rs2Prayer.toggle(defensivePrayer);
            logMessage("Activated: " + defensivePrayer.getName());

        } catch (Exception e) {
            logMessage("Failed to activate defensive prayer: " + e.getMessage());
        }
    }


    // ===== PUBLIC API FOR OVERLAY INTEGRATION =====
    /**
     * Returns the current target name for overlay display
     */
    public String getCurrentTargetName() {
        if (currentTarget != null && isValidTarget(currentTarget)) {
            return currentTarget.getName();
        }
        return null;
    }

    /**
     * Returns combat status for overlay display
     */
    public boolean isInCombat() {
        return inCombat;
    }

    /**
     * Returns the current target for overlay highlighting
     */
    public Actor getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Returns time remaining on target persistence in milliseconds
     */
    public long getTargetPersistenceRemaining() {
        if (lastKnownOpponent != null && lastInteractionTime > 0) {
            long elapsed = System.currentTimeMillis() - lastInteractionTime;
            return Math.max(0, TARGET_PERSISTENCE_MS - elapsed);
        }
        return 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!Microbot.isLoggedIn()) {
            return;
        }

        // Hotkey Profile One
        if (config.enablePvPOne() && config.toggleKey1().matches(e)) {
            e.consume();
            logMessage("Hotkey Profile One activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeHotkeyProfile(
                    config.gearToEquip1(),
                    config.prayersToEnable1(),
                    config.spellToCast1(),
                    config.activateSpecialAttack1(),
                    config.attackTarget1(),
                    1
                );
                return null;
            });
        }

        // Hotkey Profile Two
        if (config.enablePvPTwo() && config.toggleKey2().matches(e)) {
            e.consume();
            logMessage("Hotkey Profile Two activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeHotkeyProfile(
                    config.gearToEquip2(),
                    config.prayersToEnable2(),
                    config.spellToCast2(),
                    config.activateSpecialAttack2(),
                    config.attackTarget2(),
                    2
                );
                return null;
            });
        }

        // Hotkey Profile Three
        if (config.enablePvPThree() && config.toggleKey3().matches(e)) {
            e.consume();
            logMessage("Hotkey Profile Three activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeHotkeyProfile(
                    config.gearToEquip3(),
                    config.prayersToEnable3(),
                    config.spellToCast3(),
                    config.activateSpecialAttack3(),
                    config.attackTarget3(),
                    3
                );
                return null;
            });
        }

        // Hotkey Profile Four
        if (config.enablePvPFour() && config.toggleKey4().matches(e)) {
            e.consume();
            logMessage("Hotkey Profile Four activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeHotkeyProfile(
                    config.gearToEquip4(),
                    config.prayersToEnable4(),
                    config.spellToCast4(),
                    config.activateSpecialAttack4(),
                    config.attackTarget4(),
                    4
                );
                return null;
            });
        }

        // Hotkey Profile Five
        if (config.enablePvPFive() && config.toggleKey5().matches(e)) {
            e.consume();
            logMessage("Hotkey Profile Five activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeHotkeyProfile(
                    config.gearToEquip5(),
                    config.prayersToEnable5(),
                    config.spellToCast5(),
                    config.activateSpecialAttack5(),
                    config.attackTarget5(),
                    5
                );
                return null;
            });
        }

        // Offensive Prayer Switching Toggle
        if (config.prayerSwitchingToggleKey().matches(e)) {
            e.consume();
            script.togglePrayerSwitching();
        }

        // Walk Under Target Hotkey
        if (config.walkUnderTargetHotkey().matches(e)) {
            e.consume();
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeWalkUnder();
                return null;
            });
        }

        // Defensive Prayer Switching Hotkeys
        if (config.protectFromMagicHotkey().matches(e)) {
            e.consume();
            logMessage("Protect from Magic hotkey activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                activateDefensivePrayer(Rs2PrayerEnum.PROTECT_MAGIC);
                return null;
            });
        }

        if (config.protectFromMissilesHotkey().matches(e)) {
            e.consume();
            logMessage("Protect from Missiles hotkey activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                activateDefensivePrayer(Rs2PrayerEnum.PROTECT_RANGE);
                return null;
            });
        }

        if (config.protectFromMeleeHotkey().matches(e)) {
            e.consume();
            logMessage("Protect from Melee hotkey activated");
            Microbot.getClientThread().runOnSeperateThread(() -> {
                activateDefensivePrayer(Rs2PrayerEnum.PROTECT_MELEE);
                return null;
            });
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed
    }

    private void executeHotkeyProfile(String gearItems, String prayers, PvPUtilitiesConfig.SpellOption spellOption, boolean useSpecialAttack, boolean attackTarget, int profileNumber) {
        logMessage("Executing hotkey profile " + profileNumber);

        // Step 1: Activate prayers
        if (prayers != null && !prayers.trim().isEmpty()) {
            activatePrayers(prayers);
        }

        // Step 2: Equip gear with enhanced timing tracking
        boolean gearWasEquipped = false;
        long gearEquipStartTime = 0;
        if (gearItems != null && !gearItems.trim().isEmpty()) {
            gearEquipStartTime = System.currentTimeMillis();
            gearWasEquipped = equipGear(gearItems);
        }

        // Step 2.5: Enhanced delay for weapon readiness (critical for special attacks)
        if (gearWasEquipped && useSpecialAttack) {
            try {
                // Calculate dynamic delay based on gear switching time
                long gearEquipTime = System.currentTimeMillis() - gearEquipStartTime;
                int baseDelay = 150; // Minimum delay for weapon to be ready
                int dynamicDelay = (int) Math.max(baseDelay, gearEquipTime + 50);

                Thread.sleep(dynamicDelay);
                logMessage("Waited " + dynamicDelay + "ms for weapon readiness");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return; // Exit cleanly if interrupted
            }
        }

        // Step 3: Cast spell with enhanced error handling and retry logic
        boolean spellCasted = false;
        boolean spellCastedOnTarget = false;
        if (spellOption != null && spellOption != PvPUtilitiesConfig.SpellOption.NONE) {
            spellCasted = castSpellWithRetry(spellOption, 2); // Retry up to 2 times

            // Check if we have a current target to determine if spell was cast on target
            if (spellCasted && currentTarget != null && isValidTarget(currentTarget)) {
                spellCastedOnTarget = true;
                logMessage("Spell cast directly on target - skipping additional attack");
            }

            // Wait for spell to be selected before proceeding
            if (spellCasted) {
                try {
                    Thread.sleep(100); // Give time for spell to be selected
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // Step 4: Activate special attack with enhanced reliability
        boolean specialActivated = false;
        if (useSpecialAttack) {
            specialActivated = activateSpecialAttackWithRetry(3); // Retry up to 3 times

            // Wait for special attack to be activated before proceeding
            if (specialActivated) {
                try {
                    Thread.sleep(75); // Slightly longer wait for special attack state
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // Step 5: Attack target ONLY if we didn't already cast a spell on the target
        if (attackTarget && !spellCastedOnTarget) {
            attackLastTarget();
        } else if (spellCastedOnTarget) {
            logMessage("Skipping additional attack - spell was already cast on target");
        }

        logMessage("Hotkey profile " + profileNumber + " execution completed");
    }
}
