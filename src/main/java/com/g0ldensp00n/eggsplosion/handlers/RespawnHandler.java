package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import java.util.*;

public class RespawnHandler implements Listener {
    public RespawnHandler(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    List<Location> spawnPoints = new ArrayList<>();

    @EventHandler
    public void playerTakeDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity().getType().equals(EntityType.PLAYER)) {
          Player player = (Player) entityDamageEvent.getEntity();
          if ((player.getHealth() - entityDamageEvent.getFinalDamage()) <= 0) {
            entityDamageEvent.setCancelled(true);
            player.setHealth(20);
            if (spawnPoints.size() > 0) {
              Random rand = new Random();
              player.teleport(spawnPoints.get(rand.nextInt(spawnPoints.size())));
            } else {
              player.teleport(player.getWorld().getSpawnLocation());
            }
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 1, 1);
          }
        }
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
      if (playerInteractEvent.getItem() != null) {
        if((playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
            switch(playerInteractEvent.getItem().getType()){
                case WOODEN_AXE:
                  Player spawnPointSetter = playerInteractEvent.getPlayer();
                  Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation().add(0, 1, 0);
                  spawnPoint.setYaw(spawnPointSetter.getLocation().getYaw());
                  spawnPointSetter.sendMessage("Spawn Point Added");
                  spawnPoints.add(spawnPoint);
                  break;
                default:
                  break;
            }
        }
      }
    }

}
