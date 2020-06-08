/*
package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import org.bukkit.entity.Player;

enum GameMode {
  LOBBY,
  DEATH_MATCH
};

public class GameMap {
  private Location cornerA;
  private Location cornerB;
  private Location cornerC;
  private Location cornerD;
  private List<Location> spawnPoints;
  private List<Location> teamASpawnLocations;
  private List<Location> teamBSpawnLocations;
  private List<GameMode> supportedGameModes;
}

public class Lobby {
  private Integer lobbyId;
  private List<Player> playersInLobby;
  private GameMode currentGamemode;
  private Integer maxPlayers;

  public Lobby(GameMode gameMode, Integer maxPlayers) {
    this.currentGamemode = gameMode;
    this.maxPlayers = maxPlayers;
  }

  List<Player> getPlayers() {
    return playersInLobby;
  }
}

public class LobbyManager {
    
    List<Lobby> lobbies = new ArrayList<>();

    public LobbyManager() {
      lobbies.add(new Lobby(GameMode.LOBBY, -1));
    }

    public Lobby getPlayersLobby(UUID playerUuid) {
      for(Lobby lobby: lobbies) {
        for(Player player: lobby.getPlayers()) {
          if (player.getUniqueId() == playerUuid) {
            return lobby;
          }
        }
      }
      return null;
    }

    public Lobby getMainLobby() {
    }


}
*/
