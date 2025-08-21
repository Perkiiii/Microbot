package net.runelite.client.plugins.microbot.pvputilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "PvP Utilities",
        description = "Minimal hotkey equipment for PvP",
        tags = {"pvp", "equipment", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class PvPUtilitiesPlugin extends Plugin {

    @Inject
    private PvPUtilitiesConfig config;

    @Provides
    PvPUtilitiesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPUtilitiesConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvPUtilitiesOverlay overlay;

    @Inject
    private PvPUtilitiesScript script;

    @Inject
    private KeyManager keyManager;

    // Only 2 hotkey listeners for minimal testing
    private HotkeyListener hk1, hk2;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(overlay);
        }

        script.run(config);

        // Create hotkey listeners that call the new direct methods
        hk1 = new HotkeyListener(() -> config.hotkey1()) {
            @Override
            public void hotkeyPressed() {
                script.executeGearSet1();
            }
        };

        hk2 = new HotkeyListener(() -> config.hotkey2()) {
            @Override
            public void hotkeyPressed() {
                script.executeGearSet2();
            }
        };

        // Register hotkey listeners
        keyManager.registerKeyListener(hk1);
        keyManager.registerKeyListener(hk2);

        log.info("PvP Utilities minimal plugin started!");
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        if (overlayManager != null) {
            overlayManager.remove(overlay);
        }

        // Unregister hotkey listeners
        if (keyManager != null) {
            keyManager.unregisterKeyListener(hk1);
            keyManager.unregisterKeyListener(hk2);
        }

        log.info("PvP Utilities plugin stopped.");
    }
}
