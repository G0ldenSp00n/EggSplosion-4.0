package com.g0ldensp00n.eggsplosion.handlers.GameModeManager;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;

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
  private LobbyManager lobbyManager;

  public GameModeListeners(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;

    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
    Player player = playerInteractEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby != null && playerLobby.getGameMode() == GameMode.CAPTURE_THE_FLAG) {
      if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
        Block clickedBlock = playerInteractEvent.getClickedBlock();
        Location locationBelowClickedBlock = playerInteractEvent.getClickedBlock().getLocation().clone();
        Block blockBelowClickedBlock = locationBelowClickedBlock.getBlock();
        ScoreManager scoreManager = playerLobby.getScoreManager();
        if (scoreManager.getPlayerTeam(player).equals(scoreManager.getTeamA())) {
          if (clickedBlock.getType().equals(Material.BLUE_BANNER) || blockBelowClickedBlock.getType().equals(Material.BLUE_BANNER)) {
            playerInteractEvent.setCancelled(true);
            ItemStack blueFlag = new ItemStack(Material.BLUE_BANNER);
            player.getInventory().setHelmet(blueFlag);
            clickedBlock.setType(Material.AIR);
            if (playerLobby.getMap().getDoFlagMessages()) {
              playerLobby.broadcastActionBar(scoreManager.getPlayerDisplayName(player) + " has picked up the " + ChatColor.BLUE + "Blue Team" + ChatColor.RESET + " Flag", true);
            }
          }
        }

        if (scoreManager.getPlayerTeam(player).equals(scoreManager.getTeamB())) {
          if (clickedBlock.getType().equals(Material.RED_BANNER) || blockBelowClickedBlock.getType().equals(Material.RED_BANNER)) {
            playerInteractEvent.setCancelled(true);
            ItemStack redFlag = new ItemStack(Material.RED_BANNER);
            player.getInventory().setHelmet(redFlag);
            clickedBlock.setType(Material.AIR);
            if (playerLobby.getMap().getDoFlagMessages()) {
              playerLobby.broadcastActionBar(scoreManager.getPlayerDisplayName(player) + " has picked up the " + ChatColor.RED + "Red Team" + ChatColor.RESET + " Flag", true);
            }
          }
        }
      }
    }
  }


  @EventHandler
  public void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
    Player player = playerMoveEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby != null && playerLobby.getGameMode() == GameMode.CAPTURE_THE_FLAG) {
      if (player.getInventory().getHelmet() != null) {
        if (player.getInventory().getHelmet().getType().equals(Material.RED_BANNER)) {
          Location playerFlagLocation = playerLobby.getMap().getSideFlagLocation(playerLobby.getMap().getTeamSide(playerLobby.getScoreManager().getTeamB()));
          playerFlagLocation.add(0, 1, 0);
          if (player.getLocation().distance(playerFlagLocation) < 5 && playerFlagLocation.getBlock().getType().equals(Material.BLUE_BANNER)) {
            playerLobby.equipPlayer(player);
            if (playerLobby.getMap().getDoFlagMessages()) {
              playerLobby.broadcastActionBar(playerLobby.getScoreManager().getPlayerDisplayName(player) + " has captured the " + ChatColor.RED + "Red Team" + ChatColor.RESET + " Flag", true);
            }
            playerLobby.getMap().respawnFlag(playerLobby.getScoreManager().getTeamA());
            playerLobby.getScoreManager().addScorePlayer(player);
          }
        } else if (player.getInventory().getHelmet().getType().equals(Material.BLUE_BANNER)) {
          Location playerFlagLocation = playerLobby.getMap().getSideFlagLocation(playerLobby.getMap().getTeamSide(playerLobby.getScoreManager().getTeamA()));
          playerFlagLocation.add(0, 1, 0);
          if (player.getLocation().distance(playerFlagLocation) < 5 && playerFlagLocation.getBlock().getType().equals(Material.RED_BANNER)) {
            playerLobby.equipPlayer(player);
            if (playerLobby.getMap().getDoFlagMessages()) {
              playerLobby.broadcastActionBar(playerLobby.getScoreManager().getPlayerDisplayName(player) + " has captured the " + ChatColor.BLUE + "Blue Team" + ChatColor.RESET + " Flag", true);
            }
            playerLobby.getMap().respawnFlag(playerLobby.getScoreManager().getTeamB());
            playerLobby.getScoreManager().addScorePlayer(player);
          }
        }
      }
    }
  }
}
