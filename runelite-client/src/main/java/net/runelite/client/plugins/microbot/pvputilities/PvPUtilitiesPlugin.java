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
        name = PluginDescriptor.Default + "PvP Utilities",
        description = "Gear + prayer + spell automation toolkit for PvP",
        tags = {"pvp", "utilities", "microbot"},
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

    // Hotkey listeners for all 5 profiles
    private HotkeyListener hk1, hk2, hk3, hk4, hk5;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(overlay);
            overlay.myButton.hookMouseListener();
        }

        script.run(config);

        // Create and register hotkey listeners for all 5 profiles
        hk1 = new HotkeyListener(() -> config.hotkey1()) {
            @Override
            public void hotkeyPressed() {
                script.executeProfile(1);
            }
        };

        hk2 = new HotkeyListener(() -> config.hotkey2()) {
            @Override
            public void hotkeyPressed() {
                script.executeProfile(2);
            }
        };

        hk3 = new HotkeyListener(() -> config.hotkey3()) {
            @Override
            public void hotkeyPressed() {
                script.executeProfile(3);
            }
        };

        hk4 = new HotkeyListener(() -> config.hotkey4()) {
            @Override
            public void hotkeyPressed() {
                script.executeProfile(4);
            }
        };

        hk5 = new HotkeyListener(() -> config.hotkey5()) {
            @Override
            public void hotkeyPressed() {
                script.executeProfile(5);
            }
        };

        // Register all hotkey listeners
        keyManager.registerKeyListener(hk1);
        keyManager.registerKeyListener(hk2);
        keyManager.registerKeyListener(hk3);
        keyManager.registerKeyListener(hk4);
        keyManager.registerKeyListener(hk5);

        if (config.enablePlugin()) {
            log.info("PvP Utilities plugin started successfully!");
        }
    }

    @Override
    protected void shutDown() {
        script.shutdown();

        if (overlayManager != null) {
            overlayManager.remove(overlay);
            overlay.myButton.unhookMouseListener();
        }

        // Unregister all hotkey listeners
        if (keyManager != null) {
            keyManager.unregisterKeyListener(hk1);
            keyManager.unregisterKeyListener(hk2);
            keyManager.unregisterKeyListener(hk3);
            keyManager.unregisterKeyListener(hk4);
            keyManager.unregisterKeyListener(hk5);
        }

        log.info("PvP Utilities plugin shut down successfully!");
    }
}
