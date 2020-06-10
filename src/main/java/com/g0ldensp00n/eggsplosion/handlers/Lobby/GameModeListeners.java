package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class GameModeListeners implements Listener {
  private Plugin plugin;
  private LobbyManager lobbyManager;
  private MapManager mapManager;

  public GameModeListeners(Plugin plugin, LobbyManager lobbyManager, MapManager mapManager) {
    this.plugin = plugin;
    this.lobbyManager = lobbyManager;
    this.mapManager = mapManager;

    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
    Player player = playerInteractEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby.getGameMode() == GameMode.CAPTURE_THE_FLAG) {
      if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
        playerInteractEvent.setCancelled(true);
        Block clickedBlock = playerInteractEvent.getClickedBlock();
        Location locationBelowClickedBlock = playerInteractEvent.getClickedBlock().getLocation().clone();
        Block blockBelowClickedBlock = locationBelowClickedBlock.getBlock();
        if (playerLobby.getScoreboardManager().getTeamA().hasEntry(player.getDisplayName())) {
          if (clickedBlock.getType().equals(Material.BLUE_BANNER) || blockBelowClickedBlock.getType().equals(Material.BLUE_BANNER)) {
            ItemStack blueFlag = new ItemStack(Material.BLUE_BANNER);
            player.getInventory().setHelmet(blueFlag);
            clickedBlock.setType(Material.AIR);
            playerLobby.broadcastMessage(player.getDisplayName() + " has picked up the " + ChatColor.BLUE + "Blue Team" + ChatColor.RESET + " Flag");
          }
        }

        if (playerLobby.getScoreboardManager().getTeamB().hasEntry(player.getDisplayName())) {
          if (clickedBlock.getType().equals(Material.RED_BANNER) || blockBelowClickedBlock.getType().equals(Material.RED_BANNER)) {
            ItemStack redFlag = new ItemStack(Material.RED_BANNER);
            player.getInventory().setHelmet(redFlag);
            clickedBlock.setType(Material.AIR);
            playerLobby.broadcastMessage(player.getDisplayName() + " has picked up the " + ChatColor.RED + "Red Team" + ChatColor.RESET + " Flag");
          }
        }
      }
    }
  }


  @EventHandler
  public void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
    Player player = playerMoveEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby.getGameMode() == GameMode.CAPTURE_THE_FLAG) {
      if (player.getInventory().getHelmet().getType().equals(Material.RED_BANNER)) {
        if (player.getLocation().distance(playerLobby.getMap().getTeamBFlagLocation()) < 5) {
          playerLobby.equipPlayer(player);
          playerLobby.broadcastMessage(player.getDisplayName() + " has captured the " + ChatColor.RED + "Red Team" + ChatColor.RESET + " Flag");
          playerLobby.getMap().respawnFlag(Team.TEAM_B);
          playerLobby.getScoreboardManager().addScorePlayer(player);
        }
      } else if (player.getInventory().getHelmet().getType().equals(Material.BLUE_BANNER)) {
        if (player.getLocation().distance(playerLobby.getMap().getTeamAFlagLocation()) < 5) {
          playerLobby.equipPlayer(player);
          playerLobby.broadcastMessage(player.getDisplayName() + " has captured the " + ChatColor.BLUE + "Blue Team" + ChatColor.RESET + " Flag");
          playerLobby.getMap().respawnFlag(Team.TEAM_B);
          playerLobby.getScoreboardManager().addScorePlayer(player);
        }
      }
    }
  }
}
