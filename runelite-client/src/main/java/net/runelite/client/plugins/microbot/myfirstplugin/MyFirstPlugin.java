package net.runelite.client.plugins.microbot.myfirstplugin;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "My First Plugin",
        description = "Your first custom Microbot plugin for experimentation",
        tags = {"custom", "experimental", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class MyFirstPlugin extends Plugin {

    @Inject
    private MyFirstPluginConfig config;

    @Provides
    MyFirstPluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MyFirstPluginConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MyFirstPluginOverlay overlay;

    @Inject
    private MyFirstPluginScript script;

    private int tickCounter = 0;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(overlay);
            overlay.myButton.hookMouseListener();
        }

        script.run(config);
    }

    @Override
    protected void shutDown() {
        script.shutdown();

        if (overlayManager != null) {
            overlayManager.remove(overlay);
            overlay.myButton.unhookMouseListener();
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (!config.enablePlugin()) {
            return;
        }

        tickCounter++;

        // Perform actions every configured interval
        if (tickCounter >= config.checkInterval()) {
            tickCounter = 0;

            if (config.debugMode()) {
                log.info("Plugin tick: " + config.customMessage());
            }

            // Add your custom logic here
            // This is where you would add your plugin's main functionality
        }
    }
}
