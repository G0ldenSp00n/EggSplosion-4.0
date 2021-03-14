package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes;

import java.util.List;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GameLobby_DeathMatch extends GameLobby {
  public GameLobby_DeathMatch(Plugin plugin, MapManager mapManager, String lobbyName, GameMap gameMap, List<Player> playersInLobby) {
    super(plugin, mapManager, lobbyName, GameMode.DEATH_MATCH, gameMap, playersInLobby);
  }

  public void initializeGameLobby() {
    setScoreManager(new ScoreManager(getMap().getPointsToWinDM(), ScoreType.SOLO, this));
  }
}
