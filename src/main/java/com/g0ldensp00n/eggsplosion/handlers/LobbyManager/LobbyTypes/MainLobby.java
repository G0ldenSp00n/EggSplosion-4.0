package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MainLobby extends Lobby {
  public MainLobby(Plugin plugin) {
    super(plugin, "MAIN_LOBBY");
  }

  public void equipPlayer(Player player) {
    return;
  }

  protected void handlePlayerJoin(Player player) {
    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    for (Player lobbyPlayer : getPlayers()) {
      lobbyPlayer.sendMessage("[EggSplosion] " + player.getName() + " has joined the main lobby!");
    }
  }

  protected void handlePlayerLeave(Player player) {
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
