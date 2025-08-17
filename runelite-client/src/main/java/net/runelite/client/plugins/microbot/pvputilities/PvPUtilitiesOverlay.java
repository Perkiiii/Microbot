package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ButtonComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class PvPUtilitiesOverlay extends OverlayPanel {

    public final ButtonComponent myButton;

    @Inject
    PvPUtilitiesConfig config;

    @Inject
    PvPUtilitiesOverlay(PvPUtilitiesPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
        myButton = new ButtonComponent("PvP Utils");
        myButton.setPreferredSize(new Dimension(100, 30));
        myButton.setParentOverlay(this);
        myButton.setFont(FontManager.getRunescapeBoldFont());
        myButton.setOnClick(() -> Microbot.openPopUp("Microbot", String.format("S-1D:<br><br><col=ffffff>%s Popup</col>", "PvP Utilities")));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            if (!config.enablePlugin()) {
                return null;
            }

            panelComponent.setPreferredSize(new Dimension(250, 200));
            panelComponent.getChildren().clear();

            // Title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("PvP Utilities V1.2.0")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            // Display Microbot status
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());

            // Display enabled profiles and their hotkeys
            if (config.enableProfile1()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Profile 1:")
                        .right(config.hotkey1().toString())
                        .rightColor(Color.CYAN)
                        .build());
            }

            if (config.enableProfile2()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Profile 2:")
                        .right(config.hotkey2().toString())
                        .rightColor(Color.CYAN)
                        .build());
            }

            if (config.enableProfile3()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Profile 3:")
                        .right(config.hotkey3().toString())
                        .rightColor(Color.CYAN)
                        .build());
            }

            if (config.enableProfile4()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Profile 4:")
                        .right(config.hotkey4().toString())
                        .rightColor(Color.CYAN)
                        .build());
            }

            if (config.enableProfile5()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Profile 5:")
                        .right(config.hotkey5().toString())
                        .rightColor(Color.CYAN)
                        .build());
            }

            panelComponent.getChildren().add(myButton);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
