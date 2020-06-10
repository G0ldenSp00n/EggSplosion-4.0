package com.g0ldensp00n.eggsplosion.handlers;

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

  public DeathMessages(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);
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
            damager.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_DESTROY_EGG, 1, 3);
            Lobby damagerLobby = lobbyManager.getPlayersLobby(damager);
            if (damagerLobby.getGameMode() == GameMode.DEATH_MATCH || damagerLobby.getGameMode() == GameMode.TEAM_DEATH_MATCH) {
              damagerLobby.getScoreboardManager().addScorePlayer(damager);
            }
            Bukkit.getServer().broadcastMessage(player.getDisplayName() + " was scrambled by " + damager.getDisplayName());
          } else {
            damager.playSound(damager.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
          }
        }
      }
    }
  }
}
