package com.g0ldensp00n.eggsplosion.handlers.LobbyManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.MainLobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.WaitingLobby;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.Utils.Utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class LobbyManager implements Listener, CommandExecutor, TabCompleter {
  private Plugin plugin;
  private Hashtable<String, Lobby> lobbies;
  private Lobby mainLobby;
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
      this.lobbies = new Hashtable<String, Lobby>();
      
      Bukkit.getPluginManager().registerEvents(this, plugin);
      mainLobby = (Lobby) new MainLobby(plugin);
      if (Bukkit.getOnlinePlayers().size() > 0) {
        mainLobby.addPlayers(Bukkit.getOnlinePlayers());
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
      return getMainLobby();
    }

    public Lobby getMainLobby() {
      return mainLobby;
    }

    public void replaceLobby(String lobbyName, Lobby lobby) {
      lobbies.put(lobbyName, lobby);
    }

    public Lobby addLobby(Integer maxPlayers, String lobbyName, GameMap map) {
      Lobby addedLobby = new WaitingLobby(this.plugin, maxPlayers, mapManager, lobbyName);
      lobbies.put(lobbyName, addedLobby);
      return addedLobby;
    }

    public void closeLobby(String lobbyName) {
      Lobby lobbyToRemove = lobbies.get(lobbyName);
      if (lobbyToRemove != null) {
        List<Player> playersInLobby = lobbyToRemove.getPlayers();
        this.getMainLobby().addPlayers(playersInLobby);
        lobbies.remove(lobbyName);
      }
    }

    @EventHandler
    public void PlayerLogin(PlayerLoginEvent playerLoginEvent) {
      Player player = playerLoginEvent.getPlayer();
      if (getPlayersLobby(player) == null) {
        getMainLobby().addPlayer(player);
        getMainLobby().broadcastMessage(player.getDisplayName() + " Joined Lobby");
      } 
    }

    public void joinLobby(Lobby lobby, Player player) {
      if (getPlayersLobby(player) instanceof MainLobby) {
        getPlayersLobby(player).removePlayer(player);
      } else {
        List<Lobby> oldLobbies = new ArrayList<Lobby>(lobbies.values());
        oldLobbies.remove(lobby);
        for(Lobby oldLobby: oldLobbies) {
          oldLobby.removePlayer(player);
          if (oldLobby != getMainLobby() && oldLobby.getPlayers().size() == 0) {
            lobbies.remove(oldLobby.getLobbyName());
          }
        }
      }
      lobby.addPlayer(player);
    }

    public void cleanupLobbies() {
      for(Lobby oldLobby: lobbies.values()) {
        if (oldLobby != getMainLobby()) {
          getMainLobby().addPlayers(oldLobby.getPlayers());
          oldLobby.removeAllPlayers();
        }
      }

      lobbies = new Hashtable<>();
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
        if (playerLobby != null && playerLobby != getMainLobby() && !playerLobby.anyOnlinePlayersExcluding(player)) {
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
                Lobby createdLobby = addLobby(10, args[1], mapManager.getMapByName("WAITING_ROOM"));
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
                if (playerLobby == null) {
                  joinLobby(getMainLobby(), playerCmdSender);
                }
                if (playerLobby != null && playerLobby != getMainLobby()) {
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
              while (lobbiesIterator.hasNext()) {
                String nextLobby = lobbiesIterator.next();
                lobbiesList += ChatColor.AQUA + nextLobby + ChatColor.RESET;
                if (lobbiesIterator.hasNext()) {
                  lobbiesList += ", ";
                }
              }

              if (lobbies.size() > 0) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
      if (cmd.getName().equalsIgnoreCase("lobby")) {
        switch (args.length) {
          case 1:
            List<String> commands = new ArrayList<>();
            commands.add("create");
            commands.add("join");
            commands.add("leave");
            commands.add("list");
            return Utils.FilterTabComplete(args[0], commands);         
          case 2:
            switch(args[0]) {
              case "join":
                List<String> lobbyNames = new ArrayList<>();
                Iterator<String> lobbyNamesIterator = lobbies.keys().asIterator();
                while (lobbyNamesIterator.hasNext()) {
                  lobbyNames.add(lobbyNamesIterator.next());
                }
                return Utils.FilterTabComplete(args[1], lobbyNames);
              case "create":
              case "list":
              case "leave":
              default:
                return new ArrayList<>();
            }
        }
      }
      return null;
    }
}
