package net.runelite.client.plugins.microbot.playerindicatorsextended;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;

public class PlayerIndicatorsExtendedService {
   private final Client client;
   private final PlayerIndicatorsExtendedConfig config;
   private final Predicate<Player> self;
   private final Predicate<Player> friend;
   private final Predicate<Player> clan;
   private final Predicate<Player> team;
   private final Predicate<Player> target;
   private final Predicate<Player> other;
   private final Predicate<Player> caller;
   private final Predicate<Player> callerTarget;

   @Inject
   private PlayerIndicatorsExtendedService(Client client, PlayerIndicatorsExtendedPlugin plugin, PlayerIndicatorsExtendedConfig config) {
      this.client = client;
      this.config = config;
      this.self = (player) -> client.getLocalPlayer().equals(player) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.SELF);
      this.friend = (player) -> !player.equals(client.getLocalPlayer()) && client.isFriended(player.getName(), false) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND);
      this.clan = (player) -> player.isFriendsChatMember() && !client.getLocalPlayer().equals(player) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN);
      this.team = (player) -> ((Player)Objects.requireNonNull(client.getLocalPlayer())).getTeam() != 0 && !player.isFriendsChatMember() && !client.isFriended(player.getName(), false) && client.getLocalPlayer().getTeam() == player.getTeam() && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM);
      this.target = (player) -> !this.team.test(player) && !this.clan.test(player) && !client.isFriended(player.getName(), false) && plugin.isAttackable(client, player) && !client.getLocalPlayer().equals(player) && !this.clan.test(player) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET);
      this.caller = (player) -> plugin.isCaller(player) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER);
      this.callerTarget = (player) -> plugin.isPile(player) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET);
      this.other = (player) -> !plugin.isAttackable(client, player) && !client.getLocalPlayer().equals(player) && !this.team.test(player) && !this.clan.test(player) && !client.isFriended(player.getName(), false) && plugin.getLocationHashMap().containsKey(PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER);
   }

   public void forEachPlayer(BiConsumer<Player, PlayerIndicatorsExtendedPlugin.PlayerRelation> consumer) {
      if (this.highlight()) {
         for(Player p : this.client.getPlayers()) {
            if (this.caller.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER);
            } else if (this.callerTarget.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.CALLER_TARGET);
            } else if (this.other.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.OTHER);
            } else if (this.self.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.SELF);
            } else if (this.friend.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.FRIEND);
            } else if (this.clan.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.CLAN);
            } else if (this.team.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.TEAM);
            } else if (this.target.test(p)) {
               consumer.accept(p, PlayerIndicatorsExtendedPlugin.PlayerRelation.TARGET);
            }
         }

      }
   }

   private boolean highlight() {
      return this.config.highlightOwnPlayer() || this.config.highlightClan() || this.config.highlightFriends() || this.config.highlightOtherPlayers() || this.config.highlightTargets() || this.config.highlightCallers() || this.config.highlightTeamMembers() || this.config.callersTargets();
   }
}
