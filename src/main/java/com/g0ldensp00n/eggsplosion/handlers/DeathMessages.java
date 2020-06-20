package com.g0ldensp00n.eggsplosion.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.g0ldensp00n.eggsplosion.handlers.Lobby.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.LobbyManager;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class DeathMessages implements Listener {
  private LobbyManager lobbyManager;
  private List<String> deathMessagesPlayerOnPlayer;

  public DeathMessages(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);

    deathMessagesPlayerOnPlayer = new ArrayList<>();
    deathMessagesPlayerOnPlayer.add("was scrambled by");
    deathMessagesPlayerOnPlayer.add("was poached by");
    deathMessagesPlayerOnPlayer.add("was shelled by");
    deathMessagesPlayerOnPlayer.add("was Humpty Dumptied by");
  }

  @EventHandler
  public void playerTakeDamage(EntityDamageByEntityEvent entityDamageEvent) {
    if (entityDamageEvent.getEntity() instanceof Player && entityDamageEvent.getDamager() instanceof Player) {
      Player player = (Player) entityDamageEvent.getEntity(); 
      Player damager = (Player) entityDamageEvent.getDamager();

      if (!lobbyManager.canPlayerAttackPlayer(player, damager)) {
        entityDamageEvent.setCancelled(true);
        return;
      }

      if (damager != null && player != null) {
        if(entityDamageEvent.getCause() == DamageCause.ENTITY_EXPLOSION) {
          if ((player.getHealth() - entityDamageEvent.getFinalDamage()) <= 0) {
            damager.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            Lobby damagerLobby = lobbyManager.getPlayersLobby(damager);
            if (damagerLobby.getGameMode() == GameMode.DEATH_MATCH || damagerLobby.getGameMode() == GameMode.TEAM_DEATH_MATCH) {
              damagerLobby.getScoreboardManager().addScorePlayer(damager);
            }

            Random random = new Random();
            String deathMessage = deathMessagesPlayerOnPlayer.get(random.nextInt(deathMessagesPlayerOnPlayer.size()));
            if (damagerLobby != null) {
              damagerLobby.broadcastMessage(damagerLobby.getScoreboardManager().getPlayerDisplayName(player) + " " + deathMessage + " " + damagerLobby.getScoreboardManager().getPlayerDisplayName(damager));
            }
          } else {
            damager.playSound(damager.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
          }
        }
      }
    }
  }
}
