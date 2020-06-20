package com.g0ldensp00n.eggsplosion.handlers;

import com.g0ldensp00n.eggsplosion.handlers.Lobby.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.LobbyManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;

public class PickupDropHandler implements Listener {
  private LobbyManager lobbyManager;

  public PickupDropHandler(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void entityPickupEvent(EntityPickupItemEvent entityPickupItemEvent) {
    Entity entity = entityPickupItemEvent.getEntity();
    if (entity instanceof Player) {
      Player player = (Player) entity;
      Lobby playerLobby = lobbyManager.getPlayersLobby(player);
      if (playerLobby != null && !playerLobby.getMap().getAllowItemPickup()) {
        entityPickupItemEvent.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void entityDropItemEvent(PlayerDropItemEvent entityDropItemEvent) {
      Player player = entityDropItemEvent.getPlayer();
      Lobby playerLobby = lobbyManager.getPlayersLobby(player);
      if (playerLobby != null && !playerLobby.getMap().getAllowItemDrop()) {
        entityDropItemEvent.setCancelled(true);
      }
  }
}
