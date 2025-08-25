package net.runelite.client.plugins.microbot.pvputilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pvputilities.core.handlers.ChatInputHandler;
import net.runelite.client.plugins.microbot.pvputilities.core.handlers.InteractionHandler;
import net.runelite.client.plugins.microbot.pvputilities.core.managers.HotkeyManager;
import net.runelite.client.plugins.microbot.pvputilities.core.managers.PrayerManager;
import net.runelite.client.plugins.microbot.pvputilities.core.managers.TargetManager;
import net.runelite.client.plugins.microbot.pvputilities.core.services.PvPUtilitiesService;
import net.runelite.client.plugins.microbot.pvputilities.ui.overlays.PvPUtilitiesOverlay;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Main plugin class for PvP Utilities
 * Coordinates all managers, handlers, and services
 */
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
    private net.runelite.api.Client client;

    @Inject
    private ClientThread clientThread;

    // Core managers and handlers
    private HotkeyManager hotkeyManager;
    private PrayerManager prayerManager;
    private InteractionHandler interactionHandler;
    private ChatInputHandler chatInputHandler;
    private PvPUtilitiesService service;

    private static final java.util.Map<String, String> TOGGLE_MESSAGES = new java.util.HashMap<>();
    static {
        TOGGLE_MESSAGES.put("attackAfterAction", "Attack After an Action");
        TOGGLE_MESSAGES.put("disableUnusedPrayers", "Disable Unused Prayers");
        TOGGLE_MESSAGES.put("showToggleMessages", "Show Toggle Feature Messages");
        TOGGLE_MESSAGES.put("alwaysShowSpecBar", "Always Show Spec Bar");
        TOGGLE_MESSAGES.put("safeCastSpells", "Safe Cast Spells");
        TOGGLE_MESSAGES.put("fastGearSwitching", "Fast Gear Switching");
        TOGGLE_MESSAGES.put("walkUnderTarget", "Walk Under Target");
        TOGGLE_MESSAGES.put("highlightTarget", "Highlight Target");
        TOGGLE_MESSAGES.put("prayerSwitchingEnabled", "Offensive Prayer Switching Enabled by Default");
        TOGGLE_MESSAGES.put("enablePvPOne", "Enable PvP One");
        TOGGLE_MESSAGES.put("enablePvPTwo", "Enable PvP Two");
        TOGGLE_MESSAGES.put("enablePvPThree", "Enable PvP Three");
        TOGGLE_MESSAGES.put("enablePvPFour", "Enable PvP Four");
        TOGGLE_MESSAGES.put("enablePvPFive", "Enable PvP Five");
        TOGGLE_MESSAGES.put("enableSpecialAttackProfile", "Enable Special Attack Profile");
        // Only main tickboxes and Enable PvP for each profile
    }

    @Provides
    PvPUtilitiesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPUtilitiesConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        // Initialize managers and handlers
        prayerManager = new PrayerManager();
        hotkeyManager = new HotkeyManager(prayerManager);
        interactionHandler = new InteractionHandler();
        chatInputHandler = new ChatInputHandler();
        service = new PvPUtilitiesService(prayerManager);

        // Register listeners and overlays
        keyManager.registerKeyListener(this);
        if (overlayManager != null) {
            overlayManager.add(overlay);
        }

        // Start the background service
        service.run(config);

        log.info("[PvP Utilities] Advanced Target Management System initialized!");
        log.info("[PvP Utilities] Plugin started successfully!");
    }

    @Override
    protected void shutDown() {
        // Shutdown service
        if (service != null) {
            service.shutdown();
        }

        // Unregister listeners and overlays
        keyManager.unregisterKeyListener(this);
        if (overlayManager != null) {
            overlayManager.remove(overlay);
        }

        // Clear all state
        TargetManager.clearAllTargetState();
        log.info("[PvP Utilities] Plugin stopped.");
    }

    // ===== EVENT HANDLERS =====

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        interactionHandler.onInteractingChanged(event);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        chatInputHandler.updateChatInputState();
        // Clean up stale targets every tick to remove dead or invalid targets
        TargetManager.cleanupStaleTargets();

        // Always Show Spec Bar feature
        if (config.alwaysShowSpecBar()) {
            int groupId = 593;
            int[] specBarChildren = {37, 38, 39, 40, 42, 43};
            for (int childId : specBarChildren) {
                Widget w = client.getWidget(groupId, childId);
                if (w != null) {
                    w.setHidden(false);
                }
            }
        }
    }

    @Subscribe
    public void onConfigChanged(net.runelite.client.events.ConfigChanged event) {
        if (!"pvputilities".equals(event.getGroup())) {
            return;
        }
        boolean showMessages = config.showToggleMessages();
        if (!showMessages) {
            return;
        }
        String key = event.getKey();
        if (TOGGLE_MESSAGES.containsKey(key)) {
            boolean enabled = false;
            try {
                java.lang.reflect.Method m = config.getClass().getMethod(key);
                Object result = m.invoke(config);
                if (result instanceof Boolean) {
                    enabled = (Boolean) result;
                }
            } catch (Exception e) {
                // fallback: do not show message if reflection fails
                return;
            }
            String prefix = "<col=00ffff>[PvP Utilities]</col> ";
            String message = prefix + TOGGLE_MESSAGES.get(key) + ": " + (enabled ? "ON" : "OFF");
            clientThread.invokeLater(() -> client.addChatMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, "", message, null));
        }
    }

    // ===== KEY LISTENER IMPLEMENTATION =====

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!Microbot.isLoggedIn() || chatInputHandler.isChatActive()) {
            return; // Don't process hotkeys while typing in chat
        }

        try {
            // Handle all hotkeys through the hotkey manager
            hotkeyManager.handleHotkeys(e, config);

            // Handle walk under target hotkey
            if (config.walkUnderTargetHotkey().matches(e)) {
                clientThread.invokeLater(this::handleWalkUnderTarget);
            }

        } catch (Exception ex) {
            log.error("Error handling key press: {}", ex.getMessage());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    // ===== UTILITY METHODS =====

    private void handleWalkUnderTarget() {
        if (!config.walkUnderTarget()) return;

        Actor target = TargetManager.getBestAvailableTarget();
        if (target != null) {
            // Implementation for walking under target
            // This would use Rs2Walker to move to target's location
            log.debug("Walk under target requested for: {}", target.getName());
        }
    }

    // ===== PUBLIC API FOR BACKWARD COMPATIBILITY =====

    /**
     * Gets the current target (for backward compatibility with overlay)
     */
    public Actor getCurrentTarget() {
        return TargetManager.getCurrentTarget();
    }

    /**
     * Logs a message if logging is enabled
     */
    public void logMessage(String message) {
        if (config.enableLogging()) {
            log.info("[PvP Utilities] {}", message);
        }
    }

    /**
     * Toggles prayer switching (public API for right-click menu)
     */
    public void togglePrayerSwitching() {
        prayerManager.togglePrayerSwitching();
    }

    /**
     * Gets attack after action setting
     */
    public boolean shouldAttackAfterAction() {
        return config.attackAfterAction();
    }

    /**
     * Gets disable unused prayers setting
     */
    public boolean shouldDisableUnusedPrayers() {
        return config.disableUnusedPrayers();
    }

    /**
     * Gets show toggle messages setting
     */
    public boolean shouldShowToggleMessages() {
        return config.showToggleMessages();
    }

    /**
     * Gets always show spec bar setting
     */
    public boolean shouldAlwaysShowSpecBar() {
        return config.alwaysShowSpecBar();
    }

    /**
     * Gets safe cast spells setting
     */
    public boolean shouldSafeCastSpells() {
        return config.safeCastSpells();
    }

    /**
     * Gets fast gear switching setting
     */
    public boolean shouldUseFastGearSwitching() {
        return config.fastGearSwitching();
    }

    /**
     * Gets minimum delay setting
     */
    public int getMinimumDelay() {
        return config.minimumDelay();
    }

    /**
     * Gets maximum delay setting
     */
    public int getMaximumDelay() {
        return config.maximumDelay();
    }
}
