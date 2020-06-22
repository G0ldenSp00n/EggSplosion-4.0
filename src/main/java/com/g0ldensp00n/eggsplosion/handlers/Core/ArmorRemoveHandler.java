package com.g0ldensp00n.eggsplosion.handlers.Core;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.plugin.Plugin;

public class ArmorRemoveHandler implements Listener {
  private LobbyManager lobbyManager;

  public ArmorRemoveHandler(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void inventoryClickEvent(InventoryClickEvent inventoryClickEvent) {
    if (inventoryClickEvent.getSlotType().equals(SlotType.ARMOR) && inventoryClickEvent.getWhoClicked() instanceof Player) {
      Lobby playerLobby = lobbyManager.getPlayersLobby((Player) inventoryClickEvent.getWhoClicked());

      if (playerLobby != null) {
        // Helmet Removal
        if (inventoryClickEvent.getSlot() == 39) {
          if (playerLobby.getMap() != null && !playerLobby.getMap().getAllowHelmetRemoval()) {
            inventoryClickEvent.setCancelled(true);
          }
        }

        // Chestplate Removal
        if (inventoryClickEvent.getSlot() == 38) {
          if (playerLobby.getMap() != null && !playerLobby.getMap().getAllowChestplateRemoval()) {
            inventoryClickEvent.setCancelled(true);
          }
        }

        // Legging Removal
        if (inventoryClickEvent.getSlot() == 37) {
          if (playerLobby.getMap() != null && !playerLobby.getMap().getAllowLeggingRemoval()) {
            inventoryClickEvent.setCancelled(true);
          }
        }

        // Boot Removal
        if (inventoryClickEvent.getSlot() == 36) {
          if (playerLobby.getMap() != null && !playerLobby.getMap().getAllowBootRemoval()) {
            inventoryClickEvent.setCancelled(true);
          }
        }
      }
    }
  }
}
