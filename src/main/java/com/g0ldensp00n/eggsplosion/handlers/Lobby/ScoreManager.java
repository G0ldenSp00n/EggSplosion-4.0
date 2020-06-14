package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

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

  public ScoreManager(Integer scoreToWin, ScoreType type, Lobby lobby, ChatColor teamAColor, ChatColor teamBColor, Boolean hideTeamNameTags) {
    this(scoreToWin, type, lobby);

    String teamAName = teamAColor.name().substring(0, 1) + teamAColor.name().toLowerCase().substring(1);
    teamA = scoreboard.registerNewTeam(teamAName + " Team");
    teamA.setPrefix("" + teamAColor);
    teamA.setColor(teamAColor);
    teamA.setDisplayName(teamAColor + teamAName + " Team");
    if (hideTeamNameTags) {
      teamA.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
    }

    String teamBName = teamBColor.name().substring(0, 1) + teamBColor.name().toLowerCase().substring(1);
    teamB = scoreboard.registerNewTeam(teamBName + " Team");
    teamB.setPrefix("" + teamBColor);
    teamB.setColor(teamBColor);
    teamB.setDisplayName(teamBColor + teamBName + " Team");
    if (hideTeamNameTags) {
      teamB.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
    }
  }

  public ScoreManager(ScoreType type, Lobby lobby, ChatColor teamAColor, ChatColor teamBColor, Boolean hideTeamNameTags) {
    this(-1, type, lobby, teamAColor, teamBColor, hideTeamNameTags);
  }

  public ScoreManager(Integer scoreToWin, ScoreType type, Lobby lobby) {
    this.scoreToWin = scoreToWin;
    this.scoreType = type;
    this.lobby = lobby;

    scoreboardManager = Bukkit.getScoreboardManager();
    scoreboard = scoreboardManager.getNewScoreboard();
    scoreObjective = scoreboard.registerNewObjective("score", "dummy", "Score");
    if (scoreType != ScoreType.TRACKING) {
      scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
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
    Team playerTeam = scoreboard.getEntryTeam(player.getName());
    return playerTeam;
  }

  public void setPlayerTeam(Player player, Team team) {
    if (team == getTeamA()) {
      if (!getTeamA().equals(getPlayerTeam(player))) {
        getTeamA().addEntry(player.getName());
        getTeamB().removeEntry(player.getName());
      }
    } else if (team == getTeamB()) {
      if (!getTeamB().equals(getPlayerTeam(player))) {
        getTeamB().addEntry(player.getName());
        getTeamA().removeEntry(player.getName());
      }
    }
  }

  public void setPlayerScoreboard(Player player) {
      Scoreboard gameScoreboard = getScoreboard();
      player.setScoreboard(gameScoreboard);
  }

  public void scoreFreeze () {
    scoreFrozen = true;
  }

  public String getPlayerDisplayName(Player player) {
    if (getPlayerTeam(player) != null && getPlayerTeam(player).equals(teamA)) {
      return teamA.getPrefix() + player.getName() + ChatColor.RESET;
    } else if (getPlayerTeam(player) != null && getPlayerTeam(player).equals(teamB)) {
      return teamB.getPrefix() + player.getName() + ChatColor.RESET;
    } else {
      return player.getName();
    }
  }

  public void addScorePlayer(Player player) {
    if (lobby.playerInLobby(player) && !scoreFrozen) {
      Integer newScore = 0;
      switch(scoreType) {
        case SOLO:
          Score playerScore = scoreObjective.getScore(player.getName());
          newScore = playerScore.getScore() + 1;
          playerScore.setScore(newScore);
          break;
        case TEAM:
          Team playerTeam = scoreboard.getEntryTeam(player.getName());
          Score teamScore = scoreObjective.getScore(playerTeam.getDisplayName());

          newScore = teamScore.getScore() + 1;
          teamScore.setScore(newScore);
          break;
        case INFO:
          break;
        case TRACKING:
          break;
      }

      if (scoreToWin != -1 && (newScore >= scoreToWin)) {
        lobby.playerWon(player);
      }
    }
  }

  public void initializeScorePlayer(Player player) {
    if (lobby.playerInLobby(player)) {
      switch(scoreType) {
        case SOLO:
          Score playerScore = scoreObjective.getScore(player.getName());
          playerScore.setScore(0);
          break;
        case TEAM:
          Score teamAScore = scoreObjective.getScore(getTeamA().getDisplayName());
          Score teamBScore = scoreObjective.getScore(getTeamB().getDisplayName());

          teamAScore.setScore(0);
          teamBScore.setScore(0);
          break;
        case INFO:
          break;
        case TRACKING:
          break;
      }
    }
  }
}
