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
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
        if (!Microbot.isLoggedIn()) {
            return;
        }

        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return;
            }

            // Clean up stale targets before processing new interaction
            cleanupStaleTargets();

            // Case 1: Local player starts interacting with someone
            if (event.getSource().equals(localPlayer) && event.getTarget() instanceof Player) {
                Player newTarget = (Player) event.getTarget();
                if (isValidTarget(newTarget)) {
                    setTargetWithPersistence(newTarget, "Player interaction");
                    inCombat = true;
                }
            }
            // Case 2: Someone starts interacting with local player
            else if (event.getTarget() != null && event.getTarget().equals(localPlayer) &&
                     event.getSource() instanceof Player) {
                Player newTarget = (Player) event.getSource();
                if (isValidTarget(newTarget)) {
                    setTargetWithPersistence(newTarget, "Being attacked");
                    inCombat = true;
                }
            }
            // Case 3: Interaction ends (target becomes null)
            else if (event.getSource().equals(localPlayer) && event.getTarget() == null) {
                if (currentTarget != null) {
                    logMessage("Interaction ended with " + currentTarget.getName() + " - maintaining persistence");
                    lastInteractionTime = System.currentTimeMillis();
                    inCombat = false;
                }
            }
        } catch (Exception e) {
            logMessage("Error in interaction handler: " + e.getMessage());
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
        // Use advanced target management to get best available target
        updateCurrentTarget();

        if (currentTarget != null && isValidTarget(currentTarget)) {
            if (currentTarget instanceof Player) {
                Rs2Player.walkUnder(new Rs2PlayerModel((Player) currentTarget));
                logMessage("Walking under target: " + currentTarget.getName());
            } else {
                logMessage("Target is not a player, cannot walk under");
            }
        } else {
            logMessage("No valid target available to walk under");
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

        // Set Target Hotkey
        if (config.setTargetHotkey().matches(e)) {
            e.consume();
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeSetTarget();
                return null;
            });
        }

        // Clear Target Hotkey
        if (config.clearTargetHotkey().matches(e)) {
            e.consume();
            Microbot.getClientThread().runOnSeperateThread(() -> {
                executeClearTarget();
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

        // Step 2: Equip gear
        if (gearItems != null && !gearItems.trim().isEmpty()) {
            equipGear(gearItems);
        }

        // Step 3: Cast spell (only if not NONE) and wait for it to be selected
        boolean spellCasted = false;
        if (spellOption != null && spellOption != PvPUtilitiesConfig.SpellOption.NONE) {
            spellCasted = castSpell(spellOption);

            // Wait for spell to be selected before proceeding
            if (spellCasted) {
                try {
                    Thread.sleep(100); // Give time for spell to be selected
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Step 4: Activate special attack if enabled (before attacking)
        boolean specialActivated = false;
        if (useSpecialAttack) {
            specialActivated = activateSpecialAttack();

            // Wait for special attack to be activated before proceeding
            if (specialActivated) {
                try {
                    Thread.sleep(50); // Give time for special attack to activate
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Step 5: Attack target AFTER spell/special attack with proper target validation
        if (attackTarget) {
            attackLastTarget();
        }

        logMessage("Hotkey profile " + profileNumber + " execution completed");
    }

    private void equipGear(String gearListConfig) {
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
        }
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
                // Always add prayers to activation list, regardless of current state
                validPrayers.add(prayer);

                if (Rs2Prayer.isPrayerActive(prayer)) {
                    logMessage("Prayer already active: " + prayer.getName());
                }
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
            if (!Rs2Prayer.isPrayerActive(firstPrayer)) {
                Rs2Prayer.toggle(firstPrayer);
                logMessage("Activated prayer: " + firstPrayer.getName());
            } else {
                logMessage("Prayer already active (ensured): " + firstPrayer.getName());
            }
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
                if (!Rs2Prayer.isPrayerActive(prayer)) {
                    Rs2Prayer.toggle(prayer);
                    logMessage("Activated prayer: " + prayer.getName() + " after " + delay + "ms");
                } else {
                    logMessage("Prayer already active (ensured): " + prayer.getName() + " after " + delay + "ms");
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean castSpell(PvPUtilitiesConfig.SpellOption spellOption) {
        if (spellOption == null || spellOption == PvPUtilitiesConfig.SpellOption.NONE) {
            return false;
        }

        logMessage("Attempting to cast spell: " + spellOption.getSpellName());

        try {
            Rs2CombatSpells combatSpell = spellOption.toCombatSpell();
            if (combatSpell != null) {
                // First try to find or update target
                updateCurrentTarget();
                if (currentTarget == null) {
                    currentTarget = findBestTarget();
                }

                // If we have a valid target, cast spell on target directly
                if (currentTarget != null && isValidTarget(currentTarget)) {
                    Rs2Magic.castOn(combatSpell.getMagicAction(), currentTarget);
                    logMessage("Cast spell: " + spellOption.getSpellName() + " on target: " + currentTarget.getName());
                    return true;
                } else {
                    // Fallback: cast spell without target (selects spell for manual targeting)
                    Rs2Magic.cast(combatSpell);
                    logMessage("Cast spell: " + spellOption.getSpellName() + " (no target - manual targeting)");
                    return true;
                }
            } else {
                logMessage("Could not convert spell option to combat spell: " + spellOption.getSpellName());
            }
        } catch (Exception e) {
            logMessage("Failed to cast spell " + spellOption.getSpellName() + ": " + e.getMessage());
        }
        return false;
    }

    private boolean activateSpecialAttack() {
        try {
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
                    Rs2Player.attack(new Rs2PlayerModel((Player) currentTarget));
                    logMessage("Attacked player target: " + currentTarget.getName());
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
     */
    private boolean isValidTarget(Actor target) {
        if (target == null) {
            return false;
        }

        // For players, check if they're attackable
        if (target instanceof Player) {
            Player player = (Player) target;
            Player localPlayer = Microbot.getClient().getLocalPlayer();

            // Basic checks - not ourselves, not null name, still alive
            return localPlayer != null
                    && !player.equals(localPlayer)
                    && player.getName() != null
                    && !player.getName().trim().isEmpty()
                    && player.getHealthRatio() > 0; // Not dead
        }

        return true; // For NPCs, basic check is enough
    }

    private Rs2PrayerEnum findPrayerByName(String name) {
        String normalizedName = name.toLowerCase().trim();

        for (Rs2PrayerEnum prayer : Rs2PrayerEnum.values()) {
            String prayerName = prayer.getName().toLowerCase();

            // Direct match
            if (prayerName.equals(normalizedName)) {
                return prayer;
            }
        }

        // If no direct match found, try common variations and shortcuts
        for (Rs2PrayerEnum prayer : Rs2PrayerEnum.values()) {
            String prayerName = prayer.getName().toLowerCase();

            // Common variations and shortcuts
            if (normalizedName.equals("deadeye") && prayerName.contains("sharp eye")) {
                return prayer;
            }
            if (normalizedName.equals("mystic vigour") && prayerName.contains("mystic will")) {
                return prayer;
            }
            if (normalizedName.equals("protect magic") && prayerName.contains("protect from magic")) {
                return prayer;
            }
            if (normalizedName.equals("protect range") && prayerName.contains("protect from missiles")) {
                return prayer;
            }
            if (normalizedName.equals("protect melee") && prayerName.contains("protect from melee")) {
                return prayer;
            }
        }

        return null;
    }

    private void logMessage(String message) {
        if (config.enableLogging()) {
            Microbot.log("[PvP] " + message);
        }
    }

    /**
     * Returns whether prayer switching is currently active
     */
    public boolean isPrayerSwitchingActive() {
        return script.isPrayerSwitchingActive();
    }

    // Prayer switching functionality
    private void handleOffensivePrayerSwitching() {
        if (!config.prayerSwitchingEnabled()) {
            logMessage("Prayer switching is disabled in config");
            return;
        }

        // Get currently equipped items
        List<Rs2ItemModel> equippedItems = Rs2Equipment.items();
        if (equippedItems.isEmpty()) {
            logMessage("No items equipped - cannot determine prayer style");
            return;
        }

        // Get equipped item IDs as strings for comparison
        List<String> equippedItemIds = equippedItems.stream()
                .map(item -> String.valueOf(item.getId()))
                .collect(Collectors.toList());

        logMessage("Currently equipped items: " + String.join(",", equippedItemIds));

        // Check which gear setup matches current equipment
        String meleeGear = config.meleeGear().trim();
        String rangeGear = config.rangeGear().trim();
        String mageGear = config.mageGear().trim();

        PvPUtilitiesConfig.OffensivePrayer prayerToActivate = null;
        String gearType = "Unknown";

        // Check for melee gear match
        if (!meleeGear.isEmpty() && hasMatchingGear(equippedItemIds, meleeGear)) {
            prayerToActivate = config.meleePrayer();
            gearType = "Melee";
        }
        // Check for range gear match
        else if (!rangeGear.isEmpty() && hasMatchingGear(equippedItemIds, rangeGear)) {
            prayerToActivate = config.rangePrayer();
            gearType = "Range";
        }
        // Check for mage gear match
        else if (!mageGear.isEmpty() && hasMatchingGear(equippedItemIds, mageGear)) {
            prayerToActivate = config.magePrayer();
            gearType = "Mage";
        }

        if (prayerToActivate != null) {
            logMessage("Detected " + gearType + " gear setup - activating " + prayerToActivate.getPrayerName());

            // Convert OffensivePrayer to Rs2PrayerEnum and activate
            Rs2PrayerEnum prayer = convertOffensivePrayer(prayerToActivate);
            if (prayer != null) {
                if (!Rs2Prayer.isPrayerActive(prayer)) {
                    Rs2Prayer.toggle(prayer);
                    logMessage("Activated prayer: " + prayer.getName());
                } else {
                    logMessage("Prayer already active: " + prayer.getName());
                }
            } else {
                logMessage("Could not find matching prayer for: " + prayerToActivate.getPrayerName());
            }
        } else {
            logMessage("No matching gear setup found for current equipment");
            logMessage("Melee gear config: " + meleeGear);
            logMessage("Range gear config: " + rangeGear);
            logMessage("Mage gear config: " + mageGear);
        }
    }

    private boolean hasMatchingGear(List<String> equippedItemIds, String configuredGear) {
        if (configuredGear == null || configuredGear.trim().isEmpty()) {
            return false;
        }

        String[] configuredItemIds = configuredGear.split("\\s*,\\s*");

        // Check if any of the configured gear items are currently equipped
        for (String configuredId : configuredItemIds) {
            configuredId = configuredId.trim();
            if (configuredId.isEmpty()) continue;

            if (equippedItemIds.contains(configuredId)) {
                logMessage("Found matching gear item: " + configuredId);
                return true;
            }
        }

        return false;
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

    // ===== STATIC METHODS FOR COMPATIBILITY =====
    public static boolean validTarget() {
        return currentTarget != null;
    }

    public static Actor getTarget() {
        return currentTarget;
    }

    public static void setTarget(Actor target) {
        currentTarget = target;
    }

    public static void clearTarget() {
        currentTarget = null;
    }

}
