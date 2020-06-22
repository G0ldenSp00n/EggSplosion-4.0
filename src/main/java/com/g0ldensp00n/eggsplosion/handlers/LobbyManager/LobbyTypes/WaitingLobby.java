package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WaitingLobby extends Lobby {
  private Hashtable<Player, Boolean> playerReadyStatus;
  private Map<Player, GameMode> gameModeVotes;
  private Map<Player, String> mapVotes;
  private Integer maxPlayers;

  public WaitingLobby(Plugin plugin, Integer maxPlayers, MapManager mapManager, String lobbyName) {
    super(plugin, mapManager, lobbyName, GameMode.WAITING, mapManager.getMapByName("WAITING_ROOM"));
    this.maxPlayers = maxPlayers;

    gameModeVotes = new Hashtable<Player, GameMode>();
    mapVotes = new Hashtable<Player, String>();

    setScoreManager(new ScoreManager(ScoreType.TRACKING, (Lobby) this, ChatColor.GRAY, ChatColor.GREEN, false));

    playerReadyStatus = new Hashtable<Player, Boolean>();
  }

  public void equipPlayer(Player player) {
    player.getInventory().clear();
    equipInventory(player);
  }

  protected void handlePlayerJoin(Player player) {
    if (getPlayers().size() >= maxPlayers) {
      throw new Error("[EggSplosion] Lobby " + ChatColor.AQUA + getLobbyName() + ChatColor.RESET + " is full, please join a different lobby");
    }
    // Player Ready Status
    playerReadyStatus.put(player, false);
    handleScoreManagerChange(player, getScoreManager());
    getScoreManager().setPlayerTeam(player, getScoreManager().getTeamA());

    // Waiting Room Setup
    player.teleport(getMap().getSpawnPoint());
    equipPlayer(player);
    configurePlayer(player);
    player.sendMessage("[EggSplosion] You have joined lobby " + ChatColor.AQUA + getLobbyName());
  }

  protected void handlePlayerLeave(Player player) {
    playerReadyStatus.remove(player);
    scoreManager.getPlayerTeam(player).removeEntry(player.getName());
  }

  protected void handleMapChange(GameMap gameMap) {
    return;
  }

  protected void handleGameModeChange(GameMode gameMode) {
    if (gameMode != GameMode.WAITING) {
      throw new Error("Waiting Room can't have game mode changed");
    }
  }

  protected void handleScoreManagerChange(Player player, ScoreManager scoreManager) {
    if (scoreManager != null) {
      scoreManager.setPlayerScoreboard(player);
      scoreManager.initializeScorePlayer(player);
    }
  }

  public Boolean isPlayerReady(Player player) {
    return playerReadyStatus.get(player);
  }

  public Boolean allPlayersReady() {
    Boolean allPlayersReady = true;
    for (Player player : getPlayers()) {
      if (!isPlayerReady(player)) {
        allPlayersReady = false;
        break;
      }
    }
    return allPlayersReady;
  }

  public void togglePlayerReady(Player player) {
    playerReadyStatus.put(player, !playerReadyStatus.get(player));
    if (isPlayerReady(player)) {
      getScoreManager().setPlayerTeam(player, getScoreManager().getTeamB());

      if (allPlayersReady()) {
        startGameModeLobby();
      }
    } else {
      getScoreManager().setPlayerTeam(player, getScoreManager().getTeamA());
    }
  }

  private void startGameModeLobby() {
    Integer minimumPlayers = 2;
    if (getPlayers().size() >= minimumPlayers) {
      Boolean allPlayersReady = allPlayersReady();

      if (allPlayersReady) {
        new BukkitRunnable(){
          Integer countDown = 5;

          @Override
          public void run() {
            if (!allPlayersReady()) {
              broadcastTitle("Game Starting", "Cancelling Player Unreadied", 0, 21, 0);
              cancel();
              return;
            } else if (countDown == 0) {
              cancel();
              List<Player> playersToMove = getPlayers();
              GameMode gameMode = tallyGameModeVote();
              String mapName = tallyGameMapVote();
              GameMap gameMap = mapManager.getMapByName(mapName);
              broadcastTitle(gameModeToString(gameMode), "Map - " + mapName, 10, 60, 10);

              // Setup Game Mode Lobby
              removeAllPlayers();
              GameLobby gameLobby = new GameLobby(plugin, mapManager, getLobbyName(), gameMode, gameMap, playersToMove);
              LobbyManager.getInstance(plugin, mapManager).replaceLobby(getLobbyName(), gameLobby);
              return;
            } if (allPlayersReady()) {
              broadcastTitle("Game Starting", ""+countDown--, 0, 21, 0);
            }
          }
        }.runTaskTimer(this.plugin, 0, (long) 20);
      }
    }
  }

  public void registerGameModeVote(GameMode gameMode, Player player) {
    if (getPlayers().contains(player)) {
      if (gameModeVotes.get(player) == null) {
        player.sendMessage("[EggSplosion] Vote Registered for Game Mode " + ChatColor.GREEN + gameModeToString(gameMode));
        gameModeVotes.put(player, gameMode);
      } else if (gameModeVotes.get(player) != gameMode){
        player.sendMessage("[EggSplosion] Vote Update to Game Mode " + ChatColor.GREEN + gameModeToString(gameMode));
        gameModeVotes.put(player, gameMode);
      } else {
        player.sendMessage("[EggSplosion] Vote Already Registered for Game Mode " + ChatColor.GREEN + gameModeToString(gameMode));
      }
    }
  }

  public void registerMapVote(String mapName, Player player) {
    if (getPlayers().contains(player)) {
      if (mapVotes.get(player) == null) {
        player.sendMessage("[EggSplosion] Vote Registered for Map " + ChatColor.AQUA + mapName);
        mapVotes.put(player, mapName);
      } else if (!mapVotes.get(player).equalsIgnoreCase(mapName)) {
        player.sendMessage("[EggSplosion] Vote Update to Map " + ChatColor.AQUA + mapName);
        mapVotes.put(player, mapName);
      } else {
        player.sendMessage("[EggSplosion] Vote Already Registed for Map " + ChatColor.AQUA + mapName);
      }
    }
  }

  private GameMode tallyGameModeVote() {
    Map<GameMode, Integer> gameModeVoteTally = new Hashtable<GameMode, Integer>();
    for(GameMode gameMode: GameMode.values()) {
      if (gameMode != GameMode.LOBBY && gameMode != GameMode.WAITING) {
        gameModeVoteTally.put(gameMode, 0);
      }
    }

    for(Player player : getPlayers()) {
      GameMode gameMode = gameModeVotes.get(player);
      if (gameMode != null && gameModeVoteTally.get(gameMode) != null) {
        gameModeVoteTally.put(gameMode, gameModeVoteTally.get(gameMode) + 1);
      }
    }

    Integer largestInteger = 0;
    for(GameMode gameMode: GameMode.values()) {
      Integer voteTally = gameModeVoteTally.get(gameMode);
      if (voteTally != null && voteTally > largestInteger) {
        largestInteger = voteTally;
      }
    }

    List<GameMode> options = new ArrayList<>();
    for(GameMode gameMode: GameMode.values()) {
      Integer voteTally = gameModeVoteTally.get(gameMode);
      if (voteTally != null && voteTally == largestInteger) {
        options.add(gameMode);
      }
    }

    Random random = new Random();
    return options.get(random.nextInt(options.size()));
  }

  private String tallyGameMapVote() {
    Map<String, Integer> mapVoteTally = new Hashtable<String, Integer>();
    for(String mapName: mapManager.getMaps().keySet()) {
      if (!mapName.equalsIgnoreCase("WAITING_ROOM")) {
        mapVoteTally.put(mapName, 0);
      }
    }

    for(Player player : getPlayers()) {
      String mapName = mapVotes.get(player);
      if (mapName != null && mapVoteTally.get(mapName) != null) {
        mapVoteTally.put(mapName, mapVoteTally.get(mapName) + 1);
      }
    }

    Integer largestInteger = 0;
    for(String mapName: mapManager.getMaps().keySet()) {
      Integer voteTally = mapVoteTally.get(mapName);
      if (voteTally != null && voteTally > largestInteger) {
        largestInteger = voteTally;
      }
    }

    List<String> options = new ArrayList<>();
    for(String mapName: mapManager.getMaps().keySet()) {
      Integer voteTally = mapVoteTally.get(mapName);
      if (voteTally != null && voteTally == largestInteger) {
        options.add(mapName);
      }
    }

    Random random = new Random();
    return options.get(random.nextInt(options.size()));
  }
}
