package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class MapManager implements Listener, CommandExecutor {
  private ItemStack Map_Tool_boundary;
  private Map<Player, List<Location>> boundaryToolTracker = new Hashtable<>();
  private Map<String, GameMap> gameMaps = new Hashtable<>();
  private String pluginFolder;
  private LobbyManager lobbyManager;

  public MapManager(Plugin plugin, String pluginFolder) {
    this.lobbyManager = LobbyManager.getInstance(plugin, this);
    this.pluginFolder = pluginFolder;
    Bukkit.getPluginManager().registerEvents(this, plugin);

    loadMapsFromFiles();
  }

  public GameMap getMapByName(String mapName) {
    return gameMaps.get(mapName);
  }

  private List<Location> convertToLocationList(List<?> list) {
    Iterator<?> listIterator = list.iterator();
    List<Location> locationList = new ArrayList<>();
    while(listIterator.hasNext()) {
      try {
        Location loc = (Location) listIterator.next();
        locationList.add(loc);
      } catch (ClassFormatError e) {
        // Suppress
      }
    }

    return locationList;
  }

  public Map<String, GameMap> getMaps() {
    return  gameMaps;
  }

  private void loadMapsFromFiles() {
    File mapFolder = new File(pluginFolder, "map");
    if (mapFolder != null && mapFolder.listFiles() != null) {
      for (File file: mapFolder.listFiles()) {
        String fileName = file.getName().substring(0, file.getName().length() - 5);
        FileConfiguration mapConfigFile = YamlConfiguration.loadConfiguration(file);
        GameMap map = new GameMap(mapConfigFile.getLocation("cornerA"), mapConfigFile.getLocation("cornerB"));
        List<?> soloSpawnItems = mapConfigFile.getList("soloSpawnLocations");
        List<?> teamASpawnItems = mapConfigFile.getList("teamASpawnLocations");
        List<?> teamBSpawnItems = mapConfigFile.getList("teamBSpawnLocations");

        List<Location> soloSpawnLocations = convertToLocationList(soloSpawnItems);
        List<Location> teamASpawnLocations = convertToLocationList(teamASpawnItems);
        List<Location> teamBSpawnLocations = convertToLocationList(teamBSpawnItems);
        map.loadMapFromFile(soloSpawnLocations, teamASpawnLocations, teamBSpawnLocations, mapConfigFile.getLocation("teamAFlagLocation"), mapConfigFile.getLocation("teamBFlagLocation"));

        gameMaps.put(fileName, map);
      }
    }
  }

  public void saveMapsToFiles() {
    for (String mapName: gameMaps.keySet()) {
      if (mapName != null) {
        GameMap map = gameMaps.get(mapName);

        File mapFile = new File(pluginFolder, "map/" + mapName + ".yaml");
        FileConfiguration mapConfigFile = YamlConfiguration.loadConfiguration(mapFile);

        mapConfigFile.set("cornerA", map.getCornerA());
        mapConfigFile.set("cornerB", map.getCornerB());
        mapConfigFile.set("teamAFlagLocation", map.getTeamAFlagLocation());
        mapConfigFile.set("teamBFlagLocation", map.getTeamBFlagLocation());
        mapConfigFile.set("soloSpawnLocations", map.getSoloSpawnLocations());
        mapConfigFile.set("teamASpawnLocations", map.getTeamASpawnLocations());
        mapConfigFile.set("teamBSpawnLocations", map.getTeamBSpawnLocations());

        try {
          mapConfigFile.save(mapFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private ItemStack createMapTool(Material material, String displayName, List<String> lore) {
    List<String> formattedLore = new ArrayList<String>();
    for(String loreLine: lore) {
      formattedLore.add(ChatColor.RESET + loreLine);
    }
    ItemStack Map_Tool = new ItemStack(material);
    ItemMeta Map_Tool_Meta = Map_Tool.getItemMeta();
    Map_Tool_Meta.setDisplayName(ChatColor.RESET + displayName);
    Map_Tool_Meta.setLore(formattedLore);
    Map_Tool_Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    Map_Tool.setItemMeta(Map_Tool_Meta);
    return Map_Tool;
  }

  private ItemStack createMapTool(Material material, String displayName) {
    return createMapTool(material, displayName, new ArrayList<String>());
  }

  @EventHandler
  public void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
    Player player = playerMoveEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby != lobbyManager.getMainLobby()) {
      if (playerLobby.getMap() != null) {
        if (! playerLobby.getMap().locationInMap(playerMoveEvent.getTo())) {
          playerMoveEvent.setCancelled(true);
        } 
      }
    }
  }

  @EventHandler
  public void PlayerTeleportEvent(PlayerTeleportEvent playerTeleportEvent) {
    Player player = playerTeleportEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);

    if (playerLobby != lobbyManager.getMainLobby()) {
      if (playerLobby.getMap() != null) {
        if (! playerLobby.getMap().locationInMap(playerTeleportEvent.getTo())) {
          playerTeleportEvent.setCancelled(true);
          player.sendMessage("[EggSplosion] Teleport out of map cancelled");
        } 
      }
    }
  }

  @EventHandler
  public void playerUseMappingTools(PlayerInteractEvent playerInteractEvent) {
    if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_AIR) || playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      ItemStack mappingTool = playerInteractEvent.getItem();
      Player player = playerInteractEvent.getPlayer();
      if (mappingTool != null) {
        if (mappingTool.equals(Map_Tool_boundary)) {
          playerInteractEvent.setCancelled(true);
          if (boundaryToolTracker.get(player) == null || boundaryToolTracker.get(player).size() == 2) {
            List<Location> locationList = new ArrayList<>();
            Location cornerA = playerInteractEvent.getClickedBlock().getLocation();
            locationList.add(cornerA);
            boundaryToolTracker.put(player, locationList);
            player.sendMessage("Added Corner A (" + cornerA.getX() + ", " + cornerA.getZ() + ") to Boundary");
          } else {
            List<Location> locationList = boundaryToolTracker.get(player);
            Location cornerB = playerInteractEvent.getClickedBlock().getLocation();
            locationList.add(cornerB);
            boundaryToolTracker.put(player, locationList);
            player.sendMessage("Added Corner B (" + cornerB.getX() + ", " + cornerB.getZ() + ") to Boundary");
            player.sendMessage("Now run the command /map create <mapName>, to create the map");
          }
        } else if (mappingTool.getType().equals(Material.IRON_AXE)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          String mapName = itemLore.get(0).split(":")[1].trim();
          GameMap map = gameMaps.get(mapName);
          if (map != null) {
            Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation();
            spawnPoint.setYaw(player.getLocation().getYaw());
            if (map.locationInMap(spawnPoint)) {
              map.addSoloSpawnpoint(spawnPoint);
              player.sendMessage("Added Solo Spawn Point (" + spawnPoint.getBlockX() + ", " + spawnPoint.getBlockY() + ", " + spawnPoint.getBlockZ() + ")");
            } else {
              player.sendMessage("Spawn Point must be in Map Boundary");
            }
          }
        } else if (mappingTool.getType().equals(Material.GOLDEN_AXE)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          String mapName = itemLore.get(0).split(":")[1].trim();
          GameMap map = gameMaps.get(mapName);
          if (map != null) {
            Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation();
            spawnPoint.setYaw(player.getLocation().getYaw());
            if (map.locationInMap(spawnPoint)) {
              map.addTeamASpawnPoint(spawnPoint);
              player.sendMessage("Added" + ChatColor.RED + " Team A " + ChatColor.RESET + "Spawn Point (" + spawnPoint.getBlockX() + ", " + spawnPoint.getBlockY() + ", " + spawnPoint.getBlockZ() + ")");
            } else {
              player.sendMessage("Spawn Point must be in Map Boundary");
            }
          }
        } else if (mappingTool.getType().equals(Material.DIAMOND_AXE)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          String mapName = itemLore.get(0).split(":")[1].trim();
          GameMap map = gameMaps.get(mapName);
          if (map != null) {
            Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation();
            spawnPoint.setYaw(player.getLocation().getYaw());
            if (map.locationInMap(spawnPoint)) {
              map.addTeamBSpawnPoint(spawnPoint);
              player.sendMessage("Added" + ChatColor.BLUE + " Team B " + ChatColor.RESET + "Spawn Point (" + spawnPoint.getBlockX() + ", " + spawnPoint.getBlockY() + ", " + spawnPoint.getBlockZ() + ")");
            } else {
              player.sendMessage("Spawn Point must be in Map Boundary");
            }
          }
        } else if (mappingTool.getType().equals(Material.GOLDEN_SHOVEL)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          String mapName = itemLore.get(0).split(":")[1].trim();
          GameMap map = gameMaps.get(mapName);
          if (map != null) {
            Location flagLocation = playerInteractEvent.getClickedBlock().getLocation();
            if (map.locationInMap(flagLocation)) {
              map.setTeamAFlagLocation(flagLocation);
              player.sendMessage("Set" + ChatColor.RED + " Team A " + ChatColor.RESET + "Flag Location (" + flagLocation.getBlockX() + ", " + flagLocation.getBlockY() + ", " + flagLocation.getBlockZ() + ")");
            } else {
              player.sendMessage("Flag Location must be in Map Boundary");
            }
          }
        } else if (mappingTool.getType().equals(Material.DIAMOND_SHOVEL)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          String mapName = itemLore.get(0).split(":")[1].trim();
          GameMap map = gameMaps.get(mapName);
          if (map != null) {
            Location flagLocation = playerInteractEvent.getClickedBlock().getLocation();
            if (map.locationInMap(flagLocation)) {
              map.setTeamBFlagLocation(flagLocation);
              player.sendMessage("Set" + ChatColor.BLUE + " Team B " + ChatColor.RESET + "Flag Location (" + flagLocation.getBlockX() + ", " + flagLocation.getBlockY() + ", " + flagLocation.getBlockZ() + ")");
            } else {
              player.sendMessage("Flag Location must be in Map Boundary");
            }
          }
        }
      }
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    if (commandLabel.equalsIgnoreCase("map")) {
      if (args.length > 0) {
        switch (args[0]) {
          case "tools":
            if (args.length > 1) {
              if (sender instanceof Player && gameMaps.get(args[1]) != null) {
                Player player = (Player) sender;
                player.getInventory().clear();

                List<String> lore = new ArrayList<>();
                lore.add("Map: "+args[1]);

                ItemStack Map_Tool_spawnPointsSolo = createMapTool(Material.IRON_AXE, "Spawn Point Tool - Solo", lore);
                player.getInventory().addItem(Map_Tool_spawnPointsSolo);

                ItemStack Map_Tool_spawnPointsTeamA = createMapTool(Material.GOLDEN_AXE, "Spawn Point Tool - Team A", lore);
                ItemStack Map_Tool_spawnPointsTeamB = createMapTool(Material.DIAMOND_AXE, "Spawn Point Tool - Team B", lore);

                ItemStack Map_Tool_flagSpawnTeamA = createMapTool(Material.GOLDEN_SHOVEL, "Flag Spawn - Team A", lore);
                ItemStack Map_Tool_flagSpawnTeamB = createMapTool(Material.DIAMOND_SHOVEL, "Flag Spawn - Team B", lore);


                player.getInventory().setItem(2, Map_Tool_spawnPointsTeamA);
                player.getInventory().setItem(3, Map_Tool_spawnPointsTeamB);

                player.getInventory().setItem(5, Map_Tool_flagSpawnTeamA);
                player.getInventory().setItem(6, Map_Tool_flagSpawnTeamB);


                player.sendMessage("Select the boudrys of the map by right clicking the shovel, then run the command /map create <mapName>");
                return true;
              }
            } else {
              if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().clear();

                Map_Tool_boundary = createMapTool(Material.WOODEN_SHOVEL, "Boundary Tool");
                player.getInventory().addItem(Map_Tool_boundary);

                player.sendMessage("Select the boudrys of the map by right clicking the shovel, then run the command /map create <mapName>");
                return true;
              }
            }
            break;
          case "create":
            if (args.length > 1) {
              if (sender instanceof Player) {
                Player player = (Player) sender;
                if (boundaryToolTracker.get(player) != null && boundaryToolTracker.get(player).size() == 2) {
                  player.sendMessage("Map " + ChatColor.GRAY + args[1] + ChatColor.RESET + " created");
                  GameMap map = new GameMap(boundaryToolTracker.get(player).get(0), boundaryToolTracker.get(player).get(1));
                  gameMaps.put(args[1], map);
                  boundaryToolTracker.remove(player);

                  player.performCommand("map tools " + args[1]);
                  return true;
                }
              }
            }
            break;
        }
      }
    }
    return false;
  }
}
