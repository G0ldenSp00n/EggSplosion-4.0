package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class GameLobby_CapturePoint extends GameLobby {
  private Map<String, List<Player>> pointsCapturers;
  private Map<String, Map<Team, Integer>> capturePointsPointsByTeam;
  private Map<String, BossBar> capturePointBars;
  public GameLobby_CapturePoint(Plugin plugin, MapManager mapManager, String lobbyName, GameMap gameMap, List<Player> playersInLobby) {
    super(plugin, mapManager, lobbyName, GameMode.CAPTURE_POINT, gameMap, playersInLobby);

    pointsCapturers = new Hashtable<>();
    capturePointsPointsByTeam = new Hashtable<>();
    capturePointBars = new Hashtable<>();
    for (String capturePointName : gameMap.getAllCapturePointName()) {
      pointsCapturers.put(capturePointName, new ArrayList<Player>());
      BossBar bossBar = Bukkit.createBossBar(capturePointName, BarColor.WHITE, BarStyle.SOLID);
      bossBar.setProgress(0);
      capturePointBars.put(capturePointName, bossBar);
    }

    handlePointCapture();
  }

  protected void initializeGameLobby() {
    setScoreManager(new ScoreManager(30, ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
    randomizeTeams();
    getMap().randomizeTeamSides(scoreManager.getTeams());
    getMap().spawnCapturePoints();
  }

  @Override
  protected void handlePlayerLeave(Player player) {
    for (String capturePointName : getMap().getAllCapturePointName()) {
      playerStopCapturingPoint(capturePointName, player);
    }
  }

  private void handlePointCapture() {
    new BukkitRunnable(){
      Integer counter = 0;
      @Override
      public void run() {
        if (!getScoreManager().isFrozen()) {
          List<String> capturePointNames = getMap().getAllCapturePointName();
          for (String capturePointName : capturePointNames) {
            List<Player> pointCapturers = pointsCapturers.get(capturePointName);
            if (pointCapturers != null && pointCapturers.size() != 0) {
              if (capturingByOneTeam(capturePointName)) {
                Team capturingTeam = getScoreManager().getPlayerTeam(pointCapturers.get(0));
                Map<Team, Integer> capturePointPointsByTeam = capturePointsPointsByTeam.get(capturePointName);
                if (capturePointPointsByTeam == null) {
                  capturePointPointsByTeam = new Hashtable<Team, Integer>();
                  capturePointsPointsByTeam.put(capturePointName, capturePointPointsByTeam);
                }

                Integer capturingTeamPoints = handleCaptureProgress(capturePointName, capturePointPointsByTeam, capturingTeam, pointCapturers.size());
                if (capturingTeamPoints >= 20) {
                  if (counter % 20 == 0) {
                    getScoreManager().addScoreTeam(capturingTeam);
                  }
                }
              }
            } else {
              Map<Team, Integer> capturePointPointsByTeam = capturePointsPointsByTeam.get(capturePointName);
              if (capturePointPointsByTeam != null) {
                for (Team team : capturePointPointsByTeam.keySet()) {
                  Integer capturingTeamPoints = capturePointPointsByTeam.get(team);
                  if (capturingTeamPoints >= 20) {
                    if (counter % 20 == 0) {
                      getScoreManager().addScoreTeam(team);
                    }
                  }
                }
              }
            }
          }
          counter ++;
        } else {
          cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0, (long) 1 * 20);
  }

  private Integer handleCaptureProgress(String capturePointName, Map<Team, Integer> capturePointPointsByTeam, Team capturingTeam, Integer numberOfPlayers) {
    Boolean otherTeamHasProgress = false;
    for (Team team : capturePointPointsByTeam.keySet()) {
      if (!team.equals(capturingTeam)) {
        Integer otherTeamPoints = capturePointPointsByTeam.get(team);
        if (otherTeamPoints > 0) {
          otherTeamHasProgress = true;
          otherTeamPoints -= numberOfPlayers;
          capturePointPointsByTeam.put(team, --otherTeamPoints);
          capturePointBars.get(capturePointName).setProgress((1.0f/20) * otherTeamPoints);
        }
      }
    }

    Integer capturingTeamPoints = capturePointPointsByTeam.get(capturingTeam);
    if (!otherTeamHasProgress) {
      if (capturingTeamPoints != null && capturingTeamPoints < 20) {
        capturingTeamPoints += numberOfPlayers;
        capturePointPointsByTeam.put(capturingTeam, capturingTeamPoints);
        capturePointBars.get(capturePointName).setColor(BarColor.valueOf(capturingTeam.getColor().name()));
        capturePointBars.get(capturePointName).setProgress((1.0f/20) * capturingTeamPoints);
        if (capturingTeamPoints >= 20) {
          broadcastMessage(capturingTeam.getDisplayName() + ChatColor.RESET + " has captured point " + ChatColor.GREEN + capturePointName);
        } else if (capturingTeamPoints == 1) {
          broadcastMessage(capturingTeam.getDisplayName() + ChatColor.RESET + " is capturing point " + ChatColor.GREEN + capturePointName);
        }
      } else if (capturingTeamPoints == null){
        capturePointPointsByTeam.put(capturingTeam, 1);
        capturePointBars.get(capturePointName).setColor(BarColor.valueOf(capturingTeam.getColor().name()));
        broadcastMessage(capturingTeam.getDisplayName() + ChatColor.RESET + " is capturing point " + ChatColor.GREEN + capturePointName);
        capturePointBars.get(capturePointName).setProgress((1.0f/20) * 1);
      }
    }

    if (capturingTeamPoints != null) {
      return capturingTeamPoints; 
    }
    return 0;
  }

  public Boolean capturingByOneTeam(String capturePointName) {
    List<Player> pointCapturers = pointsCapturers.get(capturePointName);
    if (pointCapturers != null && pointCapturers.size() > 0) {
      Team playerTeam = getScoreManager().getPlayerTeam(pointCapturers.get(0));
      for (Player player : pointCapturers) {
        if (!getScoreManager().getPlayerTeam(player).equals(playerTeam)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public void playerCapturingPoint(String capturePointName, Player player) {
    List<Player> pointCapturers = pointsCapturers.get(capturePointName);
    capturePointBars.get(capturePointName).addPlayer(player);
    if (pointCapturers != null && !pointCapturers.contains(player)) {
      pointCapturers.add(player);
    }
  }

  public void playerStopCapturingPoint(String capturePointName, Player player) {
    List<Player> pointCapturers = pointsCapturers.get(capturePointName);
    capturePointBars.get(capturePointName).removePlayer(player);
    if (pointCapturers != null) {
      pointCapturers.remove(player);
    }
  }
}
