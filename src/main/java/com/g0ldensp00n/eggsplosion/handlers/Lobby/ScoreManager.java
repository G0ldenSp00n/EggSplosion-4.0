package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

public class ScoreManager {
  private Integer scoreToWin;
  private ScoreType scoreType;
  private ScoreboardManager scoreboardManager;
  private Scoreboard scoreboard;
  private Objective scoreObjective;
  private Lobby lobby;
  private Boolean scoreFrozen = false;
  private Team teamA;
  private Team teamB;

  public ScoreManager(Integer scoreToWin, ScoreType type, Lobby lobby) {
    this.scoreToWin = scoreToWin;
    this.scoreType = type;
    this.lobby = lobby;

    scoreboardManager = Bukkit.getScoreboardManager();
    scoreboard = scoreboardManager.getNewScoreboard();
    scoreObjective = scoreboard.registerNewObjective("score", "dummy", "Score");
    scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

    teamA = scoreboard.registerNewTeam("Red Team");
    teamA.setPrefix("" + ChatColor.RED);
    teamA.setColor(ChatColor.RED);
    teamA.setDisplayName(ChatColor.RED + "Red Team");
    teamA.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);

    teamB = scoreboard.registerNewTeam("Blue Team");
    teamA.setPrefix("" + ChatColor.BLUE);
    teamB.setColor(ChatColor.BLUE);
    teamB.setDisplayName(ChatColor.BLUE + "Blue Team");
    teamB.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
  }

  public Team getTeamA() {
    return teamA;
  }

  public Team getTeamB() {
    return teamB;
  }

  public Scoreboard getScoreboard() {
    return scoreboard;
  }

  public ScoreType getScoreType() {
    return scoreType;
  }

  public Team getPlayerTeam(Player player) {
    Team playerTeam = scoreboard.getEntryTeam(player.getDisplayName());
    return playerTeam;
  }

  public void scoreFreeze () {
    scoreFrozen = true;
  }

  public void addScorePlayer(Player player) {
    if (lobby.playerInLobby(player) && !scoreFrozen) {
      Integer newScore = 0;
      switch(scoreType) {
        case SOLO:
          Score playerScore = scoreObjective.getScore(player.getDisplayName());
          newScore = playerScore.getScore() + 1;
          playerScore.setScore(newScore);
          break;
        case TEAM:
          Team playerTeam = scoreboard.getEntryTeam(player.getDisplayName());
          Score teamScore = scoreObjective.getScore(playerTeam.getDisplayName());

          newScore = teamScore.getScore() + 1;
          teamScore.setScore(newScore);
          break;
        case INFO:
          break;
      }

      if (scoreToWin != -1 && (newScore >= scoreToWin)) {
        lobby.playerWon(player);
      }
    }
  }
}
