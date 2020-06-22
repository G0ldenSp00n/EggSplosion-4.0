package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;
import com.g0ldensp00n.eggsplosion.handlers.Utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class GameLobby extends Lobby {
  public GameLobby(Plugin plugin, MapManager mapManager, String lobbyName, GameMode gameMode, GameMap gameMap, List<Player> playersInLobby) {
    super(plugin, mapManager, lobbyName, gameMode, gameMap, playersInLobby);
  }

  public void spawnPlayerInMap(Player player) {
    ScoreManager scoreManager = getScoreManager();
    ScoreType scoreType = scoreManager.getScoreType();
    if (scoreType == ScoreType.SOLO) {
      Location spawnPoint = getMap().getSpawnPoint();
      player.teleport(spawnPoint);
    } else if (scoreType == ScoreType.TEAM) {
      Location spawnPoint = getMap().getSpawnPoint(scoreManager.getPlayerTeam(player));
      if (spawnPoint != null) {
        player.teleport(spawnPoint);
      }
    }
  }

  public void equipPlayer(Player player) {
    player.getInventory().clear();
    equipInventory(player);
    ScoreManager scoreManager = getScoreManager();
    if (scoreManager != null && scoreManager.getScoreType() == ScoreType.TEAM) {
      Color armorColor = Utils.chatColorToColor(scoreManager.getPlayerTeam(player).getColor());
      equipArmor(player, armorColor);
    }
  }

  protected void handlePlayerJoin(Player player) {
    throw new Error("Can't join an in progress lobby");
  }

  protected void handlePlayerLeave(Player player) {
    return;
  }

  protected void handleMapChange(GameMap gameMap) {
    return;
  }

  protected void handleScoreManagerChange(Player player, ScoreManager scoreManager) {
    if (scoreManager != null) {
      scoreManager.setPlayerScoreboard(player);
      scoreManager.initializeScorePlayer(player);
    }
  }
  
  protected void handleGameModeChange(GameMode gameMode) {
    switch (gameMode) {
      case DEATH_MATCH:
        setScoreManager(new ScoreManager(getMap().getPointsToWinDM(), ScoreType.SOLO, this));
        break;
      case TEAM_DEATH_MATCH:
        setScoreManager(new ScoreManager(getMap().getPointsToWinCTF(), ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
        randomizeTeams();
        getMap().randomizeTeamSides(scoreManager.getTeams());
        break;
      case CAPTURE_THE_FLAG:
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
        break;
      default:
        throw new Error("Not Implemented");
    }

    for (Player player : getPlayers()) {
      equipPlayer(player);
      spawnPlayerInMap(player);
    }
  }

  public void playerWon(Player player) {
    scoreManager.scoreFreeze();
    if (this.scoreManager.getScoreType() == ScoreType.SOLO) {
      broadcastTitle(scoreManager.getPlayerDisplayName(player), ChatColor.GOLD + " has won the game!", 0, 21, 0);
      player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
    } else if (this.scoreManager.getScoreType() == ScoreType.TEAM) {
      Team playerTeam = this.scoreManager.getPlayerTeam(player);
      broadcastTitle(scoreManager.getPlayerTeam(player).getDisplayName(), ChatColor.GOLD + " has won the game!", 0, 21, 0);
      for(Player playerOnTeam : getPlayers()) {
        if (playerTeam.equals(scoreManager.getPlayerTeam(playerOnTeam))) {
          playerOnTeam.playSound(playerOnTeam.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        } else {
          playerOnTeam.playSound(playerOnTeam.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
        }
      }
    }
    new BukkitRunnable(){
        Integer countDown = 6;

        @Override
        public void run() {
          if (scoreManager.getScoreType() == ScoreType.TEAM) {
            Team playerTeam = getScoreManager().getPlayerTeam(player);
            for(Player playerOnTeam : getPlayers()) {
              if (playerTeam.equals(scoreManager.getPlayerTeam(playerOnTeam))) {
                Firework firework = (Firework) playerOnTeam.getWorld().spawnEntity(playerOnTeam.getLocation(), EntityType.FIREWORK);
                FireworkMeta fireworkMeta = firework.getFireworkMeta();
                FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(Color.WHITE).build();
                fireworkMeta.addEffect(fireworkEffect);
                fireworkMeta.setPower(2);

                firework.setFireworkMeta(fireworkMeta);
              }
            }
          }
          if (countDown == 0) {
            cancel();
            List<Player> playersToMove = getPlayers();

            removeAllPlayers();
            WaitingLobby waitingLobby = new WaitingLobby(plugin, 10, mapManager, getLobbyName());

            for (Player player : playersToMove) {
              waitingLobby.addPlayer(player);
            }
            LobbyManager.getInstance(plugin, mapManager).replaceLobby(getLobbyName(), waitingLobby);
            return;
          } else if (countDown <= 5) {
            broadcastTitle("Return to waiting room", "" + countDown, 0, 21, 0);
          }
          countDown--;
        }
      }.runTaskTimer(this.plugin, 0, (long) 20);

  }

  public void rotateSides() {
    if (getMap().getDoSideSwitch()) {
      getMap().switchTeamSides();
      getMap().spawnFlags();
      for(Player player: getPlayers()) {
        equipPlayer(player);
      }
      broadcastTitle("Switching Sides", "", 0, 40, 0);
      for (Player player : getPlayers()) {
        spawnPlayerInMap(player);
      }
    }
  }

  public void randomizeTeams() {
    Random random = new Random();
    ScoreManager sm = getScoreManager();
    if (sm != null) {
      List<Player> playersToAdd = new ArrayList<>(getPlayers());
      Boolean teamA = true;
      while (playersToAdd.size() > 0) {
        Integer nextPlayer = random.nextInt(playersToAdd.size());
        Player player = playersToAdd.get(nextPlayer);
        if (teamA) {
          sm.getTeamA().addEntry(player.getName());;
        } else {
          sm.getTeamB().addEntry(player.getName());;
        }
        teamA = !teamA;
        playersToAdd.remove(player);
      }
    }
  }
}
