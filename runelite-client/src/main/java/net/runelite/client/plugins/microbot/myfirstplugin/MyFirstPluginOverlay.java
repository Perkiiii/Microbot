package net.runelite.client.plugins.microbot.myfirstplugin;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class MyFirstPluginOverlay extends OverlayPanel {

    @Inject
    MyFirstPluginConfig config;

    @Inject
    MyFirstPluginScript script;

    public MyFirstPluginOverlay(MyFirstPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            if (!config.enablePlugin()) {
                return null;
            }

            panelComponent.setPreferredSize(new Dimension(250, 100));
            panelComponent.getChildren().clear();

            // Title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("My First Plugin")
                    .color(Color.GREEN)
                    .build());

            // Display custom message
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Message:")
                    .right(config.customMessage())
                    .build());

            // Display if script is running
            boolean isRunning = script.isRunning();
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(isRunning ? "Running" : "Stopped")
                    .rightColor(isRunning ? Color.GREEN : Color.RED)
                    .build());

            // Display current game tick info
            if (Microbot.getClient().getGameState() != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Game State:")
                        .right(Microbot.getClient().getGameState().toString())
                        .build());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
