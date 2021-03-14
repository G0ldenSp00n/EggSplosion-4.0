package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class Lobby {
  // Managers
  protected Plugin plugin;
  protected MapManager mapManager;
  protected ScoreManager scoreManager;

  // Lobby Data
  private GameMode gameMode;
  private GameMap currentMap;
  private List<Player> playersInLobby;
  private String lobbyName;

  public Lobby(Plugin plugin, String lobbyName) {
    // Initialize Managers
    this.plugin = plugin;

    // Initialize Map Data
    this.lobbyName = lobbyName;
    this.playersInLobby = new ArrayList<>();
  }

  public Lobby(Plugin plugin, MapManager mapManager, String lobbyName, GameMode gameMode, GameMap gameMap) {
    this(plugin, mapManager, lobbyName, gameMode, gameMap, null);
  }

  public Lobby(Plugin plugin, MapManager mapManager, String lobbyName, GameMode gameMode, GameMap gameMap, List<Player> playersInLobby) {
    this(plugin, lobbyName);
    // Initialize Managers
    this.mapManager = mapManager;
    if (playersInLobby != null) {
      this.playersInLobby = playersInLobby;
    }

    setMap(gameMap);
    setGameMode(gameMode);
    applyMapEffects();
  }

  public abstract void equipPlayer(Player player);
  protected abstract void handlePlayerJoin(Player player);
  protected abstract void handlePlayerLeave(Player player);
  protected abstract void handleScoreManagerChange(Player player, ScoreManager scoreManager);
  protected abstract void handleMapChange(GameMap gameMap);

  public void applyMapEffects() {
    new BukkitRunnable(){
        @Override
        public void run() {
          if (getMap() != null && getPlayers() != null) {
            for (Player player: getPlayers()) {
                for(PotionEffect mapEffect: getMap().getMapEffects()) {
                  player.addPotionEffect(mapEffect);
                }
            }
          }
        }
      }.runTaskTimer(this.plugin, 0, (long) 5 * 20);
  }

  public Boolean anyOnlinePlayersExcluding(Player excludedPlayer) {
    Boolean anyOnline = false;
    for (Player player: getPlayers()) {
      if (!player.equals(excludedPlayer)) {
        if (Bukkit.getServer().getOnlinePlayers().contains(player)) {
          anyOnline = true;
        }
      }
    }
    return anyOnline;
  }

  public void addPlayer(Player player) {
    if (player != null && !getPlayers().contains(player)) {
      try {
        handlePlayerJoin(player);
      } catch (Error error) {
        player.sendMessage(error.getMessage());
      }
      playersInLobby.add(player);
    }
  }

  public void addPlayers(List<Player> players) {
    for (Player player : players) {
      addPlayer(player);
    }
  }

  public void addPlayers(Collection<? extends Player> players) {
    for (Player player : players) {
      addPlayer(player);
    }
  }

  public void removePlayer(Player player) {
    handlePlayerLeave(player);
    playersInLobby.remove(player);
  }

  public void removeAllPlayers() {
    for (Player player: playersInLobby) {
      handlePlayerLeave(player);
    }
    playersInLobby = new ArrayList<>();
  }

  public List<Player> getPlayers() {
    return playersInLobby;
  }

  public String getLobbyName() {
    return lobbyName;
  }

  public GameMap getMap() {
    return currentMap;
  }

  public void setMap(GameMap gameMap) {
    try {
      handleMapChange(gameMap);
    } catch (Error error) {
      error.printStackTrace();
      return;
    }

    this.currentMap = gameMap;
  }

  public GameMode getGameMode() {
    return gameMode;
  }

  public void setGameMode(GameMode gameMode) {
    this.gameMode = gameMode;
  }

  public ScoreManager getScoreManager() {
    return scoreManager;
  }

  public void setScoreManager(ScoreManager scoreManager) {
    try {
      for (Player player : getPlayers()) {
        handleScoreManagerChange(player, scoreManager);
      }
    } catch (Error error) {
      error.printStackTrace();
      return;
    }
    this.scoreManager = scoreManager;
  }

  public Boolean playerInLobby(Player player) {
    return getPlayers().contains(player);
  }

  protected ItemStack setUnbreakable(ItemStack itemStack) {
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setUnbreakable(true);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  private void equipDefaultInventory(Player player) {
    ItemStack woodenHoe = new ItemStack(Material.WOODEN_HOE);
    ItemStack stoneHoe = new ItemStack(Material.STONE_HOE);
    ItemStack ironHoe = new ItemStack(Material.IRON_HOE);
    ItemStack goldenHoe = new ItemStack(Material.GOLDEN_HOE);
    ItemStack diamondHoe = new ItemStack(Material.DIAMOND_HOE);

    player.getInventory().setItem(0, setUnbreakable(woodenHoe));
    player.getInventory().setItem(1, setUnbreakable(stoneHoe));
    player.getInventory().setItem(2, setUnbreakable(ironHoe));
    player.getInventory().setItem(3, setUnbreakable(goldenHoe));
    player.getInventory().setItem(4, setUnbreakable(diamondHoe));
  }

  protected void equipInventory(Player player) {
    if (getMap() != null) {
      Boolean equippedInventory = false;
      for (ItemStack itemStack: getMap().getLoadout().getContents()) {
        if (itemStack != null) {
          player.getInventory().setContents(getMap().getLoadout().getContents());
          equippedInventory = true;
          break;
        }
      }

      if (!equippedInventory) {
        equipDefaultInventory(player);
      }
    }
  }

  protected void equipArmor(Player player, Color color) {
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

    player.getInventory().setHelmet(setUnbreakable(helmet));
    player.getInventory().setChestplate(setUnbreakable(chestplate));
    player.getInventory().setLeggings(setUnbreakable(leggings));
    player.getInventory().setBoots(setUnbreakable(boots));
  }

  public void broadcastMessageTypeMessage(ChatMessageType chatMessageType , String message) {
    for(Player player: playersInLobby) {
      player.spigot().sendMessage(chatMessageType, new TextComponent(message));
    }
  }

  public void broadcastActionBar(String message, Boolean includeChatMessage) {
    broadcastMessageTypeMessage(ChatMessageType.ACTION_BAR, message);
    if (includeChatMessage) {
      broadcastMessage(message);
    }
  }

  public void broadcastActionBar(String message) {
    broadcastActionBar(message, false);
  }

  public void broadcastMessage(String message) {
    broadcastMessageTypeMessage(ChatMessageType.CHAT, message);
  }

  public void broadcastTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
    for (Player player: playersInLobby) {
      player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }
  }

  protected void configurePlayer(Player player) {
    player.setGameMode(org.bukkit.GameMode.ADVENTURE);
    player.setFoodLevel(20);
    player.setHealth(20);
  }

  protected String gameModeToString(GameMode gameMode) {
    switch(gameMode) {
      case TEAM_DEATH_MATCH:
        return "Team Death Match";
      case CAPTURE_THE_FLAG:
        return "Capture the Flag";
      case DEATH_MATCH:
        return "Death Match";
      case LOBBY:
        return "Lobby";
      case CAPTURE_POINT:
        return "Capture Point";
      default:
        return "";
    }
  }
}

