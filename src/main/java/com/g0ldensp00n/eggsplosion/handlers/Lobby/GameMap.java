package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

public class GameMap {
  private Location cornerA;
  private Location cornerB;
  private List<Location> soloSpawnLocations;
  private Map<Integer, List<Location>> sideSpawnLocations;
  private List<GameMode> supportedGameModes;
  private Map<Integer, Location> sideFlagLocation;
  private Map<Team, Integer> teamSide;
  private List<PotionEffect> mapEffects;
  private Inventory playerLoadout;
  private ItemStack helmet;
  private ItemStack chestplate;
  private ItemStack leggings;
  private ItemStack boots;
  private Boolean doSideSwitch = false;

  public GameMap(Location cornerA, Location cornerB) {
    this.cornerA = cornerA;
    this.cornerB = cornerB;

    teamSide = new Hashtable<Team, Integer>();
    playerLoadout = Bukkit.createInventory(null, InventoryType.PLAYER, "Map Loadout");

    soloSpawnLocations = new ArrayList<Location>();
    sideSpawnLocations = new Hashtable<Integer, List<Location>>();
    sideFlagLocation = new Hashtable<Integer, Location>();

    supportedGameModes = new ArrayList<GameMode>();
    mapEffects = new ArrayList<PotionEffect>();
  }

  public Location getCornerA() {
    return cornerA;
  }

  public Location getCornerB() {
    return cornerB;
  }

  public List<PotionEffect> getMapEffects() {
    return mapEffects;
  }

  public void addMapEffect(PotionEffect effect) {
    mapEffects.add(effect);
  }

  public Inventory getLoadout() {
    return playerLoadout;
  }

  public void setLoadoutContents (ItemStack[] playerLoadoutContents) {
    this.playerLoadout.setContents(playerLoadoutContents);
  }

  public void addMapEffect(PotionEffectType potionType, int amplifier) {
    if (potionType != null) {
      PotionEffect effect = new PotionEffect(potionType, 20 * 9, amplifier - 1, true);
      mapEffects.add(effect);
    }
  }

  public void removeMapEffect(PotionEffect effect) {
    mapEffects.remove(effect);
  }

  public Boolean getDoSideSwitch() {
    return doSideSwitch;
  }

  public void setDoSideSwitch(Boolean doSideSwitch) {
    this.doSideSwitch = doSideSwitch;
  }

  public ItemStack getHelmet() {
    if (helmet != null) {
      return helmet.clone();
    }
    return null;
  }

  public ItemStack getChestplate() {
    if (chestplate != null) {
      return chestplate.clone();
    }
    return null;
  }

  public ItemStack getLeggings() {
    if (leggings != null) {
      return leggings.clone();
    }
    return null;
  }

  public ItemStack getBoots() {
    if (boots != null) {
      return boots.clone();
    }
    return null;
  }

  public void setArmor(Integer slot, ItemStack item) {
    if (slot == 0) {
      setHelmet(item);
    } else if (slot == 1) {
      setChestplate(item);
    } else if (slot == 2) {
      setLeggings(item);
    } else if (slot == 3){
      setBoots(item);
    }
  }

  public void setHelmet(ItemStack helmet) {
    this.helmet = helmet;
  }

  public void setChestplate(ItemStack chestplate) {
    this.chestplate = chestplate;
  }

  public void setLeggings(ItemStack leggings) {
    this.leggings = leggings;
  }

  public void setBoots(ItemStack boots) {
    this.boots = boots;
  }

  public List<Location> getSoloSpawnLocations() {
    return soloSpawnLocations;
  }

  public List<Location> getSideSpawnLocations(Integer side) {
    return sideSpawnLocations.get(side);
  }

  public List<Location> getSideASpawnLocations() {
    return getSideSpawnLocations(0);
  }

  public List<Location> getSideBSpawnLocations() {
    return getSideSpawnLocations(1);
  }

  public Team getSideTeam(Integer side) {
    for(Entry<Team, Integer> entry : teamSide.entrySet()) {
      if (entry.getValue() == side) {
        return entry.getKey();
      }
    }
    return null;
  }

  public void randomizeTeamSides(List<Team> teams) {
    if (doSideSwitch) {
      Random random = new Random();
      List<Team> teamsToAdd = new ArrayList<>(teams);
      Integer side = 0;
      while(teamsToAdd.size() > 0) {
        Integer nextTeam = random.nextInt(teamsToAdd.size());
        Team team = teamsToAdd.get(nextTeam);
        teamSide.put(team, side);

        side++;
        teamsToAdd.remove(team);
      }
    } else {
      Integer side = 0;
      for (Team team: teams) {
        teamSide.put(team, side++);
      }
    }
  }

  public void switchTeamSides() {
    if (doSideSwitch) {
      Team sideOneTeam = getSideTeam(0); 
      for (Integer side = 0; side < teamSide.size(); side++) {
        Team team = getSideTeam(side);
        teamSide.put(team, side - 1);
      }

      teamSide.put(sideOneTeam, teamSide.size()-1);
    }
  }

  public Integer getTeamSide(Team team) {
    return teamSide.get(team);
  }

  public Location getSideFlagLocation(Integer side) {
    if (sideFlagLocation.get(side) != null) {
      return sideFlagLocation.get(side).clone();
    }
    return null;
  }

  public void spawnFlag(Integer side) {
    Team team = getSideTeam(side);
    String bannerType  = team.getColor().name() + "_BANNER";
    Location flagLocation = getSideFlagLocation(side).clone();
    Material flagType = Material.getMaterial(bannerType);

    flagLocation.add(0, 1, 0);
    Block teamFlag = flagLocation.getBlock();
    Location teamFlagBase = flagLocation.clone();
    teamFlagBase.add(0, -1, 0);
    Block teamFlagBaseBlock = teamFlagBase.getBlock();

    teamFlag.setType(Material.AIR);
    teamFlagBaseBlock.setType(Material.OBSIDIAN);
    teamFlag.setType(flagType);
  }

  public void spawnFlags() {
    for (int side = 0; side < sideFlagLocation.size(); side++) {
      spawnFlag(side);
    }
  }

  public void respawnFlag(Team team) {
    spawnFlag(getTeamSide(team));
  }

  public void setSideFlagLocation(Integer side, Location location) {
    sideFlagLocation.put(side, location);
  }

  public void setSideAFlagLocation(Location location) {
    setSideFlagLocation(0, location);
  }

  public void setSideBFlagLocation(Location location) {
    setSideFlagLocation(1, location);
  }

  public Location getSideAFlagLocation() {
    return getSideFlagLocation(0);
  }

  public Location getSideBFlagLocation() {
    return getSideFlagLocation(1);
  }

  public void loadMapFromFile(List<Location> soloSpawnLocations, List<Location> sideASpawnLocations, List<Location> sideBSpawnLocations, Location sideAFlagLocation, Location sideBFlagLocation) {
    this.soloSpawnLocations = soloSpawnLocations;
    this.sideSpawnLocations.put(0, sideASpawnLocations);
    this.sideSpawnLocations.put(1, sideBSpawnLocations);

    this.sideFlagLocation.put(0, sideAFlagLocation);
    this.sideFlagLocation.put(1, sideBFlagLocation);
  }

  public void addSupportedGameMode(GameMode gameMode) {
    supportedGameModes.add(gameMode);
  }

  public boolean locationInMap(Location location) {
    double[] dim = new double[2];
 
    dim[0] = cornerA.getX();
    dim[1] = cornerB.getX();
    Arrays.sort(dim);
    if(location.getX() > dim[1] || location.getX() < dim[0])
        return false;
 
    dim[0] = cornerA.getZ();
    dim[1] = cornerB.getZ();
    Arrays.sort(dim);
    if(location.getZ() > dim[1] || location.getZ() < dim[0])
        return false;
 
    return true;
  }

  public void setBoundry(Location cornerA, Location cornerB) {
    this.cornerA = cornerA;
    this.cornerB = cornerB;
  }

  public boolean playerInMap(Player player) {
    return locationInMap(player.getLocation());
  }

  public void addSoloSpawnpoint(Location location) {
    soloSpawnLocations.add(location);
  }

  public void addSideSpawnPoint(Integer side, Location location) {
    sideSpawnLocations.get(side).add(location);
  }

  public void addSideASpawnPoint(Location location) {
    addSideSpawnPoint(0, location);
  }

  public void addSideBSpawnPoint(Location location) {
    addSideSpawnPoint(1, location);
  }

  public Location getSpawnPoint(Team team) {
    Random random = new Random();
    Location spawnPoint = null;
    if (team == null) {
      if (soloSpawnLocations.size() > 0) {
        Integer spawnInt = random.nextInt(soloSpawnLocations.size());
        spawnPoint = soloSpawnLocations.get(spawnInt);
      }
    } else {
      Integer playerTeamSide = getTeamSide(team);
      List<Location> spawnLocations = getSideSpawnLocations(playerTeamSide);
      if (spawnLocations != null && (spawnLocations.size() > 0)) {
        Integer spawnInt = random.nextInt(spawnLocations.size());
        spawnPoint = spawnLocations.get(spawnInt);
      }
    }

    if (spawnPoint != null) {
      Location adjustedSpawnPoint = spawnPoint.clone();
      adjustedSpawnPoint.add(0, 1, 0);
      return adjustedSpawnPoint;
    }
    return null;
  }

  public Location getSpawnPoint() {
    return getSpawnPoint(null);
  }
}
