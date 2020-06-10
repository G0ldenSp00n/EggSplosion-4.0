package com.g0ldensp00n.eggsplosion.handlers.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GameMap {
  private Location cornerA;
  private Location cornerB;
  private List<Location> soloSpawnLocations;
  private List<Location> teamASpawnLocations;
  private List<Location> teamBSpawnLocations;
  private List<GameMode> supportedGameModes;

  public GameMap(Location cornerA, Location cornerB) {
    this.cornerA = cornerA;
    this.cornerB = cornerB;

    soloSpawnLocations = new ArrayList<Location>();
    teamASpawnLocations = new ArrayList<Location>();
    teamBSpawnLocations = new ArrayList<Location>();

    supportedGameModes = new ArrayList<GameMode>();
  }

  public Location getCornerA() {
    return cornerA;
  }

  public Location getCornerB() {
    return cornerB;
  }

  public List<Location> getSoloSpawnLocations() {
    return soloSpawnLocations;
  }

  public List<Location> getTeamASpawnLocations() {
    return teamASpawnLocations;
  }

  public List<Location> getTeamBSpawnLocations() {
    return teamBSpawnLocations;
  }

  public void loadMapFromFile(List<Location> soloSpawnLocations, List<Location> teamASpawnLocations, List<Location> teamBSpawnLocations) {
    this.soloSpawnLocations = soloSpawnLocations;
    this.teamASpawnLocations = teamASpawnLocations;
    this.teamBSpawnLocations = teamBSpawnLocations;
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

  public void addTeamASpawnPoint(Location location) {
    teamASpawnLocations.add(location);
  }

  public void addTeamBSpawnPoint(Location location) {
    teamBSpawnLocations.add(location);
  }

  public Location getSpawnPoint(Team team) {
    Random random = new Random();
    Location spawnPoint = null;
    if (team == Team.SOLO) {
      Integer spawnInt = random.nextInt(soloSpawnLocations.size());
      spawnPoint = soloSpawnLocations.get(spawnInt);
    } else if (team == Team.TEAM_A) {
      Integer spawnInt = random.nextInt(teamASpawnLocations.size());
      spawnPoint = teamASpawnLocations.get(spawnInt);
    } else if (team == Team.TEAM_B) {
      Integer spawnInt = random.nextInt(teamBSpawnLocations.size());
      spawnPoint = teamBSpawnLocations.get(spawnInt);
    }

    if (spawnPoint != null) {
      Location adjustedSpawnPoint = spawnPoint.clone();
      adjustedSpawnPoint.add(0, 1, 0);
      return adjustedSpawnPoint;
    }
    return null;
  }
}
