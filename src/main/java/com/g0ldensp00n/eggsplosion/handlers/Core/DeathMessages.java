package com.g0ldensp00n.eggsplosion.handlers.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes.GameLobby_DeathMatch;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes.GameLobby_TeamDeathMatch;

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
            if (damagerLobby instanceof GameLobby_DeathMatch || damagerLobby instanceof GameLobby_TeamDeathMatch) {
              damagerLobby.getScoreManager().addScorePlayer(damager);
            }

            Random random = new Random();
            String deathMessage = deathMessagesPlayerOnPlayer.get(random.nextInt(deathMessagesPlayerOnPlayer.size()));
            if (damagerLobby != null) {
              damagerLobby.broadcastMessage(damagerLobby.getScoreManager().getPlayerDisplayName(player) + " " + deathMessage + " " + damagerLobby.getScoreManager().getPlayerDisplayName(damager));
            }
          } else {
            damager.playSound(damager.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
          }
        }
      }
    }
  }
}
