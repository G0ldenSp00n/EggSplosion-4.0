package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes;

import java.util.Hashtable;
import java.util.Map;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MainLobby extends Lobby {
  Map<Player, ItemStack[]> playerInventoryMemory;
  Map<Player, org.bukkit.GameMode> playerGameModeMemory;
  Map<Player, Location> playerLocationMemory;
  Map<Player, Boolean> playerFlightMemory;

  public MainLobby(Plugin plugin) {
    super(plugin, "MAIN_LOBBY");

    this.playerInventoryMemory = new Hashtable<>();
    this.playerGameModeMemory = new Hashtable<>();
    this.playerLocationMemory = new Hashtable<>();
    this.playerFlightMemory = new Hashtable<>();
  }

  public void equipPlayer(Player player) {
    return;
  }

  public void savePlayerState(Player player) {
    playerLocationMemory.put(player, player.getLocation());
    playerInventoryMemory.put(player, player.getInventory().getContents());
    playerGameModeMemory.put(player, player.getGameMode());
    playerFlightMemory.put(player, player.isFlying());
  }

  public void loadPlayerState(Player player) {
    player.sendMessage("[EggSplosion] Loading Saved Player State");

    Location playerLocation = playerLocationMemory.get(player);
    if (playerLocation != null) {
      player.teleport(playerLocation);
    }

    ItemStack[] playerInventory = playerInventoryMemory.get(player);
    if (playerInventory != null) {
      player.getInventory().setContents(playerInventory);
    }

    org.bukkit.GameMode playerGameMode = playerGameModeMemory.get(player);
    if (playerGameMode != null) {
      player.setGameMode(playerGameMode);
    }

    Boolean playerFlying = playerFlightMemory.get(player);
    if (playerFlying != null) {
      player.setFlying(playerFlying);
    }
  }

  protected void handlePlayerJoin(Player player) {
    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    loadPlayerState(player);
    for (Player lobbyPlayer : getPlayers()) {
      lobbyPlayer.sendMessage("[EggSplosion] " + player.getName() + " has joined the main lobby!");
    }
  }

  protected void handlePlayerLeave(Player player) {
    savePlayerState(player);
    for (Player lobbyPlayer : getPlayers()) {
      lobbyPlayer.sendMessage("[EggSplosion] " + player.getName() + " has left the main lobby!");
    }
  }

  protected void handleScoreManagerChange(Player player, ScoreManager scoreManager) {
    throw new Error("Main Lobby can't have a score manager");
  }

  protected void handleMapChange(GameMap gameMap) {
    throw new Error("Main Lobby can't have a map");
  }

  protected void handleGameModeChange(GameMode gameMode) {
    throw new Error("Main Lobby can't have a gamemode");
  }
}
