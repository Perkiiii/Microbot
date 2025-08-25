package net.runelite.client.plugins.microbot.playerindicatorsextended;

import net.runelite.client.plugins.microbot.Script;
import java.util.concurrent.TimeUnit;

public class PlayerIndicatorsExtendedScript extends Script {
    public static double version = 1.0;

    public boolean run(PlayerIndicatorsExtendedConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                // Your script logic here (currently does nothing)
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

