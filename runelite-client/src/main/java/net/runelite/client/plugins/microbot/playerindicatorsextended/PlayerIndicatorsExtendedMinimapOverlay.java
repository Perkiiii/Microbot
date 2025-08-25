package net.runelite.client.plugins.microbot.playerindicatorsextended;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;

public class PlayerIndicatorsExtendedMinimapOverlay extends Overlay {
   private final PlayerIndicatorsExtendedService playerIndicatorsExtendedService;
   private final PlayerIndicatorsExtendedPlugin plugin;
   private final PlayerIndicatorsExtendedConfig config;
   private final BufferedImage skullIcon = ImageUtil.loadImageResource(PlayerIndicatorsExtendedPlugin.class, "skull.png");

   @Inject
   private PlayerIndicatorsExtendedMinimapOverlay(PlayerIndicatorsExtendedPlugin plugin, PlayerIndicatorsExtendedConfig config, PlayerIndicatorsExtendedService playerIndicatorsExtendedService) {
      this.plugin = plugin;
      this.config = config;
      this.playerIndicatorsExtendedService = playerIndicatorsExtendedService;
      this.setLayer(OverlayLayer.ABOVE_WIDGETS);
      this.setPosition(OverlayPosition.DYNAMIC);
      this.setPriority(OverlayPriority.HIGH);
   }

   private void renderMinimapOverlays(Graphics2D graphics, Player actor, PlayerIndicatorsExtendedPlugin.PlayerRelation relation) {
      if (this.plugin.getLocationHashMap().containsKey(relation) && actor.getName() != null) {
         List indicationLocations = Arrays.asList(this.plugin.getLocationHashMap().get(relation));
         Color color = (Color)this.plugin.getRelationColorHashMap().get(relation);
         if (indicationLocations.contains(PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MINIMAP)) {
            String name = actor.getName().replace(' ', ' ');
            Point minimapLocation = actor.getMinimapLocation();
            if (minimapLocation != null) {
               if (this.config.showCombatLevel()) {
                  name = name + "-(" + actor.getCombatLevel() + ")";
               }

               if (actor.getSkullIcon() != -1 && this.config.playerSkull()) {
                  int width = graphics.getFontMetrics().stringWidth(name);
                  int height = graphics.getFontMetrics().getHeight();
                  if (this.config.skullLocation().equals(PlayerIndicatorsExtendedPlugin.MinimapSkullLocations.AFTER_NAME)) {
                     OverlayUtil.renderImageLocation(graphics, new Point(minimapLocation.getX() + width, minimapLocation.getY() - height), ImageUtil.resizeImage(this.skullIcon, height, height));
                  } else {
                     OverlayUtil.renderImageLocation(graphics, new Point(minimapLocation.getX(), minimapLocation.getY() - height), ImageUtil.resizeImage(this.skullIcon, height, height));
                     minimapLocation = new Point(minimapLocation.getX() + this.skullIcon.getWidth(), minimapLocation.getY());
                  }
               }

               OverlayUtil.renderTextLocation(graphics, minimapLocation, name, color);
            }
         }

      }
   }

   public Dimension render(Graphics2D graphics) {
      this.playerIndicatorsExtendedService.forEachPlayer((Player player, PlayerIndicatorsExtendedPlugin.PlayerRelation playerRelation) -> this.renderMinimapOverlays(graphics, player, playerRelation));
      return null;
   }
}
