package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class PvPUtilitiesOverlay extends OverlayPanel {
    private final PvPUtilitiesConfig config;

    @Inject
    PvPUtilitiesOverlay(PvPUtilitiesConfig config) {
        super();
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enablePlugin()) return null;

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("PvP Equipment")
                .color(Color.GREEN)
                .build());

        // Show configured hotkeys
        if (!config.gear1().isEmpty()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Gear Set 1")
                    .right(config.hotkey1().toString())
                    .build());
        }

        if (!config.gear2().isEmpty()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Gear Set 2")
                    .right(config.hotkey2().toString())
                    .build());
        }

        return super.render(graphics);
    }
}
