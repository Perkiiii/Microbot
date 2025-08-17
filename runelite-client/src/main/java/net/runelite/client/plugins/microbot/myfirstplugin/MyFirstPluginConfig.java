package net.runelite.client.plugins.microbot.myfirstplugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("MyFirstPlugin")
public interface MyFirstPluginConfig extends Config {

    @ConfigItem(
            keyName = "enablePlugin",
            name = "Enable Plugin",
            description = "Enable or disable the plugin functionality"
    )
    default boolean enablePlugin() {
        return true;
    }

    @ConfigItem(
            keyName = "debugMode",
            name = "Debug Mode",
            description = "Show debug messages in chat"
    )
    default boolean debugMode() {
        return false;
    }

    @ConfigItem(
            keyName = "customMessage",
            name = "Custom Message",
            description = "Custom message to display"
    )
    default String customMessage() {
        return "Hello from My First Plugin!";
    }

    @ConfigItem(
            keyName = "checkInterval",
            name = "Check Interval (ticks)",
            description = "How often to perform checks (in game ticks)"
    )
    default int checkInterval() {
        return 5;
    }
}
