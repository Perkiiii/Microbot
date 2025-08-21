package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.api.Actor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class PvPUtilitiesOverlay extends OverlayPanel {
    private final PvPUtilitiesConfig config;
    private final PvPUtilitiesPlugin plugin;

    @Inject
    PvPUtilitiesOverlay(PvPUtilitiesConfig config, PvPUtilitiesPlugin plugin) {
        super();
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!Microbot.isLoggedIn()) {
            return null;
        }

        try {
            panelComponent.setPreferredSize(new Dimension(200, 100));

            // Title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("PvP Utilities")
                    .color(Color.WHITE)
                    .build());

            // Current Target
            Actor currentTarget = null;
            try {
                currentTarget = Rs2Player.getInteracting();
            } catch (Exception ex) {
                // If we can't get current target, just show "None"
            }

            String targetName = "None";
            Color targetColor = Color.RED;

            if (currentTarget != null) {
                targetName = currentTarget.getName();
                targetColor = Color.GREEN;
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Target:")
                    .right(targetName)
                    .rightColor(targetColor)
                    .build());

            // Offensive Prayer Switching (only show if enabled in config)
            if (config.prayerSwitchingEnabled()) {
                try {
                    String opsStatus = plugin.isPrayerSwitchingActive() ? "Active" : "Not Active";
                    Color opsColor = plugin.isPrayerSwitchingActive() ? Color.GREEN : Color.RED;

                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("OPS:")
                            .right(opsStatus)
                            .rightColor(opsColor)
                            .build());
                } catch (Exception ex) {
                    // If there's an error getting prayer status, show as Not Active
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("OPS:")
                            .right("Not Active")
                            .rightColor(Color.RED)
                            .build());
                }
            }

        } catch (Exception ex) {
            // If there's any error, just show basic info
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right("Error")
                    .rightColor(Color.RED)
                    .build());
        }

        return super.render(graphics);
    }
}
