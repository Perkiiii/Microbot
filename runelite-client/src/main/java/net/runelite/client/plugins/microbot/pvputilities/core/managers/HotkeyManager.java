package net.runelite.client.plugins.microbot.pvputilities.core.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.pvputilities.PvPUtilitiesConfig;
import net.runelite.client.plugins.microbot.pvputilities.enums.SpellOption;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.api.VarClientInt;

import java.awt.event.KeyEvent;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages all hotkey functionality for PvP utilities
 * Handles defensive prayer hotkeys, prayer switching toggles, profile hotkeys, and walk under
 */
@Slf4j
public class HotkeyManager {

    private final PrayerManager prayerManager;
    private static final ExecutorService HOTKEY_EXECUTOR = Executors.newSingleThreadExecutor();

    // Queues for robust tick-based action processing
    public static final Queue<String> pendingGearActions = new LinkedList<>();
    public static final Queue<Rs2PrayerEnum> pendingPrayerActions = new LinkedList<>();
    private final Random random = new Random();

    // Cooldown tracking for profile execution
    private static final Map<String, Long> profileCooldowns = new ConcurrentHashMap<>();
    private static final long FAILURE_COOLDOWN_MS = 2000; // 2 seconds cooldown after failure

    public HotkeyManager(PrayerManager prayerManager) {
        this.prayerManager = prayerManager;
    }

    /**
     * Handles all hotkey events for the plugin
     */
    public void handleHotkeys(KeyEvent e, PvPUtilitiesConfig config) {
        // Offload all hotkey handling to a background thread to avoid blocking the client thread
        HOTKEY_EXECUTOR.submit(() -> {
            handleDefensivePrayerHotkeys(e, config);
            handlePrayerSwitchingToggle(e, config);
            handleProfileHotkeys(e, config);
            handleWalkUnderHotkey(e, config);
            handleSpecialAttackProfileHotkey(e, config);
        });
    }

    /**
     * Handles defensive prayer hotkeys
     */
    private void handleDefensivePrayerHotkeys(KeyEvent e, PvPUtilitiesConfig config) {
        if (config.protectFromMagicHotkey().matches(e)) {
            boolean currentlyActive = net.runelite.client.plugins.microbot.Microbot.getVarbitValue(Rs2PrayerEnum.PROTECT_MAGIC.getVarbit()) == 1;
            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, !currentlyActive);
            logMessage("Protect from Magic " + (!currentlyActive ? "activated" : "deactivated"), config);
        } else if (config.protectFromMissilesHotkey().matches(e)) {
            boolean currentlyActive = net.runelite.client.plugins.microbot.Microbot.getVarbitValue(Rs2PrayerEnum.PROTECT_RANGE.getVarbit()) == 1;
            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, !currentlyActive);
            logMessage("Protect from Missiles " + (!currentlyActive ? "activated" : "deactivated"), config);
        } else if (config.protectFromMeleeHotkey().matches(e)) {
            boolean currentlyActive = net.runelite.client.plugins.microbot.Microbot.getVarbitValue(Rs2PrayerEnum.PROTECT_MELEE.getVarbit()) == 1;
            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, !currentlyActive);
            logMessage("Protect from Melee " + (!currentlyActive ? "activated" : "deactivated"), config);
        }
    }

    /**
     * Handles prayer switching toggle hotkey
     */
    private void handlePrayerSwitchingToggle(KeyEvent e, PvPUtilitiesConfig config) {
        if (config.prayerSwitchingToggleKey().matches(e)) {
            prayerManager.togglePrayerSwitching();
        }
    }

    /**
     * Handles hotkey profiles 1-5
     */
    private void handleProfileHotkeys(KeyEvent e, PvPUtilitiesConfig config) {
        if (config.enablePvPOne() && config.toggleKey1().matches(e)) {
            executeProfile("Profile 1", config.gearToEquip1(), config.prayersToEnable1(),
                    config.spellToCast1(), config.activateSpecialAttack1(), config.attackTarget1(), config);
        } else if (config.enablePvPTwo() && config.toggleKey2().matches(e)) {
            executeProfile("Profile 2", config.gearToEquip2(), config.prayersToEnable2(),
                    config.spellToCast2(), config.activateSpecialAttack2(), config.attackTarget2(), config);
        } else if (config.enablePvPThree() && config.toggleKey3().matches(e)) {
            executeProfile("Profile 3", config.gearToEquip3(), config.prayersToEnable3(),
                    config.spellToCast3(), config.activateSpecialAttack3(), config.attackTarget3(), config);
        } else if (config.enablePvPFour() && config.toggleKey4().matches(e)) {
            executeProfile("Profile 4", config.gearToEquip4(), config.prayersToEnable4(),
                    config.spellToCast4(), config.activateSpecialAttack4(), config.attackTarget4(), config);
        } else if (config.enablePvPFive() && config.toggleKey5().matches(e)) {
            executeProfile("Profile 5", config.gearToEquip5(), config.prayersToEnable5(),
                    config.spellToCast5(), config.activateSpecialAttack5(), config.attackTarget5(), config);
        }
    }

    /**
     * Handles walk under target hotkey
     */
    private void handleWalkUnderHotkey(KeyEvent e, PvPUtilitiesConfig config) {
        if (config.walkUnderTargetHotkey().matches(e)) {
            executeWalkUnder();
            logMessage("Walk under target executed", config);
        }
    }

    /**
     * Handles the Special Attack Profile hotkey (Profile 6)
     */
    private void handleSpecialAttackProfileHotkey(KeyEvent e, PvPUtilitiesConfig config) {
        if (config.enableSpecialAttackProfile() && config.specialAttackProfileToggleKey().matches(e)) {
            executeSpecialAttackProfile(config);
        }
    }

    /**
     * Executes a hotkey profile with basic functionality
     */
    private void executeProfile(String profileName, String gearToEquip, String prayersToEnable,
                                SpellOption spellToCast,
                                boolean activateSpecialAttack, boolean attackTarget, PvPUtilitiesConfig config) {
        HOTKEY_EXECUTOR.submit(() -> {
            long now = System.currentTimeMillis();
            Long lastFail = profileCooldowns.get(profileName);
            if (lastFail != null && now - lastFail < FAILURE_COOLDOWN_MS) {
                // Still in cooldown, skip execution
                logMessage("[COOLDOWN] Skipping profile '" + profileName + "' due to recent failure.", config);
                return;
            }
            boolean failureOccurred = false;
            try {
                logMessage("Executing " + profileName, config);

                // 1. Activate prayers first (as in 1.4)
                if (prayersToEnable != null && !prayersToEnable.trim().isEmpty()) {
                    var newPrayers = Arrays.stream(prayersToEnable.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.toUpperCase().replace(" ", "_"))
                            .collect(Collectors.toSet());
                    for (String prayerName : newPrayers) {
                        try {
                            Rs2PrayerEnum prayer = Rs2PrayerEnum.valueOf(prayerName);
                            if (!net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.isPrayerActive(prayer)) {
                                net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle(prayer, true);
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                    logMessage("Activated prayers: " + prayersToEnable, config);
                }

                // 2. Equip gear
                boolean gearWasEquipped = false;
                long gearEquipStartTime = 0;
                java.util.List<Integer> equippedItems = new java.util.ArrayList<>();
                if (gearToEquip != null && !gearToEquip.trim().isEmpty()) {
                    gearEquipStartTime = System.currentTimeMillis();
                    String[] items = gearToEquip.split(",");
                    for (String item : items) {
                        String trimmed = item.trim();
                        if (trimmed.isEmpty()) continue;
                        try {
                            int itemId = Integer.parseInt(trimmed);
                            if (net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.hasItem(itemId)) {
                                net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.wield(itemId);
                                equippedItems.add(itemId);
                                try {
                                    int delay;
                                    if (config != null && config.fastGearSwitching()) {
                                        delay = random.nextInt(3) + 1; // 1-3ms
                                    } else {
                                        delay = 8 + random.nextInt(11); // 8-18ms
                                    }
                                    Thread.sleep(delay);
                                } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                            }
                        } catch (NumberFormatException e) {
                            // Optionally handle by name if needed
                        }
                    }
                    gearWasEquipped = true;
                    if (!equippedItems.isEmpty()) {
                        logMessage("Equipped gear: " + equippedItems.stream().map(String::valueOf).collect(Collectors.joining(", ")), config);
                    } else {
                        logMessage("No gear equipped (none found in inventory)", config);
                    }
                }

                // 2.5. Add dynamic delay for weapon readiness if special attack is enabled (as in 1.4)
                if (gearWasEquipped && activateSpecialAttack) {
                    try {
                        long gearEquipTime = System.currentTimeMillis() - gearEquipStartTime;
                        int baseDelay = 150; // Minimum delay for weapon to be ready
                        int dynamicDelay = (int) Math.max(baseDelay, gearEquipTime + 50);
                        Thread.sleep(dynamicDelay);
                        logMessage("Waited " + dynamicDelay + "ms for weapon readiness", config);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // 3. Cast spell if specified, and track if it was cast on the target
                boolean spellCastedOnTarget = false;
                if (spellToCast != null && spellToCast != SpellOption.NONE) {
                    logMessage(profileName + " - Spell casting requested: " + spellToCast.getSpellName(), config);
                    ensureMagicTabOpen();
                    try { Thread.sleep(150); } catch (InterruptedException ignored) {}

                    // Always select the spell first
                    boolean spellSelected = net.runelite.client.plugins.microbot.util.magic.Rs2Magic.cast(spellToCast.getSpell());
                    if (!spellSelected) {
                        logMessage("[WARN] Could not select spell: " + spellToCast.getSpellName(), config);
                        failureOccurred = true;
                    } else if (attackTarget && TargetManager.getCurrentTarget() instanceof net.runelite.api.Player) {
                        // If a valid target exists, attempt to cast on the player
                        net.runelite.api.Player player = (net.runelite.api.Player) TargetManager.getCurrentTarget();
                        boolean spellCastSuccess = net.runelite.client.plugins.microbot.util.magic.Rs2Magic.castOn(
                            spellToCast.getSpell(), player);
                        if (!spellCastSuccess) {
                            logMessage("[WARN] Could not cast spell on player: " + spellToCast.getSpellName(), config);
                            failureOccurred = true;
                        } else {
                            spellCastedOnTarget = true;
                        }
                    } // If no valid target, spell remains selected for manual cast
                }

                // 4. Activate special attack if requested
                if (activateSpecialAttack) {
                    logMessage(profileName + " - Special attack activation requested", config);
                    activateSpecialAttackWithRetry(3, 500, config);
                }

                // 5. Attack target ONLY if we didn't already cast a spell on the target
                if (attackTarget && !spellCastedOnTarget) {
                    logMessage(profileName + " - Target attack requested", config);
                    if (TargetManager.getCurrentTarget() instanceof net.runelite.api.Player) {
                        net.runelite.api.Player player = (net.runelite.api.Player) TargetManager.getCurrentTarget();
                        net.runelite.client.plugins.microbot.util.player.Rs2Player.attack(new net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel(player));
                    } else {
                        logMessage("No valid target to attack.", config);
                        failureOccurred = true;
                    }
                } else if (spellCastedOnTarget) {
                    logMessage("Skipping additional attack - spell was already cast on target", config);
                }
            } finally {
                if (failureOccurred) {
                    profileCooldowns.put(profileName, System.currentTimeMillis());
                }
            }
        });
    }

    /**
     * Executes the Special Attack Profile (Profile 6) with expanded options (no spell logic)
     */
    private void executeSpecialAttackProfile(PvPUtilitiesConfig config) {
        HOTKEY_EXECUTOR.submit(() -> {
            String profileName = "Special Attack Profile";
            long now = System.currentTimeMillis();
            Long lastFail = profileCooldowns.get(profileName);
            if (lastFail != null && now - lastFail < FAILURE_COOLDOWN_MS) {
                logMessage("[COOLDOWN] Skipping profile '" + profileName + "' due to recent failure.", config);
                return;
            }
            boolean failureOccurred = false;
            try {
                logMessage("Executing " + profileName, config);
                // 1. Activate prayers
                String prayersToEnable = config.specialAttackProfilePrayers();
                if (prayersToEnable != null && !prayersToEnable.trim().isEmpty()) {
                    var newPrayers = Arrays.stream(prayersToEnable.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.toUpperCase().replace(" ", "_"))
                            .collect(Collectors.toSet());
                    for (String prayerName : newPrayers) {
                        try {
                            Rs2PrayerEnum prayer = Rs2PrayerEnum.valueOf(prayerName);
                            if (!net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.isPrayerActive(prayer)) {
                                net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle(prayer, true);
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                    logMessage("Activated prayers: " + prayersToEnable, config);
                }
                // 2. Equip gear
                boolean gearWasEquipped = false;
                long gearEquipStartTime = 0;
                String gearToEquip = config.specialAttackProfileGear();
                if (gearToEquip != null && !gearToEquip.trim().isEmpty()) {
                    gearEquipStartTime = System.currentTimeMillis();
                    String[] items = gearToEquip.split(",");
                    for (String item : items) {
                        String trimmed = item.trim();
                        if (trimmed.isEmpty()) continue;
                        try {
                            int itemId = Integer.parseInt(trimmed);
                            if (net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.hasItem(itemId)) {
                                net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.wield(itemId);
                                try {
                                    int delay;
                                    if (config.fastGearSwitching()) {
                                        delay = random.nextInt(3) + 1; // 1-3ms
                                    } else {
                                        delay = 8 + random.nextInt(11); // 8-18ms
                                    }
                                    Thread.sleep(delay);
                                } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                            }
                        } catch (NumberFormatException e) {
                            // Optionally handle by name if needed
                        }
                    }
                    gearWasEquipped = true;
                    logMessage("Equipped gear: " + gearToEquip, config);
                }
                // 2.5. Add dynamic delay for weapon readiness if special attack is enabled
                if (gearWasEquipped) {
                    try {
                        long gearEquipTime = System.currentTimeMillis() - gearEquipStartTime;
                        int baseDelay = 150; // Minimum delay for weapon to be ready
                        int dynamicDelay = (int) Math.max(baseDelay, gearEquipTime + 50);
                        Thread.sleep(dynamicDelay);
                        logMessage("Waited " + dynamicDelay + "ms for weapon readiness", config);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                // 3. Activate special attack (single/double) if requested
                logMessage(profileName + " - Special attack activation requested", config);
                int requiredEnergy = config.specialAttackProfileSpecEnergy();
                PvPUtilitiesConfig.SpecialAttackSpecType specType = config.specialAttackProfileSpecType();
                int maxRetries = 3;
                if (specType == PvPUtilitiesConfig.SpecialAttackSpecType.DOUBLE) {
                    // Double spec: try to activate twice, with delay and energy check
                    boolean first = activateSpecialAttackWithRetry(maxRetries, requiredEnergy, config);
                    if (first) {
                        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                        boolean second = activateSpecialAttackWithRetry(maxRetries, requiredEnergy, config);
                        if (!second) {
                            logMessage("[WARN] Second special attack activation failed.", config);
                            failureOccurred = true;
                        }
                    } else {
                        logMessage("[WARN] First special attack activation failed.", config);
                        failureOccurred = true;
                    }
                } else {
                    boolean single = activateSpecialAttackWithRetry(maxRetries, requiredEnergy, config);
                    if (!single) {
                        logMessage("[WARN] Special attack activation failed.", config);
                        failureOccurred = true;
                    }
                }
                // 4. Attack target if requested
                if (config.specialAttackProfileAttackTarget()) {
                    logMessage(profileName + " - Target attack requested", config);
                    if (TargetManager.getCurrentTarget() instanceof net.runelite.api.Player) {
                        net.runelite.api.Player player = (net.runelite.api.Player) TargetManager.getCurrentTarget();
                        net.runelite.client.plugins.microbot.util.player.Rs2Player.attack(new net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel(player));
                    } else {
                        logMessage("No valid target to attack.", config);
                        failureOccurred = true;
                    }
                }
            } finally {
                if (failureOccurred) {
                    profileCooldowns.put(profileName, System.currentTimeMillis());
                }
            }
        });
    }

    /**
     * Executes walk under functionality
     */
    private void executeWalkUnder() {
        // Use the sophisticated walk under logic from TargetManager
        TargetManager.executeWalkUnder();
    }

    /**
     * Logs a message with plugin prefix
     */
    private void logMessage(String message, PvPUtilitiesConfig config) {
        boolean shouldLog = true;
        try {
            if (config != null) {
                shouldLog = config.enableLogging();
            }
        } catch (Exception ignored) {}
        if (shouldLog) {
            log.info("[PvP Utilities] {}", message);
        }
    }

    /**
     * Enhanced special attack activation with retry logic and state verification
     */
    private boolean activateSpecialAttackWithRetry(int maxRetries, int requiredEnergy, PvPUtilitiesConfig config) {
        final boolean[] result = {false};
        HOTKEY_EXECUTOR.submit(() -> {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    final boolean[] specEnabled = {false};
                    SwingUtilities.invokeAndWait(() -> specEnabled[0] = net.runelite.client.plugins.microbot.util.combat.Rs2Combat.getSpecState());
                    if (specEnabled[0]) {
                        logMessage("Special attack already enabled", config);
                        result[0] = true;
                        return;
                    }
                    final int[] specEnergy = {0};
                    SwingUtilities.invokeAndWait(() -> specEnergy[0] = net.runelite.client.plugins.microbot.util.combat.Rs2Combat.getSpecEnergy());
                    logMessage("[DEBUG] Initial special attack energy: " + (specEnergy[0]/10) + "%", config);

                    // Wait up to 500ms for spec energy to update if it's too low
                    int waitTime = 0;
                    while (specEnergy[0] < requiredEnergy && waitTime < 500) {
                        Thread.sleep(50);
                        waitTime += 50;
                        SwingUtilities.invokeAndWait(() -> specEnergy[0] = net.runelite.client.plugins.microbot.util.combat.Rs2Combat.getSpecEnergy());
                    }
                    logMessage("[DEBUG] Post-wait special attack energy: " + (specEnergy[0]/10) + "% after " + waitTime + "ms", config);

                    if (specEnergy[0] < requiredEnergy) {
                        logMessage("Not enough special attack energy (need " + (requiredEnergy/10) + "%+, have " + (specEnergy[0]/10) + "%)", config);
                        result[0] = false;
                        return;
                    }
                    final boolean[] activated = {false};
                    SwingUtilities.invokeAndWait(() -> activated[0] = net.runelite.client.plugins.microbot.util.combat.Rs2Combat.setSpecState(true, requiredEnergy));
                    if (activated[0]) {
                        Thread.sleep(50);
                        final boolean[] check = {false};
                        SwingUtilities.invokeAndWait(() -> check[0] = net.runelite.client.plugins.microbot.util.combat.Rs2Combat.getSpecState());
                        if (check[0]) {
                            logMessage("Special attack activated", config);
                            result[0] = true;
                            return;
                        }
                    }
                    if (attempt < maxRetries) {
                        logMessage("Special attack activation attempt " + attempt + " failed, retrying...", config);
                        Thread.sleep(75);
                    }
                } catch (Exception e) {
                    logMessage("Special attack attempt " + attempt + " exception: " + e.getMessage(), config);
                    if (attempt < maxRetries) {
                        try { Thread.sleep(100); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                    }
                }
            }
            logMessage("Special attack activation failed after " + maxRetries + " attempts", config);
            result[0] = false;
        });
        return result[0];
    }

    /**
     * Ensures the magic tab is open before casting a spell.
     * Waits up to 500ms for the tab to open.
     */
    private void ensureMagicTabOpen() {
        final int MAGIC_TAB_INDEX = 6; // Standard for RuneLite, adjust if needed
        int maxWait = 500;
        int waitStep = 50;
        int waited = 0;
        if (Microbot.getClient().getVarcIntValue(VarClientInt.INVENTORY_TAB) != MAGIC_TAB_INDEX) {
            Rs2Tab.switchTo(InterfaceTab.MAGIC);
            while (waited < maxWait) {
                if (Microbot.getClient().getVarcIntValue(VarClientInt.INVENTORY_TAB) == MAGIC_TAB_INDEX) {
                    break;
                }
                try { Thread.sleep(waitStep); } catch (InterruptedException ignored) {}
                waited += waitStep;
            }
        }
    }
}

