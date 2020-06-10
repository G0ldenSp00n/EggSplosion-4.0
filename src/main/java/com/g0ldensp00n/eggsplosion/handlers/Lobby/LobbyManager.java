package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class LobbyManager implements Listener, CommandExecutor {
  private Plugin plugin;
  private Hashtable<String, Lobby> lobbies = new Hashtable<String, Lobby>();
  private MapManager mapManager;
  private static LobbyManager lobbyManager;

    public static LobbyManager getInstance(Plugin plugin, MapManager mapManager) {
      if (lobbyManager == null) {
        lobbyManager = new LobbyManager(plugin, mapManager);
      }
      return lobbyManager;
    }

    public LobbyManager(Plugin plugin, MapManager mapManager) {
      this.plugin = plugin;
      this.mapManager = mapManager;
      
      Bukkit.getPluginManager().registerEvents(this, plugin);
      if (Bukkit.getOnlinePlayers().size() > 0) {
        this.addLobby(GameMode.LOBBY, -1, "MAIN_LOBBY", null).addPlayers(Bukkit.getOnlinePlayers());
      } else {
        this.addLobby(GameMode.LOBBY, -1, "MAIN_LOBBY", null);
      }
    }

    public Lobby getPlayersLobby(Player player) {
      Collection<Lobby> lobbiesCollection = lobbies.values();
      for(Lobby lobby: lobbiesCollection) {
        for(Player playerToCheck: lobby.getPlayers()) {
          if (playerToCheck.getUniqueId() == player.getUniqueId()) {
            return lobby;
          }
        }
      }
      return null;
    }

    public Lobby getMainLobby() {
      return lobbies.get("MAIN_LOBBY");
    }

    public Lobby addLobby(GameMode gameMode, Integer maxPlayers, String lobbyName, GameMap map) {
      Lobby addedLobby = new Lobby(this.plugin, mapManager, lobbyName, gameMode, maxPlayers, map);
      lobbies.put(lobbyName, addedLobby);
      return addedLobby;
    }

    public void closeLobby(String lobbyName) {
      if (lobbyName != "MAIN_LOBBY") {
        Lobby lobbyToRemove = lobbies.get(lobbyName);
        List<Player> playersInLobby = lobbyToRemove.getPlayers();
        this.getMainLobby().addPlayers(playersInLobby);
        lobbies.remove(lobbyName);
      }
    }

    @EventHandler
    public void PlayerLogin(PlayerLoginEvent playerLoginEvent) {
      Player player = playerLoginEvent.getPlayer();
      if (getPlayersLobby(player) == null) {
        Bukkit.getLogger().info("" + getMainLobby());
        getMainLobby().addPlayer(player);
        getMainLobby().broadcastMessage(player.getDisplayName() + " Joined Lobby");
      }
    }

    public void joinLobby(Lobby lobby, Player player) {
      lobby.addPlayer(player);
      for(Lobby oldLobby: lobbies.values()) {
        if (oldLobby != lobby) {
          oldLobby.removePlayer(player);
          if (oldLobby != getMainLobby() && oldLobby.getPlayers().size() == 0) {
            lobbies.remove(oldLobby.getLobbyName());
          }
        }
      }
    }

    public Boolean canPlayerAttackPlayer(Player playerA, Player playerB) {
      Lobby playerLobby = lobbyManager.getPlayersLobby(playerA);
      Lobby damagerLobby = lobbyManager.getPlayersLobby(playerB);
      return playerLobby == damagerLobby;
    }

    @EventHandler
    public void PlayerLogout(PlayerQuitEvent playerQuitEvent) {
      Player player = playerQuitEvent.getPlayer();
      if (getPlayersLobby(player) == getMainLobby()) {
        getMainLobby().removePlayer(player);
      } else {
        Lobby playerLobby = getPlayersLobby(player);
        if (playerLobby.getLobbyName() != "MAIN_LOBBY" && !playerLobby.anyOnlinePlayersExcluding(player)) {
          playerLobby.removeAllPlayers();
          lobbies.remove(playerLobby.getLobbyName());
        }
      }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      if (commandLabel.equalsIgnoreCase("lobby")) {
        if (args.length >= 1)  {
          switch (args[0]) {
            case "create":
              if (lobbies.get(args[1]) == null) {
                Lobby createdLobby = addLobby(GameMode.WAITING, 10, args[1], mapManager.getMapByName("WAITING_ROOM"));
                sender.sendMessage("[EggSplosion] Lobby " + args[1] + " created!");
                if (sender instanceof Player) {
                  Player playerCmdSender = (Player) sender;
                  joinLobby(createdLobby, playerCmdSender);
                }
                return true;
              } else {
                sender.sendMessage("[EggSplosion] Lobby " + args[1] + " already exists, use /join lobby " + args[1]);
              }
              break;
            case "join":
              if (sender instanceof Player) {
                Player playerCmdSender = (Player) sender;
                Lobby lobby = lobbies.get(args[1]);
                if (lobby != null) {
                  joinLobby(lobby, playerCmdSender);
                  return true;
                }  else {
                  playerCmdSender.sendMessage("[EggSplosion] Lobby " + args[0] + " does not exist");
                }
              }
              break;
            case "leave":
              if (sender instanceof Player) {
                Player playerCmdSender = (Player) sender;
                Lobby playerLobby = getPlayersLobby(playerCmdSender);
                if (playerLobby != getMainLobby()) {
                  joinLobby(getMainLobby(), playerCmdSender);

                  playerCmdSender.sendMessage("[EggSplosion] Left Lobby " + ChatColor.AQUA + playerLobby.getLobbyName());
                  if (playerLobby.getPlayers().size() == 0) {
                    lobbies.remove(playerLobby.getLobbyName());
                  }
                } else {
                  playerCmdSender.sendMessage("[EggSplosion] You can't leave the main lobby");
                }
                return true;
              }
              break;
            case "list":
              String lobbiesList = "[EggSplosion] Current Lobbies - ";
              Iterator<String> lobbiesIterator = lobbies.keys().asIterator();
              Boolean foundLobby = false;
              while (lobbiesIterator.hasNext()) {
                String nextLobby = lobbiesIterator.next();
                if (nextLobby != "MAIN_LOBBY") {
                  lobbiesList += ChatColor.AQUA + nextLobby + ChatColor.RESET;
                  if (lobbiesIterator.hasNext()) {
                    lobbiesList += ", ";
                  }
                  foundLobby = true;
                }
              }

              if (foundLobby) {
                sender.sendMessage(lobbiesList);
              } else {
                sender.sendMessage("[EggSplosion] No Game Lobbies Currently Exist, create one with /lobby create <name>");
              }
              return true;
           }
        }
      }
      return false;
    }
}
