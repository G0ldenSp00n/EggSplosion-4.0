package com.g0ldensp00n.eggsplosion.handlers.Core;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes.GameLobby_CaptureTheFlag;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

public class RespawnHandler implements Listener {
  private LobbyManager lobbyManager;
  
  public RespawnHandler(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  List<Location> spawnPoints = new ArrayList<>();

  @EventHandler
  public void playerTakeDamage(EntityDamageEvent entityDamageEvent) {
    if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
      EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;

      if (entityDamageByEntityEvent.getEntity() instanceof Player && entityDamageByEntityEvent.getDamager() instanceof Player) {
        Player player = (Player) entityDamageByEntityEvent.getEntity(); 
        Player damager = (Player) entityDamageByEntityEvent.getDamager();

        if (!lobbyManager.canPlayerAttackPlayer(player, damager)) {
          entityDamageEvent.setCancelled(true);
          return;
        }
      }
    }
      if (entityDamageEvent.getEntity().getType().equals(EntityType.PLAYER)) {
        Player player = (Player) entityDamageEvent.getEntity();
        if ((player.getHealth() - entityDamageEvent.getFinalDamage()) <= 0) {
          entityDamageEvent.setCancelled(true);
          player.setHealth(20);
          Lobby playersLobby = lobbyManager.getPlayersLobby(player);
          Location spawnPoint = null;
          if (playersLobby != null && playersLobby.getScoreManager() != null && playersLobby.getScoreManager().getScoreType() == ScoreType.TEAM) {
            if (playersLobby instanceof GameLobby_CaptureTheFlag) {
              GameLobby_CaptureTheFlag gameLobby = (GameLobby_CaptureTheFlag) playersLobby;
              gameLobby.resetPlayerFlag(player, "has dropped the");
            }
            
            ScoreManager scoreboardManager = playersLobby.getScoreManager();
            if (scoreboardManager.getPlayerTeam(player).equals(scoreboardManager.getTeamA())) {
              spawnPoint = playersLobby.getMap().getSpawnPoint(scoreboardManager.getTeamA());
            } else if (scoreboardManager.getPlayerTeam(player).equals(scoreboardManager.getTeamB())) {
              spawnPoint = playersLobby.getMap().getSpawnPoint(scoreboardManager.getTeamB());
            }
          } else {
            if (playersLobby != null && playersLobby.getMap() != null) {
              spawnPoint = playersLobby.getMap().getSpawnPoint();
            }
          }

          if (spawnPoint != null) {
            player.teleport(spawnPoint);
          } else {
            player.teleport(player.getWorld().getSpawnLocation());
          }
          player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 1, 1);
        }
      }
  }
}
