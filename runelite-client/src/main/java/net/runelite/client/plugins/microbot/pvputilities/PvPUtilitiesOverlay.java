package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.client.plugins.microbot.Microbot;
<<<<<<< Updated upstream
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
=======
>>>>>>> Stashed changes
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

public class PvPUtilitiesOverlay extends OverlayPanel {
    private final PvPUtilitiesConfig config;
    private final PvPUtilitiesPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    PvPUtilitiesOverlay(PvPUtilitiesConfig config, PvPUtilitiesPlugin plugin, ModelOutlineRenderer modelOutlineRenderer) {
        super();
        this.config = config;
        this.plugin = plugin;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!Microbot.isLoggedIn()) {
            return null;
        }

        try {
<<<<<<< Updated upstream
            panelComponent.setPreferredSize(new Dimension(200, 100));
=======
            panelComponent.setPreferredSize(new Dimension(180, 80));
>>>>>>> Stashed changes

            // Title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("PvP Utilities")
                    .color(Color.WHITE)
                    .build());

<<<<<<< Updated upstream
            // Show our setTarget currentTarget instead of interacting target
            Actor currentTarget = PvPUtilitiesPlugin.getTarget();

=======
            // Current Target only (removed status and spec info as requested)
            Actor currentTarget = plugin.getCurrentTarget();
>>>>>>> Stashed changes
            String targetName = "None";
            Color targetColor = Color.RED;

            if (currentTarget != null) {
                targetName = currentTarget.getName();
                targetColor = Color.GREEN;

<<<<<<< Updated upstream
                // Highlight the target if highlighting is enabled
                if (config.highlightTarget()) {
                    highlightTarget(currentTarget);
                }
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Set Target:")
=======
                // Highlight the target if enabled
                highlightTarget(currentTarget);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Target:")
>>>>>>> Stashed changes
                    .right(targetName)
                    .rightColor(targetColor)
                    .build());

<<<<<<< Updated upstream
            // Also show current interacting target for comparison
            Actor interactingTarget = null;
            try {
                interactingTarget = Rs2Player.getInteracting();
            } catch (Exception ex) {
                // If we can't get current target, just show "None"
            }

            String interactingName = "None";
            Color interactingColor = Color.GRAY;

            if (interactingTarget != null) {
                interactingName = interactingTarget.getName();
                interactingColor = Color.YELLOW;
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Interacting:")
                    .right(interactingName)
                    .rightColor(interactingColor)
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
=======
        } catch (Exception e) {
            // Fail silently to avoid log spam
>>>>>>> Stashed changes
        }

        return super.render(graphics);
    }

    private void highlightTarget(Actor target) {
        if (target == null || !config.highlightTarget()) {
            return;
        }

        try {
            // Only highlight players for now (most common PvP scenario)
            if (target instanceof Player) {
                Player player = (Player) target;
                Color highlightColor = config.targetHighlightColor();

                // Create outline highlight around the target
                modelOutlineRenderer.drawOutline(player, 2, highlightColor, 0);
            }
        } catch (Exception e) {
            // Silently fail if highlighting doesn't work to avoid spam
        }
    }
}
