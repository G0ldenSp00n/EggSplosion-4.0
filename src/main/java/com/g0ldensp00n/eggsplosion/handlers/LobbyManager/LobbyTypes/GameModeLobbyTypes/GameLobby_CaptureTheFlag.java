package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes;

import java.util.List;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameLobby_CaptureTheFlag extends GameLobby {
  public GameLobby_CaptureTheFlag(Plugin plugin, MapManager mapManager, String lobbyName, GameMap gameMap, List<Player> playersInLobby) {
    super(plugin, mapManager, lobbyName, GameMode.CAPTURE_THE_FLAG, gameMap, playersInLobby);
  }

  public void initializeGameLobby() {
    setScoreManager(new ScoreManager(getMap().getPointsToWinCTF(), ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
    randomizeTeams();
    getMap().randomizeTeamSides(scoreManager.getTeams());

    if (getMap().getFlagSpawnDelay() == 0) {
      getMap().spawnFlags();
    } else {
      getMap().clearFlags();
      if (getMap().getFlagSpawnDelay() > 5 && getMap().getDoFlagMessages()) {
           broadcastTitle("", "Flags Spawning in " + getMap().getFlagSpawnDelay(), 0, 21, 0);
      }
      new BukkitRunnable(){
        Integer countDown = getMap().getFlagSpawnDelay();

        @Override
        public void run() {
          if (countDown == 0) {
            cancel();
            getMap().spawnFlags();
            return;
          } else if (countDown <= 5){
            if (getMap().getDoFlagMessages()) {
              broadcastTitle("", "Flags Spawning in " + countDown, 0, 21, 0);
            }
          }
          countDown--;
        }
      }.runTaskTimer(this.plugin, 0, (long) 20);
    }
  }

  public void resetPlayerFlag(Player player, String eventMessage, Boolean addScore) {
    if (getGameMode().equals(GameMode.CAPTURE_THE_FLAG)) {
      if (player.getInventory().getHelmet() != null) {
        if (player.getInventory().getHelmet().getType().equals(Material.BLUE_BANNER)) {
          if (getMap().getDoFlagMessages()) {
            broadcastActionBar(getScoreManager().getPlayerDisplayName(player)  + " " + eventMessage + " " + ChatColor.BLUE + "Blue Team" + ChatColor.RESET + " Flag", true);
          }
          getMap().respawnFlag(getScoreManager().getTeamB());
          if (addScore) {
            getScoreManager().addScorePlayer(player);
          }
          equipPlayer(player);
        } else if (player.getInventory().getHelmet().getType().equals(Material.RED_BANNER)) {
          if (getMap().getDoFlagMessages()) {
            broadcastActionBar(getScoreManager().getPlayerDisplayName(player) + " " + eventMessage + " " + ChatColor.RED + "Red Team" + ChatColor.RESET + " Flag", true);
          }
          getMap().respawnFlag(getScoreManager().getTeamA());
          if (addScore) {
            getScoreManager().addScorePlayer(player);
          }
          equipPlayer(player);
        }
      }
    }
  }

  @Override
  public void handlePlayerLeave(Player player) {
    resetPlayerFlag(player, "has dropped the");
  }

  public void resetPlayerFlag(Player player, String eventMessage) {
    resetPlayerFlag(player, eventMessage, false);
  }
}
