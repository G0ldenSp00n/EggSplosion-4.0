package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Lobby {
  private Plugin plugin;
  private List<Player> playersInLobby = new ArrayList<>();
  private Map<Player, GameMode> gameModeVotes = new Hashtable<Player, GameMode>();
  private Map<Player, String> mapVotes = new Hashtable<Player, String>();
  private Map<Player, Boolean> readyPlayers = new Hashtable<Player, Boolean>();
  private GameMode currentGamemode;
  private GameMap currentMap;
  private Integer maxPlayers;
  private String lobbyName;
  private MapManager mapManager;
  private ScoreManager scoreManager;

  public Lobby(Plugin plugin, MapManager mapManager, String lobbyName, GameMode gameMode, Integer maxPlayers, GameMap gameMap) {
    this.mapManager = mapManager;
    this.plugin = plugin;
    this.lobbyName = lobbyName;
    this.currentGamemode = gameMode;
    this.currentMap = gameMap;
    this.maxPlayers = maxPlayers;
  }

  public String getLobbyName() {
    return lobbyName;
  }

  public List<Player> getPlayers() {
    return playersInLobby;
  }

  public GameMode getGameMode() {
    return currentGamemode;
  }

  public ScoreManager getScoreboardManager() {
    return scoreManager;
  }

  public Boolean playerInLobby(Player player) {
    return playersInLobby.contains(player);
  }

  public void randomizeTeams() {
    Random random = new Random();
    if (scoreManager != null) {
      List<Player> playersToAdd = new ArrayList<>(getPlayers());
      Boolean teamA = true;
      while (playersToAdd.size() > 0) {
        Integer nextPlayer = random.nextInt(playersToAdd.size());
        Player player = playersToAdd.get(nextPlayer);
        if (teamA) {
          player.setDisplayName(ChatColor.RED + player.getDisplayName() + ChatColor.RESET);
          scoreManager.getTeamA().addEntry(player.getDisplayName());
          scoreManager.getTeamA().addPlayer((OfflinePlayer) player);
        } else {
          player.setDisplayName(ChatColor.BLUE + player.getDisplayName() + ChatColor.RESET);
          scoreManager.getTeamB().addEntry(player.getDisplayName());
          scoreManager.getTeamB().addPlayer((OfflinePlayer) player);
        }
        teamA = !teamA;
        playersToAdd.remove(player);
      }
    }
  }

  public void playerWon(Player player) {
    scoreManager.scoreFreeze();
    if (this.scoreManager.getScoreType() == ScoreType.SOLO) {
      broadcastMessage(ChatColor.GOLD + player.getDisplayName() + " has won the game!");
      player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
    } else if (this.scoreManager.getScoreType() == ScoreType.TEAM) {
      Team playerTeam = this.scoreManager.getPlayerTeam(player);
      broadcastMessage(ChatColor.GOLD + playerTeam.getDisplayName() + ChatColor.GOLD + " has won the game!");
      for(Player playerOnTeam : getPlayers()) {
        if (playerTeam.hasEntry(playerOnTeam.getDisplayName())) {
          playerOnTeam.playSound(playerOnTeam.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        }
      }
    }
    new BukkitRunnable(){
        Integer countDown = 5;

        @Override
        public void run() {
          if (countDown == 0) {
            cancel();
            loadWaiting();
            return;
          }

          broadcastMessage("Returning to waiting room in " + countDown-- + "...");
        }
      }.runTaskTimer(this.plugin, 0, (long) 20);

  }

  public String addPlayer(Player player) {
    if (maxPlayers == -1 || playersInLobby.size() <= maxPlayers) {
      if (getGameMode() == GameMode.WAITING) {
        playersInLobby.add(player);
        if (!readyPlayers.containsKey(player)) {
          readyPlayers.put(player, false);
        }
        equipPlayer(player);
        if (lobbyName != "MAIN_LOBBY") {
          Location spawnPoint = this.currentMap.getSpawnPoint(com.g0ldensp00n.eggsplosion.handlers.Lobby.Team.SOLO);
          player.teleport(spawnPoint);
          player.setGameMode(org.bukkit.GameMode.ADVENTURE);
          player.setFoodLevel(20);
          player.setHealth(20);
          return "[EggSplosion] You have joined lobby " + ChatColor.AQUA + lobbyName;
        }
      } else {
        return "[EggSplosion] Can't join a in progress lobby";
      }
    }

    return "[EggSplosion] Can't join a full lobby";
  }

  public Boolean hasPlayer(Player player) {
    return getPlayers().contains(player);
  }

  public boolean anyOnlinePlayersExcluding(Player excludedPlayer) {
    Boolean anyOnline = false;
    for (Player player: getPlayers()) {
      if (player != excludedPlayer) {
        if (Bukkit.getServer().getOnlinePlayers().contains(player)) {
          anyOnline = true;
        }
      }
    }

    return anyOnline;
  }

  public void removeAllPlayers() {
    for (Player player: playersInLobby) {
      removePlayerCleanup(player);
    }

    playersInLobby = new ArrayList<>();
  }

  public Boolean allPlayersReady() {
    Boolean allPlayersReady = true;
    for(Player player: playersInLobby) {
      if (!isPlayerReady(player)) {
        allPlayersReady = false;
      }
    }
    return allPlayersReady;
  }

  private GameMode tallyGameModeVote() {
    Map<GameMode, Integer> gameModeVoteTally = new Hashtable<GameMode, Integer>();
    for(GameMode gameMode: GameMode.values()) {
      if (gameMode != GameMode.LOBBY && gameMode != GameMode.WAITING) {
        gameModeVoteTally.put(gameMode, 0);
      }
    }

    for(Player player : playersInLobby) {
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

    for(Player player : playersInLobby) {
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

  public void setScoreManager(ScoreManager scoreManager) {
    if (scoreManager != null) {
      if (scoreManager.getTeamA().getEntries().size() > 0) {
        for (Player player: getPlayers()) {
          player.setDisplayName(player.getDisplayName().substring(2, player.getDisplayName().length() - 2));
        }
      }
    }
    this.scoreManager = scoreManager;
    for(Player player : getPlayers()) {
      Scoreboard gameScoreboard = scoreManager.getScoreboard();
      player.setScoreboard(gameScoreboard);
      if (scoreManager.getScoreType() == ScoreType.SOLO) {
        gameScoreboard.getObjective("score").getScore(player.getDisplayName()).setScore(0);
      } else if (scoreManager.getScoreType() == ScoreType.TEAM) {
        gameScoreboard.getObjective("score").getScore(scoreManager.getTeamA().getDisplayName()).setScore(0);
        gameScoreboard.getObjective("score").getScore(scoreManager.getTeamB().getDisplayName()).setScore(0);
      }
    }
  } 

  public void loadWaiting() {
    setGameMode(GameMode.WAITING);
    setMap(mapManager.getMapByName("WAITING_ROOM"), ScoreType.SOLO);
    if (scoreManager != null) {
      if (scoreManager.getTeamA().getEntries().size() > 0) {
        for (Player player: getPlayers()) {
          player.setDisplayName(player.getDisplayName().substring(2, player.getDisplayName().length() - 2));
          player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
      }
    }
  }

  public void startGame() {
    GameMode gameMode = tallyGameModeVote();
    String mapName = tallyGameMapVote();
    broadcastMessage("----------------------------");
    broadcastMessage("GameMode - " + gameModeToString(gameMode));
    broadcastMessage("Map - " + mapName);
    broadcastMessage("----------------------------");

    switch(gameMode) {
      case DEATH_MATCH:
        setScoreManager(new ScoreManager(15, ScoreType.SOLO, this));
        setMap(mapManager.getMapByName(mapName), ScoreType.SOLO);
        break;
      case TEAM_DEATH_MATCH:
        setScoreManager(new ScoreManager(15, ScoreType.TEAM, this));
        randomizeTeams();
        setMap(mapManager.getMapByName(mapName), ScoreType.TEAM);
        break;
      case CAPTURE_THE_FLAG:
        setScoreManager(new ScoreManager(3, ScoreType.TEAM, this));
        randomizeTeams();
        setMap(mapManager.getMapByName(mapName), ScoreType.TEAM);
        this.currentMap.spawnFlags();
        break;
      default:
        break;
    }
    setGameMode(gameMode);
  }

  public void ifAllPlayersReadyStartGame() {
    Integer minimumPlayers = 2;
    if (playersInLobby.size() >= minimumPlayers) {
      Boolean allPlayersReady = allPlayersReady();

      if (allPlayersReady) {
        broadcastMessage("All Players Ready!");
        new BukkitRunnable(){
          Integer countDown = 5;

          @Override
          public void run() {
            if (countDown == 0) {
              cancel();
              startGame();
              return;
            } if (allPlayersReady()) {
              broadcastMessage("Game Starting in " + countDown-- + "...");
            } else {
              broadcastMessage("Player Unreadied! Cancelling Countdown");
              cancel();
            }
          }
        }.runTaskTimer(this.plugin, 0, (long) 20);
      }
    }

  }

  public void readyPlayer(Player player) {
    readyPlayers.put(player, true);
    for(Player playerInLobby : playersInLobby) {
      playerInLobby.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1, 1);
    }
    ifAllPlayersReadyStartGame();
  }

  public void unreadyPlayer(Player player) {
    readyPlayers.put(player, false);
    for(Player playerInLobby : playersInLobby) {
      playerInLobby.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1, 1);
    }
  }

  public boolean isPlayerReady(Player player) {
    return readyPlayers.get(player);
  }

  public void equipWeapons(Player player) {
    ItemStack woodenHoe = new ItemStack(Material.WOODEN_HOE);
    ItemStack stoneHoe = new ItemStack(Material.STONE_HOE);
    ItemStack ironHoe = new ItemStack(Material.IRON_HOE);
    ItemStack goldenHoe = new ItemStack(Material.GOLDEN_HOE);
    ItemStack diamondHoe = new ItemStack(Material.DIAMOND_HOE);

    player.getInventory().setItem(0, woodenHoe);
    player.getInventory().setItem(1, stoneHoe);
    player.getInventory().setItem(2, ironHoe);
    player.getInventory().setItem(3, goldenHoe);
    player.getInventory().setItem(4, diamondHoe);
  }

  public void equipArmor(Player player, Color color) {
    ItemStack leatherHelmet = new ItemStack(Material.LEATHER_HELMET);
    ItemStack leatherChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
    ItemStack leatherLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
    ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);

    LeatherArmorMeta leatherHelmetMeta = (LeatherArmorMeta) leatherHelmet.getItemMeta();
    LeatherArmorMeta leatherChestplateMeta = (LeatherArmorMeta) leatherChestplate.getItemMeta();
    LeatherArmorMeta leatherLeggingsMeta = (LeatherArmorMeta) leatherLeggings.getItemMeta();
    LeatherArmorMeta leatherBootsMeta = (LeatherArmorMeta) leatherBoots.getItemMeta(); 

    leatherHelmetMeta.setColor(color);
    leatherChestplateMeta.setColor(color);
    leatherLeggingsMeta.setColor(color);
    leatherBootsMeta.setColor(color);
    
    leatherHelmet.setItemMeta(leatherHelmetMeta);
    leatherChestplate.setItemMeta(leatherChestplateMeta);
    leatherLeggings.setItemMeta(leatherLeggingsMeta);
    leatherBoots.setItemMeta(leatherLeggingsMeta);

    player.getInventory().setHelmet(leatherHelmet);
    player.getInventory().setChestplate(leatherChestplate);
    player.getInventory().setLeggings(leatherLeggings);
    player.getInventory().setBoots(leatherBoots);
  }

  public void equipPlayer(Player player) {
    if (playersInLobby.contains(player)) {
      player.getInventory().clear();
      switch (currentGamemode) {
        case WAITING: {
          ItemStack item = new ItemStack(Material.EMERALD);
          ItemMeta itemMeta = item.getItemMeta();
          itemMeta.setDisplayName("Lobby Menu");
          item.setItemMeta(itemMeta);
          player.getInventory().addItem(item);

          ItemStack woodenHoe = new ItemStack(Material.WOODEN_HOE);
          player.getInventory().addItem(woodenHoe);
          break;
        }
        case DEATH_MATCH:
          equipWeapons(player);
          break;
        case TEAM_DEATH_MATCH:
        case CAPTURE_THE_FLAG:
          equipWeapons(player);

          if (scoreManager != null) {
            if (scoreManager.getTeamA().hasEntry(player.getDisplayName())) {
              equipArmor(player, Color.fromRGB(11546150));
            } else if (scoreManager.getTeamB().hasEntry(player.getDisplayName())) {
              equipArmor(player, Color.fromRGB(3949738));
            }
          }
          break;
        case LOBBY:
          break;
        default:
          break;
      }
    }
  }

  public void broadcastMessage(String message) {
    for(Player player: playersInLobby) {
      player.sendMessage(message);
    }
  }

  private String gameModeToString(GameMode gameMode) {
    switch(gameMode) {
      case TEAM_DEATH_MATCH:
        return "Team Death Match";
      case CAPTURE_THE_FLAG:
        return "Capture the Flag";
      case DEATH_MATCH:
        return "Death Match";
      case LOBBY:
        return "Lobby";
      default:
        return "";
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
      }
    }
  }

  public void setGameMode(GameMode gameMode) {
    this.currentGamemode = gameMode;
    for(Player player: playersInLobby) {
      equipPlayer(player);
    }
  }

  public void setMap(GameMap map, ScoreType scoreType) {
    this.currentMap = map;
    for (Player player: getPlayers()) {
      if (scoreType == ScoreType.SOLO || scoreType == ScoreType.INFO) {
        Location spawnPoint = this.currentMap.getSpawnPoint(com.g0ldensp00n.eggsplosion.handlers.Lobby.Team.SOLO);
        player.teleport(spawnPoint);
      } else if (scoreType == ScoreType.TEAM) {
        Location spawnPoint = null;
        if (scoreManager.getTeamA().hasEntry(player.getDisplayName())) {
          spawnPoint = this.getMap().getSpawnPoint(com.g0ldensp00n.eggsplosion.handlers.Lobby.Team.TEAM_A);
        } else if (scoreManager.getTeamB().hasEntry(player.getDisplayName())) {
          spawnPoint = this.getMap().getSpawnPoint(com.g0ldensp00n.eggsplosion.handlers.Lobby.Team.TEAM_B);
        }

        if (spawnPoint != null) {
          player.teleport(spawnPoint);
        }
      }
    }
  }

  public boolean addPlayers(Collection<? extends Player> players) {
    if (maxPlayers == -1 || maxPlayers <= (playersInLobby.size() + players.size())) {
      for(Player player: players) {
        if (player != null) {
          addPlayer(player);
        }
      }
      return true;
    }
    return false;
  }

  private void removePlayerCleanup(Player player) {
    if (scoreManager != null) {
      player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
      if (scoreManager.getTeamA().hasEntry(player.getDisplayName()) || scoreManager.getTeamB().hasEntry(player.getDisplayName())) {
        player.setDisplayName(player.getDisplayName().substring(2, player.getDisplayName().length() - 2));
      }
    }
  }

  public void removePlayer(Player player) {
    removePlayerCleanup(player);
    playersInLobby.remove(player);
  }

  public void registerMapVote(String mapName, Player player) {
    if (getPlayers().contains(player)) {
      if (mapVotes.get(player) == null) {
        player.sendMessage("[EggSplosion] Vote Registered for Map " + ChatColor.AQUA + mapName);
        mapVotes.put(player, mapName);
      } else if (!mapVotes.get(player).equalsIgnoreCase(mapName)) {
        player.sendMessage("[EggSplosion] Vote Update to Map " + ChatColor.AQUA + mapName);
        mapVotes.put(player, mapName);
      }
    }
  }

  public GameMap getMap() {
    return currentMap;
  }
}
