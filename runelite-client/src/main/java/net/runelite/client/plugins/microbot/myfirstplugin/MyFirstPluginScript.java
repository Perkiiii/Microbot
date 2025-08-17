package net.runelite.client.plugins.microbot.myfirstplugin;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.util.concurrent.TimeUnit;

public class MyFirstPluginScript extends Script {

    public static double version = 1.0;

    public boolean run(MyFirstPluginConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.enablePlugin()) return;

                long startTime = System.currentTimeMillis();

                // Example functionality - you can modify this section
                if (config.debugMode()) {
                    Microbot.log("Script is running - Custom message: " + config.customMessage());
                }

                // Add your custom plugin logic here!
                // This is where you would implement your specific functionality
                // For example:
                // - Combat automation
                // - Skilling automation
                // - Item management
                // - Banking operations
                // - Walking/navigation
                // - etc.

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                if (config.debugMode()) {
                    System.out.println("Total time for loop " + totalTime);
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
