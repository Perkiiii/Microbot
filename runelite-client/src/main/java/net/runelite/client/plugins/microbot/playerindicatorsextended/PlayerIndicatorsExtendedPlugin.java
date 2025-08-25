package net.runelite.client.plugins.microbot.playerindicatorsextended;

import com.google.inject.Provides;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.events.FriendsChatMemberJoined;
import net.runelite.api.events.FriendsChatMemberLeft;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import net.runelite.client.plugins.microbot.Microbot;

@PluginDescriptor(
   name = "Player Indicators Extended",
   description = "Highlight players on-screen and/or on the minimap",
   tags = {"highlight", "minimap", "overlay", "players", "pklite"}
)
public class PlayerIndicatorsExtendedPlugin extends Plugin {
   @Inject
   private HiscoreClient HISCORE_CLIENT;
   private final List<String> callers = new ArrayList<>();
   private final Map<String, Object> colorizedMenus = new ConcurrentHashMap<>();
   private final Map<PlayerRelation, Color> relationColorHashMap = new ConcurrentHashMap<>();
   private final Map<PlayerRelation, Object[]> locationHashMap = new ConcurrentHashMap<>();
   private final Map<String, Actor> callerPiles = new ConcurrentHashMap<>();
   private final Map<String, HiscoreResult> resultCache = new HashMap<>();
   private final ExecutorService executorService = Executors.newFixedThreadPool(100);
   private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("^Level: (\\d+)\\.*");
   @Inject
   private OverlayManager overlayManager;
   @Inject
   private PlayerIndicatorsExtendedConfig config;
   @Inject
   private PlayerIndicatorsExtendedOverlay playerIndicatorsExtendedOverlay;
   @Inject
   private PlayerIndicatorsExtendedMinimapOverlay playerIndicatorsExtendedMinimapOverlay;
   @Inject
   private PlayerIndicatorsExtendedService playerIndicatorsExtendedService;
   @Inject
   private Client client;
   @Inject
   private ChatIconManager chatIconManager;

   @Provides
   PlayerIndicatorsExtendedConfig provideConfig(ConfigManager configManager) {
      return (PlayerIndicatorsExtendedConfig)configManager.getConfig(PlayerIndicatorsExtendedConfig.class);
   }

   protected void startUp() {
      this.updateConfig();
      this.resultCache.clear();
      this.overlayManager.add(this.playerIndicatorsExtendedOverlay);
      this.overlayManager.add(this.playerIndicatorsExtendedMinimapOverlay);
      this.getCallerList();
   }

   protected void shutDown() {
      this.overlayManager.remove(this.playerIndicatorsExtendedOverlay);
      this.overlayManager.remove(this.playerIndicatorsExtendedMinimapOverlay);
      this.resultCache.clear();
   }

   @Subscribe
   private void onInteractingChanged(InteractingChanged event) {
      if (this.config.callersTargets() && event.getSource() != null && !this.callers.isEmpty() && this.isCaller(event.getSource())) {
         Actor caller = event.getSource();
         if (this.callerPiles.containsKey(caller.getName())) {
            if (event.getTarget() == null) {
               this.callerPiles.remove(caller.getName());
            } else {
               this.callerPiles.replace(caller.getName(), event.getTarget());
            }
         } else if (event.getTarget() != null) {
            this.callerPiles.put(caller.getName(), event.getTarget());
         }
      }
   }

   @Subscribe
   private void onConfigChanged(ConfigChanged event) {
      if (event.getGroup().equals("playerindicatorsextended")) {
         this.updateConfig();
      }
   }

   @Subscribe
   private void onFriendsChatMemberJoined(FriendsChatMemberJoined event) {
      this.getCallerList();
   }

   @Subscribe
   private void onFriendsChatMemberLeft(FriendsChatMemberLeft event) {
      this.getCallerList();
   }

   @Subscribe
   private void onPlayerSpawned(PlayerSpawned event) {
      Player player = event.getPlayer();
      if (this.config.showAgilityLevel() && !this.resultCache.containsKey(player.getName()) && (this.client.getVarbitValue(5963) != 0 || WorldType.isPvpWorld(this.client.getWorldType()))) {
         this.executorService.submit(() -> {
            int timeout = 0;

            while(timeout < 10) {
               HiscoreResult result;
               try {
                  result = this.HISCORE_CLIENT.lookup(player.getName());
               } catch (IOException var7) {
                  ++timeout;
                  result = null;

                  try {
                     Thread.sleep(250L);
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
               }

               if (result != null) {
                  this.resultCache.put(player.getName(), result);
                  return;
               }
            }

         });
      }
   }

   @Subscribe
   private void onMenuEntryAdded(MenuEntryAdded menuEntryAdded) {
      int type = menuEntryAdded.getType();
      if (type >= 2000) {
         type -= 2000;
      }

      int identifier = menuEntryAdded.getIdentifier();
      if (type == MenuAction.WIDGET_TARGET_ON_PLAYER.getId() || type == MenuAction.ITEM_USE_ON_PLAYER.getId() || type == MenuAction.PLAYER_FIRST_OPTION.getId() || type == MenuAction.PLAYER_SECOND_OPTION.getId() || type == MenuAction.PLAYER_THIRD_OPTION.getId() || type == MenuAction.PLAYER_FOURTH_OPTION.getId() || type == MenuAction.PLAYER_FIFTH_OPTION.getId() || type == MenuAction.PLAYER_SIXTH_OPTION.getId() || type == MenuAction.PLAYER_SEVENTH_OPTION.getId() || type == MenuAction.PLAYER_EIGTH_OPTION.getId() || type == MenuAction.RUNELITE.getId()) {
         Player localPlayer = this.client.getLocalPlayer();
         // Use playerIndicatorsExtendedService.forEachPlayer to find the player by identifier
         final Player[] foundPlayer = {null};
         playerIndicatorsExtendedService.forEachPlayer((player, relation) -> {
            if (player.getName() != null && player.getName().hashCode() == identifier) {
                foundPlayer[0] = player;
            }
         });
         Player player = foundPlayer[0];

         if (player == null) {
            return;
         }

         int image = -1;
         int image2 = -1;
         Color color = null;
         if (this.config.highlightCallers() && this.isCaller(player)) {
            if (this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER) != null &&
                Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
               color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER);
            }
         } else if (this.config.callersTargets() && this.isPile(player)) {
            if (this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET) != null &&
                Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
               color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET);
            }
         } else if (this.config.highlightFriends() && this.client.isFriended(player.getName(), false)) {
            if (this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND) != null &&
                Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
               color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND);
            }
         } else if (this.config.highlightClan() && player.isFriendsChatMember()) {
            if (this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN) != null &&
                Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
               color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN);
            }

            FriendsChatRank rank = this.getRank(player.getName());
            if (rank != FriendsChatRank.UNRANKED) {
               image = this.chatIconManager.getIconNumber(rank);
            }
         } else if (this.config.highlightTeamMembers() && player.getTeam() > 0 && (localPlayer != null ? localPlayer.getTeam() : -1) == player.getTeam()) {
            if (this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM) != null &&
                Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
               color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM);
            }
         } else if (this.config.highlightOtherPlayers() && !player.isFriendsChatMember() && !player.isFriend() && !this.isAttackable(this.client, player)) {
            if (this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER) != null &&
                Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
               color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER);
            }
         } else if (this.config.highlightTargets() && !player.isFriendsChatMember() && !this.client.isFriended(player.getName(), false) && this.isAttackable(this.client, player) && this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET) != null && Arrays.stream(this.locationHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET)).anyMatch(loc -> loc == PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU)) {
            color = (Color)this.relationColorHashMap.get(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET);
         }

         if (this.config.playerSkull() && !player.isFriendsChatMember() && player.getSkullIcon() != -1) {
            image2 = 35;
         }

         if (image != -1 || color != null) {
            MenuEntry[] menuEntries = this.client.getMenuEntries();
            MenuEntry lastEntry = menuEntries[menuEntries.length - 1];
            if (color != null) {
               String target = lastEntry.getTarget();
               int idx = target.indexOf(62);
               if (idx != -1) {
                  target = target.substring(idx + 1);
               }

               lastEntry.setTarget(ColorUtil.prependColorTag(target, color));
            }

            if (image != -1) {
               lastEntry.setTarget("<img=" + image + ">" + lastEntry.getTarget());
            }

            if (image2 != -1 && this.config.playerSkull()) {
               lastEntry.setTarget("<img=" + image2 + ">" + lastEntry.getTarget());
            }

            this.client.setMenuEntries(menuEntries);
         }
      }

   }

   private void getCallerList() {
      if (this.config.highlightCallers()) {
         this.callers.clear();
         FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
         if (this.config.useClanchatRanks() && clanMemberManager != null) {
            for(FriendsChatMember clanMember : (FriendsChatMember[])clanMemberManager.getMembers()) {
               if (clanMember.getRank().getValue() >= this.config.callerRank().getValue()) {
                  this.callers.add(Text.standardize(clanMember.getName()));
               }
            }
         }

         if (this.config.callers().contains(",")) {
            this.callers.addAll(Arrays.asList(this.config.callers().split(",")));
         } else {
            if (!this.config.callers().equals("")) {
               this.callers.add(this.config.callers());
            }

         }
      }
   }

   boolean isCaller(Actor player) {
      if (player != null && player.getName() != null) {
         if (!this.callers.isEmpty()) {
            for(String name : this.callers) {
               String finalName = Text.standardize(name.trim());
               if (Text.standardize(player.getName()).equals(finalName)) {
                  return true;
               }
            }
         }
         return false;
      } else {
         return false;
      }
   }

   public boolean isPile(Actor actor) {
      return actor == null ? false : this.callerPiles.containsValue(actor);
   }

   private void updateConfig() {
      this.locationHashMap.clear();
      this.relationColorHashMap.clear();
      if (this.config.highlightOwnPlayer()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.SELF, this.config.getOwnPlayerColor());
         if (this.config.selfIndicatorModes() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.SELF, this.config.selfIndicatorModes().toArray());
         }
      }

      if (this.config.highlightFriends()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND, this.config.getFriendColor());
         if (this.config.friendIndicatorMode() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND, this.config.friendIndicatorMode().toArray());
         }
      }

      if (this.config.highlightClan()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN, this.config.getFriendsChatColor());
         if (this.config.friendsChatIndicatorModes() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN, this.config.friendsChatIndicatorModes().toArray());
         }
      }

      if (this.config.highlightTeamMembers()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM, this.config.getTeamcolor());
         if (this.config.teamIndicatorModes() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM, this.config.teamIndicatorModes().toArray());
         }
      }

      if (this.config.highlightOtherPlayers()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER, this.config.getOtherColor());
         if (this.config.otherIndicatorModes() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER, this.config.otherIndicatorModes().toArray());
         }
      }

      if (this.config.highlightTargets()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET, this.config.getTargetsColor());
         if (this.config.targetsIndicatorModes() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET, this.config.targetsIndicatorModes().toArray());
         }
      }

      if (this.config.highlightCallers()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER, this.config.callerColor());
         if (this.config.callerHighlightOptions() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER, this.config.callerHighlightOptions().toArray());
         }

         this.getCallerList();
      }

      if (this.config.callersTargets()) {
         this.relationColorHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET, this.config.callerTargetColor());
         if (this.config.callerTargetHighlightOptions() != null) {
            this.locationHashMap.put(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET, this.config.callerTargetHighlightOptions().toArray());
         }
      }

   }

   public FriendsChatRank getRank(String playerName) {
      FriendsChatManager friendsChatManager = this.client.getFriendsChatManager();
      if (friendsChatManager == null) {
         return FriendsChatRank.UNRANKED;
      } else {
         FriendsChatMember friendsChatMember = (FriendsChatMember)friendsChatManager.findByName(playerName);
         return friendsChatMember != null ? friendsChatMember.getRank() : FriendsChatRank.UNRANKED;
      }
   }

   public boolean isAttackable(Client client, Player player) {
      int wildernessLevel = 0;
      Stream var10000 = client.getWorldType().stream();
      WorldType var10001 = WorldType.DEADMAN;
      Objects.requireNonNull(var10001);
      if (var10000.anyMatch(var10001::equals)) {
         return true;
      } else {
         if (WorldType.isPvpWorld(client.getWorldType())) {
            wildernessLevel += 15;
         }

         if (client.getVarbitValue(5963) == 1) {
            wildernessLevel += getWildernessLevelFromWidget(client);
         }

         return wildernessLevel != 0 && Math.abs(client.getLocalPlayer().getCombatLevel() - player.getCombatLevel()) <= wildernessLevel;
      }
   }

   public static int getWildernessLevelFromWidget(Client client) {
      Widget wildernessLevelWidget = client.getWidget(WidgetInfo.PVP_WILDERNESS_LEVEL);
      if (wildernessLevelWidget == null) {
         return 0;
      } else {
         String wildernessLevelText = wildernessLevelWidget.getText();
         Pattern WILDERNESS_OTHER_PATTERN = Pattern.compile("^Level: (\\d+)<br>.*");
         Matcher m = WILDERNESS_LEVEL_PATTERN.matcher(wildernessLevelText);
         Matcher otherM = WILDERNESS_OTHER_PATTERN.matcher(wildernessLevelText);
         if ((m.matches() || otherM.matches()) && !WorldType.isPvpWorld(client.getWorldType())) {
            int wildernessLevel = 0;
            if (m.matches()) {
               wildernessLevel = Integer.parseInt(m.group(1));
            } else {
               wildernessLevel = Integer.parseInt(otherM.group(1));
            }

            return wildernessLevel;
         } else {
            return 0;
         }
      }
   }

   HiscoreClient getHISCORE_CLIENT() {
      return this.HISCORE_CLIENT;
   }

   List<String> getCallers() {
      return this.callers;
   }

   Map<String, Object> getColorizedMenus() {
      return this.colorizedMenus;
   }

   Map<PlayerRelation, Color> getRelationColorHashMap() {
      return this.relationColorHashMap;
   }

   Map<PlayerRelation, Object[]> getLocationHashMap() {
      return this.locationHashMap;
   }

   Map<String, Actor> getCallerPiles() {
      return this.callerPiles;
   }

   Map<String, HiscoreResult> getResultCache() {
      return this.resultCache;
   }

   public enum MinimapSkullLocations {
      BEFORE_NAME,
      AFTER_NAME
   }

   public enum AgilityFormats {
      TEXT,
      ICONS
   }

   public enum PlayerIndicationLocation {
      ABOVE_HEAD,
      HULL,
      MINIMAP,
      MENU,
      TILE
   }

   public enum PlayerRelation {
      SELF,
      FRIEND,
      CLAN,
      TEAM,
      TARGET,
      OTHER,
      CALLER,
      CALLER_TARGET
   }
}
