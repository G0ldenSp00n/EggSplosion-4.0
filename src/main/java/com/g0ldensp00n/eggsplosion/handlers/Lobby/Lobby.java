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
  private List<Player> playersInLobby;
  private Map<Player, GameMode> gameModeVotes;
  private Map<Player, String> mapVotes;
  private Map<Player, Boolean> readyPlayers;
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

    playersInLobby = new ArrayList<>();
    gameModeVotes = new Hashtable<Player, GameMode>();
    mapVotes = new Hashtable<Player, String>();
    readyPlayers = new Hashtable<Player, Boolean>();

    if (gameMode == GameMode.WAITING) {
      setScoreManager(new ScoreManager(ScoreType.TRACKING, this, ChatColor.WHITE, ChatColor.GREEN, false));
    }
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
          scoreManager.getTeamA().addEntry(player.getName());;
        } else {
          scoreManager.getTeamB().addEntry(player.getName());;
        }
        teamA = !teamA;
        playersToAdd.remove(player);
      }
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
          if (countDown == 0) {
            cancel();
            loadWaiting();
            return;
          } else if (countDown <= 5) {
            broadcastTitle("Return to waiting room", "" + countDown, 0, 21, 0);
          }
          countDown--;
        }
      }.runTaskTimer(this.plugin, 0, (long) 20);

  }

  public String addPlayer(Player player) {
    if (maxPlayers == -1 || playersInLobby.size() <= maxPlayers) {
      if (getGameMode() == GameMode.WAITING || getGameMode() == GameMode.LOBBY) {
        if (scoreManager != null) {
          scoreManager.setPlayerScoreboard(player);
        }
        playersInLobby.add(player);
        unreadyPlayer(player);
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
      if (scoreManager.getTeamA() != null) {
        for (Player player: getPlayers()) {
          player.setDisplayName(player.getName());
        }
      }
    }
    this.scoreManager = scoreManager;
    for(Player player : getPlayers()) {
      scoreManager.setPlayerScoreboard(player);
      scoreManager.initializeScorePlayer(player);
    }
  } 

  public void loadWaiting() {
    setGameMode(GameMode.WAITING);
    setMap(mapManager.getMapByName("WAITING_ROOM"), ScoreType.SOLO);
    for (Player player: getPlayers()) {
      unreadyPlayer(player);
    }
    setScoreManager(new ScoreManager(ScoreType.TRACKING, this, ChatColor.WHITE, ChatColor.GREEN, false));
  }

  public void startGame() {
    GameMode gameMode = tallyGameModeVote();
    String mapName = tallyGameMapVote();
    broadcastTitle(gameModeToString(gameMode), "Map - " + mapName, 10, 60, 10);

    switch(gameMode) {
      case DEATH_MATCH:
        setScoreManager(new ScoreManager(2, ScoreType.SOLO, this));
        setMap(mapManager.getMapByName(mapName), ScoreType.SOLO);
        break;
      case TEAM_DEATH_MATCH:
        setScoreManager(new ScoreManager(15, ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
        randomizeTeams();
        setMap(mapManager.getMapByName(mapName), ScoreType.TEAM);
        break;
      case CAPTURE_THE_FLAG:
        setScoreManager(new ScoreManager(3, ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
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
        new BukkitRunnable(){
          Integer countDown = 5;

          @Override
          public void run() {
            if (countDown == 0) {
              cancel();
              startGame();
              return;
            } if (allPlayersReady()) {
              broadcastTitle("Game Starting", ""+countDown--, 0, 21, 0);
            } else {
              broadcastTitle("Game Starting", "Cancelling Player Unreadied", 0, 21, 0);
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
    if (scoreManager != null) {
      getScoreboardManager().setPlayerTeam(player, getScoreboardManager().getTeamB());
    }
    ifAllPlayersReadyStartGame();
  }

  public void unreadyPlayer(Player player) {
    readyPlayers.put(player, false);
    for(Player playerInLobby : playersInLobby) {
      playerInLobby.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1, 1);
    }
    if (scoreManager != null) {
      getScoreboardManager().setPlayerTeam(player, getScoreboardManager().getTeamA());
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
    ItemStack helmet = null;
    ItemStack chestplate = null;
    ItemStack leggings = null;
    ItemStack boots = null;

    GameMap gameMap = getMap();
    if (gameMap != null) {
      if (gameMap.getHelmet() != null) {
        helmet = gameMap.getHelmet();
      }

      if (gameMap.getChestplate() != null) {
        chestplate = gameMap.getChestplate();
      }

      if (gameMap.getLeggings() != null) {
        leggings = gameMap.getLeggings();
      }

      if (gameMap.getBoots() != null) {
        boots = gameMap.getBoots();
      }
    }

    if (helmet == null) {
      helmet = new ItemStack(Material.LEATHER_HELMET);
    }

    if (chestplate == null) {
      chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
    }

    if (leggings == null) {
      leggings = new ItemStack(Material.LEATHER_LEGGINGS);
    }

    if (boots == null) {
      boots = new ItemStack(Material.LEATHER_BOOTS);
    }

    if (helmet.getItemMeta() instanceof LeatherArmorMeta) {
      LeatherArmorMeta leatherHelmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
      leatherHelmetMeta.setColor(color);
      helmet.setItemMeta(leatherHelmetMeta);
    }

    if (chestplate.getItemMeta() instanceof LeatherArmorMeta) {
      LeatherArmorMeta leatherChestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
      leatherChestplateMeta.setColor(color);
      chestplate.setItemMeta(leatherChestplateMeta);
    }

    if (leggings.getItemMeta() instanceof LeatherArmorMeta) {
      LeatherArmorMeta leatherLeggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
      leatherLeggingsMeta.setColor(color);
      leggings.setItemMeta(leatherLeggingsMeta);
    }

    if (boots.getItemMeta() instanceof LeatherArmorMeta) {
      LeatherArmorMeta leatherBootsMeta = (LeatherArmorMeta) boots.getItemMeta(); 
      leatherBootsMeta.setColor(color);
      boots.setItemMeta(leatherBootsMeta);
    }

    player.getInventory().setHelmet(helmet);
    player.getInventory().setChestplate(chestplate);
    player.getInventory().setLeggings(leggings);
    player.getInventory().setBoots(boots);
  }

  public void equipPlayer(Player player) {
    if (playersInLobby.contains(player)) {
      if (getGameMode() != GameMode.LOBBY) {
        player.getInventory().clear();
      }
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
            if (scoreManager.getPlayerTeam(player).equals(scoreManager.getTeamA())) {
              equipArmor(player, Color.fromRGB(11546150));
            } else if (scoreManager.getPlayerTeam(player).equals(scoreManager.getTeamB())) {
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

  public void broadcastTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
    for (Player player: playersInLobby) {
      player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
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
        if (scoreManager.getTeamA().hasEntry(player.getName())) {
          spawnPoint = this.getMap().getSpawnPoint(com.g0ldensp00n.eggsplosion.handlers.Lobby.Team.TEAM_A);
        } else if (scoreManager.getTeamB().hasEntry(player.getName())) {
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
