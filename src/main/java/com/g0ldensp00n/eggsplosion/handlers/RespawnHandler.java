package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class RespawnHandler implements Listener {
    public RespawnHandler(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerTakeDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity().getType().equals(EntityType.PLAYER)) {
          switch ()
          Player player = (Player) entityDamageEvent.getEntity();
          if ((player.getHealth() - entityDamageEvent.getFinalDamage()) <= 0) {
            entityDamageEvent.setCancelled(true);
            player.setHealth(20);
            player.teleport(player.getWorld().getSpawnLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 1, 1);
          }
        }
    }
}
