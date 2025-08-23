package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.client.plugins.microbot.Microbot;
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
            panelComponent.setPreferredSize(new Dimension(180, 80));

            // Title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("PvP Utilities")
                    .color(Color.WHITE)
                    .build());

            // Current Target only (removed status and spec info as requested)
            Actor currentTarget = plugin.getCurrentTarget();
            String targetName = "None";
            Color targetColor = Color.RED;

            if (currentTarget != null) {
                targetName = currentTarget.getName();
                targetColor = Color.GREEN;

                // Highlight the target if enabled
                highlightTarget(currentTarget);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Target:")
                    .right(targetName)
                    .rightColor(targetColor)
                    .build());

        } catch (Exception e) {
            // Fail silently to avoid log spam
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
