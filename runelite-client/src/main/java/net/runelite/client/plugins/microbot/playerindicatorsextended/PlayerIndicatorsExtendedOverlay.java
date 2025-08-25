package net.runelite.client.plugins.microbot.playerindicatorsextended;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.WorldType;
import net.runelite.api.kit.KitType;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerIndicatorsExtendedOverlay extends Overlay {
   private static final Logger log = LoggerFactory.getLogger(PlayerIndicatorsExtendedOverlay.class);
   private static final int ACTOR_OVERHEAD_TEXT_MARGIN = 40;
   private static final int ACTOR_HORIZONTAL_TEXT_MARGIN = 10;
   private final BufferedImage agilityIcon = ImageUtil.getResourceStreamFromClass(PlayerIndicatorsExtendedPlugin.class, "agility.png");
   private final BufferedImage noAgilityIcon = ImageUtil.getResourceStreamFromClass(PlayerIndicatorsExtendedPlugin.class, "no-agility.png");
   private final BufferedImage skullIcon = ImageUtil.getResourceStreamFromClass(PlayerIndicatorsExtendedPlugin.class, "skull.png");
   private final PlayerIndicatorsExtendedPlugin plugin;
   private final PlayerIndicatorsExtendedConfig config;
   private final PlayerIndicatorsExtendedService playerIndicatorsExtendedService;
   @Inject
   private Client client;
   @Inject
   private ChatIconManager chatIconManager;

   @Inject
   public PlayerIndicatorsExtendedOverlay(PlayerIndicatorsExtendedPlugin plugin, PlayerIndicatorsExtendedConfig config, PlayerIndicatorsExtendedService playerIndicatorsExtendedService) {
      this.plugin = plugin;
      this.config = config;
      this.playerIndicatorsExtendedService = playerIndicatorsExtendedService;
      this.setPosition(OverlayPosition.DYNAMIC);
      this.setPriority(OverlayPriority.MED);
   }

   public Dimension render(Graphics2D graphics) {
      this.playerIndicatorsExtendedService.forEachPlayer((player, playerRelation) -> this.drawSceneOverlays(graphics, player, playerRelation));
      return null;
   }

   private void drawSceneOverlays(Graphics2D graphics, Player actor, PlayerIndicatorsExtendedPlugin.PlayerRelation relation) {
      if (actor.getName() != null && this.plugin.getLocationHashMap().containsKey(relation)) {
         List indicationLocations = Arrays.asList(this.plugin.getLocationHashMap().get(relation));
         Color color = (Color)this.plugin.getRelationColorHashMap().get(relation);
         boolean skulls = this.config.playerSkull();
         String name = actor.getName();
         int zOffset = actor.getLogicalHeight() + 40;
         Point textLocation = actor.getCanvasTextLocation(graphics, name, zOffset);
         if (indicationLocations.contains(PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.ABOVE_HEAD)) {
            StringBuilder nameSb = new StringBuilder(name);
            if (this.config.showCombatLevel()) {
               nameSb.append(" (");
               nameSb.append(actor.getCombatLevel());
               nameSb.append(")");
            }

            if (this.config.unchargedGlory() && actor.getPlayerComposition().getEquipmentId(KitType.AMULET) == 1704) {
               nameSb.append(" (glory)");
            }

            String builtString = nameSb.toString();
            int x = graphics.getFontMetrics().stringWidth(builtString);
            int y = graphics.getFontMetrics().getHeight();
            if (this.config.highlightClan() && actor.isFriendsChatMember() && this.config.showFriendsChatRanks() && relation == PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN) {
               if (this.plugin.getRank(actor.getName()) != null) {
                  BufferedImage clanRankImage = this.chatIconManager.getRankImage(this.plugin.getRank(actor.getName()));
                  if (clanRankImage != null) {
                     renderActorTextAndImage(graphics, actor, builtString, color, ImageUtil.resizeImage(clanRankImage, y, y), 0, 10);
                  }
               }
            } else if (skulls && actor.getSkullIcon() != -1 && relation.equals(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET)) {
               renderActorTextAndImage(graphics, actor, builtString, color, ImageUtil.resizeImage(this.skullIcon, y, y), 40, 10);
            } else {
               renderActorTextOverlay(graphics, actor, builtString, color);
            }
         }

         if (actor.getConvexHull() != null && indicationLocations.contains(PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.HULL)) {
            OverlayUtil.renderPolygon(graphics, actor.getConvexHull(), color);
         }

         if (indicationLocations.contains(PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.TILE) && actor.getCanvasTilePoly() != null) {
            OverlayUtil.renderPolygon(graphics, actor.getCanvasTilePoly(), color);
         }

         if (relation.equals(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET) && this.config.showAgilityLevel() && this.checkWildy() && this.plugin.getResultCache().containsKey(actor.getName())) {
            if (textLocation == null) {
               return;
            }

            int level = ((HiscoreResult)this.plugin.getResultCache().get(actor.getName())).getSkill(HiscoreSkill.AGILITY).getLevel();
            if (this.config.agilityFormat() == PlayerIndicatorsExtendedPlugin.AgilityFormats.ICONS) {
               int width = this.config.showCombatLevel() ? graphics.getFontMetrics().stringWidth(name) + 10 : graphics.getFontMetrics().stringWidth(name);
               int height = graphics.getFontMetrics().getHeight();
               if (level >= this.config.agilityFirstThreshold()) {
                  OverlayUtil.renderImageLocation(graphics, new Point(textLocation.getX() + 5 + width, textLocation.getY() - height), ImageUtil.resizeImage(this.agilityIcon, height, height));
               }

               if (level >= this.config.agilitySecondThreshold()) {
                  OverlayUtil.renderImageLocation(graphics, new Point(textLocation.getX() + this.agilityIcon.getWidth() + width, textLocation.getY() - height), ImageUtil.resizeImage(this.agilityIcon, height, height));
               }

               if (level < this.config.agilityFirstThreshold()) {
                  OverlayUtil.renderImageLocation(graphics, new Point(textLocation.getX() + 5 + width, textLocation.getY() - height), ImageUtil.resizeImage(this.noAgilityIcon, height, height));
               }
            } else {
               Color agiColor = Color.WHITE;
               if (level >= this.config.agilityFirstThreshold()) {
                  agiColor = Color.CYAN;
               } else if (level >= this.config.agilitySecondThreshold()) {
                  agiColor = Color.GREEN;
               } else if (level < this.config.agilityFirstThreshold()) {
                  agiColor = Color.RED;
               }

               String n = level + " Agility";
               renderActorTextOverlay(graphics, actor, n, agiColor, 60);
            }
         }

      }
   }

   private boolean checkWildy() {
      return this.client.getVarbitValue(5963) == 1 || WorldType.isPvpWorld(this.client.getWorldType());
   }

   public static void renderActorTextAndImage(Graphics2D graphics, Actor actor, String text, Color color, BufferedImage image, int yOffset, int xOffset) {
      Point textLocation = actor.getCanvasTextLocation(graphics, text, actor.getLogicalHeight() + yOffset);
      if (textLocation != null) {
         OverlayUtil.renderImageLocation(graphics, textLocation, image);
         textLocation = new Point(textLocation.getX() + xOffset, textLocation.getY());
         OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
      }

   }

   public static void renderActorTextOverlay(Graphics2D graphics, Actor actor, String text, Color color) {
      renderActorTextOverlay(graphics, actor, text, color, 40);
   }

   public static void renderActorTextOverlay(Graphics2D graphics, Actor actor, String text, Color color, int offset) {
      Point textLocation = actor.getCanvasTextLocation(graphics, text, actor.getLogicalHeight() + offset);
      if (textLocation != null) {
         OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
      }

   }
}
