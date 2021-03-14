package com.g0ldensp00n.eggsplosion.handlers.GameModeManager;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes.GameLobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes.GameLobby_CapturePoint;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes.GameLobby_CaptureTheFlag;
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

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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

  public void handleFlagCaptureLogic(Player player, GameLobby gameLobby) {
    if (gameLobby instanceof GameLobby_CaptureTheFlag) {
      GameLobby_CaptureTheFlag ctfLobby = (GameLobby_CaptureTheFlag) gameLobby;
      if (player.getInventory().getHelmet() != null) {
        Location playerFlagLocation = gameLobby.getMap().getSideFlagLocation(gameLobby.getMap().getTeamSide(gameLobby.getScoreManager().getPlayerTeam(player)));
        playerFlagLocation.add(0, 1, 0);
        if (player.getLocation().distance(playerFlagLocation) < 5) {
          ctfLobby.resetPlayerFlag(player, "has captured the", true);
        }
      }
    }
  }

  public void handleCapturePointLogic(Player player, GameLobby gameLobby) {
    if (gameLobby instanceof GameLobby_CapturePoint) {
      GameLobby_CapturePoint cpLobby = (GameLobby_CapturePoint) gameLobby;
      Location playerLocation = player.getLocation();
      for (String capturePointName : gameLobby.getMap().getAllCapturePointName()) {
        if(gameLobby.getMap().getCapturePoint(capturePointName).distance(playerLocation) < 5) {
          cpLobby.playerCapturingPoint(capturePointName, player);
        } else {
          cpLobby.playerStopCapturingPoint(capturePointName, player);
        }
      }
    }
  }

  @EventHandler
  public void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
    Player player = playerMoveEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby instanceof GameLobby) {
      GameLobby gameLobby = (GameLobby) playerLobby;

      handleFlagCaptureLogic(player, gameLobby);

      handleCapturePointLogic(player, gameLobby);
    }
  }
}
