package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes;

import java.util.List;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GameLobby_TeamDeathMatch extends GameLobby {
  public GameLobby_TeamDeathMatch(Plugin plugin, MapManager mapManager, String lobbyName, GameMap gameMap, List<Player> playersInLobby) {
    super(plugin, mapManager, lobbyName, GameMode.TEAM_DEATH_MATCH, gameMap, playersInLobby);
  }

  public void initializeGameLobby() {
        setScoreManager(new ScoreManager(getMap().getPointsToWinCTF(), ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
        randomizeTeams();
        getMap().randomizeTeamSides(scoreManager.getTeams());
  }
}
