package net.runelite.client.plugins.microbot.playerindicatorsextended;

import java.awt.Color;
import java.util.Set;
import net.runelite.api.FriendsChatRank;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.playerindicatorsextended.PlayerIndicatorsExtendedPlugin;

@ConfigGroup("playerindicatorsextended")
public interface PlayerIndicatorsExtendedConfig extends Config {
   Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> defaultPlayerIndicatorMode = Set.of(
      PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.ABOVE_HEAD,
      PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MINIMAP,
      PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.MENU,
      PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.TILE);
   @ConfigSection(
      name = "Yourself",
      description = "",
      position = 0
   )
   String yourselfSection = "Yourself";
   @ConfigSection(
      name = "Friends",
      description = "",
      position = 1
   )
   String friendsSection = "Friends";
   @ConfigSection(
      name = "Clan",
      description = "",
      position = 2
   )
   String friendsChatSection = "Clan";
   @ConfigSection(
      name = "Team",
      description = "",
      position = 3
   )
   String teamSection = "Team";
   @ConfigSection(
      name = "Target",
      description = "",
      position = 4
   )
   String targetSection = "Target";
   @ConfigSection(
      name = "Other",
      description = "",
      position = 5
   )
   String otherSection = "Other";
   @ConfigSection(
      name = "Callers",
      description = "",
      position = 6
   )
   String callersSection = "Callers";
   @ConfigSection(
      name = "Miscellaneous",
      description = "",
      position = 7
   )
   String miscellaneousSection = "Miscellaneous";

   @ConfigItem(
      position = 0,
      keyName = "drawOwnNameNew",
      name = "Highlight own player",
      description = "Configures whether or not your own player should be highlighted",
      section = "Yourself"
   )
   default boolean highlightOwnPlayer() {
      return false;
   }

   @ConfigItem(
      position = 1,
      keyName = "ownNameColorNew",
      name = "Own player color",
      description = "Color of your own player",
      section = "Yourself"
   )
   default Color getOwnPlayerColor() {
      return new Color(0, 184, 212);
   }

   @ConfigItem(
      position = 2,
      keyName = "selfIndicatorModesNew",
      name = "Indicator Mode",
      description = "Location(s) of the overlay",
      section = "Yourself"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> selfIndicatorModes() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 0,
      keyName = "drawFriendNamesNew",
      name = "Highlight friends",
      description = "Configures whether or not friends should be highlighted",
      section = "Friends"
   )
   default boolean highlightFriends() {
      return false;
   }

   @ConfigItem(
      position = 1,
      keyName = "friendNameColorNew",
      name = "Friend color",
      description = "Color of friend names",
      section = "Friends"
   )
   default Color getFriendColor() {
      return new Color(0, 200, 83);
   }

   @ConfigItem(
      position = 2,
      keyName = "friendIndicatorModeNew",
      name = "Indicator Mode",
      description = "Location(s) of the overlay",
      section = "Friends"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> friendIndicatorMode() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 0,
      keyName = "highlightClanNew",
      name = "Highlight friends chat members",
      description = "Configures whether or friends chat members should be highlighted",
      section = "Clan"
   )
   default boolean highlightClan() {
      return true;
   }

   @ConfigItem(
      position = 1,
      keyName = "clanMemberColorNew",
      name = "Friends chat member color",
      description = "Color of friends chat members",
      section = "Clan"
   )
   default Color getFriendsChatColor() {
      return new Color(170, 0, 255);
   }

   @ConfigItem(
      position = 2,
      keyName = "clanIndicatorModesNew",
      name = "Indicator Mode",
      description = "Location(s) of the overlay",
      section = "Clan"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> friendsChatIndicatorModes() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 3,
      keyName = "clanMenuIconsNew",
      name = "Show friends chat ranks",
      description = "Add friends chat rank to right click menu and next to player names",
      section = "Clan"
   )
   default boolean showFriendsChatRanks() {
      return false;
   }

   @ConfigItem(
      position = 0,
      keyName = "drawTeamMemberNamesNew",
      name = "Highlight team members",
      description = "Configures whether or not team members should be highlighted",
      section = "Team"
   )
   default boolean highlightTeamMembers() {
      return false;
   }

   @ConfigItem(
      position = 1,
      keyName = "teamMemberColorNew",
      name = "Team member color",
      description = "Color of team members",
      section = "Team"
   )
   default Color getTeamcolor() {
      return new Color(19, 110, 247);
   }

   @ConfigItem(
      position = 2,
      keyName = "teamIndicatorModesNew",
      name = "Indicator Mode",
      description = "Location(s) of the overlay",
      section = "Team"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> teamIndicatorModes() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 0,
      keyName = "drawTargetsNamesNew",
      name = "Highlight attackable targets",
      description = "Configures whether or not attackable targets should be highlighted",
      section = "Target"
   )
   default boolean highlightTargets() {
      return false;
   }

   @ConfigItem(
      position = 1,
      keyName = "targetColorNew",
      name = "Target member color",
      description = "Color of attackable targets",
      section = "Target"
   )
   default Color getTargetsColor() {
      return new Color(19, 110, 247);
   }

   @ConfigItem(
      position = 2,
      keyName = "targetsIndicatorModesNew",
      name = "Indicator Mode",
      description = "Location(s) of the overlay",
      section = "Target"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> targetsIndicatorModes() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 3,
      keyName = "showAgilityNew",
      name = "Show Agility Levels",
      description = "Show the agility level of attackable players next to their name while in the wilderness.",
      section = "Target"
   )
   default boolean showAgilityLevel() {
      return false;
   }

   @ConfigItem(
      position = 4,
      keyName = "agilityFormatNew",
      name = "Format",
      description = "Whether to show the agility level as text, or as icons (1 skull >= 1st threshold, 2 skulls >= 2nd threshold).",
      section = "Target"
   )
   default PlayerIndicatorsExtendedPlugin.AgilityFormats agilityFormat() {
      return PlayerIndicatorsExtendedPlugin.AgilityFormats.TEXT;
   }

   @ConfigItem(
      position = 5,
      keyName = "agilityFirstThresholdNew",
      name = "First Threshold",
      description = "When showing agility as icons, show one icon for agility >= this level.",
      section = "Target"
   )
   default int agilityFirstThreshold() {
      return 70;
   }

   @ConfigItem(
      position = 6,
      keyName = "agilitySecondThresholdNew",
      name = "Second Threshold",
      description = "When showing agility as icons, show two icons for agility >= this level.",
      section = "Target"
   )
   default int agilitySecondThreshold() {
      return 84;
   }

   @ConfigItem(
      position = 7,
      keyName = "playerSkullNew",
      name = "Show Skull Information",
      description = "shows",
      section = "Target"
   )
   default boolean playerSkull() {
      return false;
   }

   @ConfigItem(
      position = 8,
      keyName = "minimapSkullLocationNew",
      name = "Skull Icon Location",
      description = "The location of the skull icon for skulled players",
      section = "Target"
   )
   default PlayerIndicatorsExtendedPlugin.MinimapSkullLocations skullLocation() {
      return PlayerIndicatorsExtendedPlugin.MinimapSkullLocations.AFTER_NAME;
   }

   @ConfigItem(
      position = 9,
      keyName = "targetRiskNew",
      name = "Indicate Target Risk",
      description = "Indicates the risk (in K GP) of the target",
      section = "Target"
   )
   default boolean targetRisk() {
      return false;
   }

   @ConfigItem(
      position = 10,
      keyName = "showCombatNew",
      name = "Show Combat Levels",
      description = "Show the combat level of attackable players next to their name.",
      section = "Target"
   )
   default boolean showCombatLevel() {
      return false;
   }

   @ConfigItem(
      position = 0,
      keyName = "drawOtherPlayerNamesNew",
      name = "Highlight other players",
      description = "Configures whether or not other players should be highlighted",
      section = "Other"
   )
   default boolean highlightOtherPlayers() {
      return false;
   }

   @ConfigItem(
      position = 1,
      keyName = "otherPlayerColorNew",
      name = "Other player color",
      description = "Color of other players' names",
      section = "Other"
   )
   default Color getOtherColor() {
      return Color.RED;
   }

   @ConfigItem(
      position = 2,
      keyName = "otherIndicatorModesNew",
      name = "Indicator Mode",
      description = "Location(s) of the overlay",
      section = "Other"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> otherIndicatorModes() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 1,
      keyName = "highlightCallersNew",
      name = "Highlight Callers",
      description = "Highlights Callers Onscreen",
      section = "Callers"
   )
   default boolean highlightCallers() {
      return false;
   }

   @ConfigItem(
      position = 2,
      keyName = "useClanchatRanksNew",
      name = "Use Ranks as Callers",
      description = "Uses friends chat ranks as the list of callers",
      section = "Callers"
   )
   default boolean useClanchatRanks() {
      return false;
   }

   @ConfigItem(
      position = 3,
      keyName = "callerRankNew",
      name = "Minimum rank for friends chat Caller",
      description = "Chooses the minimum rank to use as friends chat callers.",
      section = "Callers"
   )
   default FriendsChatRank callerRank() {
      return FriendsChatRank.CAPTAIN;
   }

   @ConfigItem(
      position = 4,
      keyName = "callersNew",
      name = "List of callers to highlight",
      description = "Highlights callers, only highlights one at a time. Separate each entry with a comma and enter in the order you want them highlighted.",
      section = "Callers"
   )
   default String callers() {
      return " ";
   }

   @ConfigItem(
      position = 6,
      keyName = "callerColorNew",
      name = "Caller Color",
      description = "Color of Indicated Callers",
      section = "Callers"
   )
   default Color callerColor() {
      return Color.WHITE;
   }

   @ConfigItem(
      position = 7,
      keyName = "callerHighlightOptionsNew",
      name = "Caller indication methods",
      description = "Location(s) of the overlay",
      section = "Callers"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> callerHighlightOptions() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 9,
      keyName = "callersTargetsNew",
      name = "Calllers' targets",
      description = "Highlights the targets of callers",
      section = "Callers"
   )
   default boolean callersTargets() {
      return true;
   }

   @ConfigItem(
      position = 10,
      keyName = "callerTargetColorNew",
      name = "Callers' targets color",
      description = "Color of the the targets of callers",
      section = "Callers"
   )
   default Color callerTargetColor() {
      return Color.WHITE.darker();
   }

   @ConfigItem(
      position = 11,
      keyName = "callerTargetHighlightOptionsNew",
      name = "Pile indication methods",
      description = "How to highlight the callers' target",
      section = "Callers"
   )
   default Set<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> callerTargetHighlightOptions() {
      return defaultPlayerIndicatorMode;
   }

   @ConfigItem(
      position = 0,
      keyName = "unchargedGloryNew",
      name = "Uncharged Glory Indication",
      description = "Indicates if players have an uncharged glory (this only works if the above head indicator is selected)",
      section = "Miscellaneous"
   )
   default boolean unchargedGlory() {
      return false;
   }
}
