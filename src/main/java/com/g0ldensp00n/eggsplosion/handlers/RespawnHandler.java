package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

import com.g0ldensp00n.eggsplosion.handlers.Lobby.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.ScoreType;

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
          if (playersLobby != null && playersLobby.getScoreboardManager() != null && playersLobby.getScoreboardManager().getScoreType() == ScoreType.TEAM) {
            if (playersLobby.getGameMode() == GameMode.CAPTURE_THE_FLAG) {
              if (player.getInventory().getHelmet() != null) {
                if (player.getInventory().getHelmet().getType().equals(Material.BLUE_BANNER)) {
                  if (playersLobby.getMap().getDoFlagMessages()) {
                    playersLobby.broadcastActionBar(playersLobby.getScoreboardManager().getPlayerDisplayName(player) + " has dropped the " + ChatColor.BLUE + "Blue Team" + ChatColor.RESET + " Flag", true);
                  }
                  playersLobby.getMap().respawnFlag(playersLobby.getScoreboardManager().getTeamB());
                } else if (player.getInventory().getHelmet().getType().equals(Material.RED_BANNER)) {
                  if (playersLobby.getMap().getDoFlagMessages()) {
                    playersLobby.broadcastActionBar(playersLobby.getScoreboardManager().getPlayerDisplayName(player) + " has dropped the " + ChatColor.RED + "Red Team" + ChatColor.RESET + " Flag", true);
                  }
                  playersLobby.getMap().respawnFlag(playersLobby.getScoreboardManager().getTeamA());
                }
                playersLobby.equipPlayer(player);
              }
            }
            
            ScoreManager scoreboardManager = playersLobby.getScoreboardManager();
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
