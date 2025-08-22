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
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.Actor;
import net.runelite.api.Player;

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
    private ConfigManager configManager;

    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvPUtilitiesOverlay overlay;

    @Inject
    private PvPUtilitiesScript script;

    private Random random = new Random();
    private Actor lastTarget = null;
    private static Actor walkUnderTarget = null;
    private boolean walkUnderEnabled = false;

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
        Microbot.log("[PvP Utilities] Plugin started successfully!");
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        keyManager.unregisterKeyListener(this);
        overlayManager.remove(overlay);

        Microbot.log("[PvP Utilities] Plugin stopped.");
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

        // Store current target for potential attack (do this safely without blocking)
        try {
            Actor currentTarget = Rs2Player.getInteracting();
            if (currentTarget != null) {
                lastTarget = currentTarget;
            }
        } catch (Exception ex) {
            // If we can't get the current target, just use the last known target
            logMessage("Could not get current target: " + ex.getMessage());
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
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed
    }

    private void executeHotkeyProfile(String gearItems, String prayers, PvPUtilitiesConfig.SpellOption spellOption, boolean useSpecialAttack, boolean attackTarget, int profileNumber) {
        logMessage("Executing hotkey profile " + profileNumber);

        // Apply random delay before actions
        applyRandomDelay();

        // Step 1: Equip gear
        if (gearItems != null && !gearItems.trim().isEmpty()) {
            equipGear(gearItems);
        }

        // Step 2: Activate prayers
        if (prayers != null && !prayers.trim().isEmpty()) {
            activatePrayers(prayers);
        }

        // Step 3: Cast spell (only if not NONE)
        if (spellOption != null && spellOption != PvPUtilitiesConfig.SpellOption.NONE) {
            castSpell(spellOption);
        }

        // Step 4: Activate special attack if enabled
        if (useSpecialAttack) {
            activateSpecialAttack();
        }

        // Step 5: Attack target if enabled
        if (attackTarget) {
            attackLastTarget();
        }

        logMessage("Hotkey profile " + profileNumber + " execution completed");
    }

    private void equipGear(String gearListConfig) {
        String[] itemIDs = gearListConfig.split("\\s*,\\s*");
        logMessage("Equipping " + itemIDs.length + " items");

        // Prepare all valid items first (human-like: mental preparation before rapid clicking)
        List<Integer> validItemIds = new ArrayList<>();
        List<String> validPatterns = new ArrayList<>();

        for (String itemIdStr : itemIDs) {
            itemIdStr = itemIdStr.trim();
            if (itemIdStr.isEmpty()) {
                continue;
            }

            try {
                // Check for fuzzy match (items with charges)
                if (itemIdStr.endsWith("*")) {
                    String baseId = itemIdStr.substring(0, itemIdStr.length() - 1);
                    List<Rs2ItemModel> inventoryItems = Rs2Inventory.items().collect(Collectors.toList());
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
                    if (Rs2Inventory.contains(itemId)) {
                        validItemIds.add(itemId);
                        validPatterns.add(itemIdStr);
                    } else {
                        logMessage("Item ID " + itemId + " not found in inventory");
                    }
                }
            } catch (NumberFormatException e) {
                logMessage("Invalid item ID: " + itemIdStr);
            }
        }

        // Now execute rapid equipping with human-like timing
        if (!validItemIds.isEmpty()) {
            equipItemsWithHumanTiming(validItemIds, validPatterns);
        }
    }

    private void equipItemsWithHumanTiming(List<Integer> itemIds, List<String> patterns) {
        Random random = new Random();

        // Human-like: First item is equipped immediately (muscle memory reaction)
        if (!itemIds.isEmpty()) {
            Rs2Inventory.equip(itemIds.get(0));
            logMessage("Equipped item ID: " + itemIds.get(0) + " (pattern: " + patterns.get(0) + ")");
        }

        // Subsequent items with human-like rapid clicking (8-18ms intervals)
        for (int i = 1; i < itemIds.size(); i++) {
            try {
                // Human click timing: 8-18ms between rapid clicks (realistic for skilled PvP players)
                int humanDelay = 8 + random.nextInt(11); // 8-18ms range
                Thread.sleep(humanDelay);

                Rs2Inventory.equip(itemIds.get(i));
                logMessage("Equipped item ID: " + itemIds.get(i) + " (pattern: " + patterns.get(i) + ") after " + humanDelay + "ms");

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
        List<String> validPrayerNames = new ArrayList<>();

        for (String prayerName : prayers) {
            prayerName = prayerName.trim();
            if (prayerName.isEmpty()) {
                continue;
            }

            Rs2PrayerEnum prayer = findPrayerByName(prayerName);
            if (prayer != null) {
                if (!Rs2Prayer.isPrayerActive(prayer)) {
                    validPrayers.add(prayer);
                    validPrayerNames.add(prayerName);
                } else {
                    logMessage("Prayer already active: " + prayer.getName());
                }
            } else {
                logMessage("Unknown prayer: " + prayerName);
            }
        }

        // Now execute rapid prayer activation with human-like timing
        if (!validPrayers.isEmpty()) {
            activatePrayersWithHumanTiming(validPrayers, validPrayerNames);
        }
    }

    private void activatePrayersWithHumanTiming(List<Rs2PrayerEnum> prayers, List<String> prayerNames) {
        Random random = new Random();

        // Human-like: First prayer is activated immediately (muscle memory reaction)
        if (!prayers.isEmpty()) {
            Rs2Prayer.toggle(prayers.get(0));
            logMessage("Activated prayer: " + prayers.get(0).getName());
        }

        // Subsequent prayers with human-like rapid clicking (8-18ms intervals)
        for (int i = 1; i < prayers.size(); i++) {
            try {
                // Human click timing: 8-18ms between rapid clicks (realistic for skilled PvP players)
                int humanDelay = 8 + random.nextInt(11); // 8-18ms range
                Thread.sleep(humanDelay);

                Rs2Prayer.toggle(prayers.get(i));
                logMessage("Activated prayer: " + prayers.get(i).getName() + " after " + humanDelay + "ms");

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void castSpell(PvPUtilitiesConfig.SpellOption spellOption) {
        if (spellOption == null || spellOption == PvPUtilitiesConfig.SpellOption.NONE) {
            return;
        }

        logMessage("Attempting to cast spell: " + spellOption.getSpellName());

        try {
            Rs2CombatSpells combatSpell = spellOption.toCombatSpell();
            if (combatSpell != null) {
                Rs2Magic.cast(combatSpell);
                logMessage("Cast spell: " + spellOption.getSpellName());
            } else {
                logMessage("Could not convert spell option to combat spell: " + spellOption.getSpellName());
            }
        } catch (Exception e) {
            logMessage("Failed to cast spell " + spellOption.getSpellName() + ": " + e.getMessage());
        }
    }

    private void activateSpecialAttack() {
        try {
            // Check if we have enough special attack energy (25% minimum for most weapons)
            if (Rs2Combat.getSpecEnergy() >= 250) {
                Rs2Combat.setSpecState(true, 250);
                logMessage("Special attack activated");
            } else {
                logMessage("Not enough special attack energy (need 25%+)");
            }
        } catch (Exception e) {
            logMessage("Failed to activate special attack: " + e.getMessage());
        }
    }

    private void attackLastTarget() {
        if (lastTarget != null) {
            try {
                Rs2Npc.attack(lastTarget.getName());
                logMessage("Attacked target: " + lastTarget.getName());
            } catch (Exception e) {
                logMessage("Failed to attack target: " + e.getMessage());
            }
        } else {
            logMessage("No target available to attack");
        }
    }

    private Rs2PrayerEnum findPrayerByName(String name) {
        String normalizedName = name.toLowerCase().trim();

        for (Rs2PrayerEnum prayer : Rs2PrayerEnum.values()) {
            String prayerName = prayer.getName().toLowerCase();

            // Direct match
            if (prayerName.equals(normalizedName)) {
                return prayer;
            }

            // Common variations and shortcuts
            if (normalizedName.equals("deadeye") && prayerName.contains("sharp eye")) {
                return prayer;
            }
            if (normalizedName.equals("mystic vigour") && prayerName.contains("mystic will")) {
                return prayer;
            }
            if (normalizedName.equals("incredible reflexes") && prayerName.contains("incredible reflexes")) {
                return prayer;
            }
            if (normalizedName.equals("ultimate strength") && prayerName.contains("ultimate strength")) {
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
            if (normalizedName.equals("piety") && prayerName.contains("piety")) {
                return prayer;
            }
            if (normalizedName.equals("rigour") && prayerName.contains("rigour")) {
                return prayer;
            }
            if (normalizedName.equals("augury") && prayerName.contains("augury")) {
                return prayer;
            }
            if (normalizedName.equals("eagle eye") && prayerName.contains("eagle eye")) {
                return prayer;
            }
            if (normalizedName.equals("mystic might") && prayerName.contains("mystic might")) {
                return prayer;
            }
        }

        return null;
    }

    private void applyRandomDelay() {
        int minDelay = config.minimumDelay();
        int maxDelay = config.maximumDelay();

        if (maxDelay > minDelay) {
            int delay = random.nextInt(maxDelay - minDelay) + minDelay;
            try {
                Thread.sleep(delay);
                logMessage("Applied random delay: " + delay + "ms");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void logMessage(String message) {
        if (config.enableLogging()) {
            Microbot.log("[PvP] " + message);
        }
    }

    /**
     * Returns whether prayer switching is currently active
     * @return true if prayer switching is active, false otherwise
     */
    public boolean isPrayerSwitchingActive() {
        return script.isPrayerSwitchingActive();
    }

    // Restored methods that were accidentally removed during cleanup
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

    private void toggleWalkUnder() {
        walkUnderEnabled = !walkUnderEnabled;
        if (walkUnderEnabled) {
            logMessage("Walk under target enabled");
            // Set walkUnderTarget to the current target if available
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer != null && localPlayer.getInteracting() != null) {
                walkUnderTarget = localPlayer.getInteracting();
                logMessage("Walk under target set to: " + walkUnderTarget.getName());
            } else {
                logMessage("No target available to walk under");
            }
        } else {
            logMessage("Walk under target disabled");
            walkUnderTarget = null;
        }
    }

    private void performWalkUnder() {
        if (walkUnderTarget != null) {
            try {
                // Walk to the target's current position
                Rs2Walker.walkTo(walkUnderTarget.getWorldLocation());
                logMessage("Walking under target: " + walkUnderTarget.getName());
            } catch (Exception e) {
                logMessage("Failed to walk under target: " + e.getMessage());
            }
        } else {
            logMessage("No walk under target set");
        }
    }

    private void executeWalkUnder() {
        if (!config.walkUnderTarget()) {
            logMessage("Walk under target is disabled in config");
            return;
        }

        // Get the current target similar to Bradley Combat logic
        Actor target = getValidTarget();
        if (target != null && target.getLocalLocation() != null && target.getLocalLocation().isInScene()) {
            // Use Bradley Combat's walkUnder logic - simulate walking to the target's position
            if (target instanceof Player) {
                Rs2Player.walkUnder(Rs2Player.getPlayer(target.getName()));
                logMessage("Walking under target: " + target.getName());
            } else {
                logMessage("Target is not a player, cannot walk under");
            }
        } else {
            logMessage("No valid target available to walk under");
        }
    }

    private Actor getValidTarget() {
        // First try to get the current interacting target
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer != null && localPlayer.getInteracting() != null) {
            return localPlayer.getInteracting();
        }

        // Fall back to the last known target
        return lastTarget;
    }

    // Static methods for target management similar to Bradley Combat
    public static Actor getWalkUnderTarget() {
        return walkUnderTarget;
    }

    public static void setWalkUnderTarget(Actor target) {
        walkUnderTarget = target;
    }

    public static boolean isValidWalkUnderTarget() {
        return walkUnderTarget != null &&
               walkUnderTarget.getLocalLocation() != null &&
               walkUnderTarget.getLocalLocation().isInScene();
    }
}
