package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class DeathMessages implements Listener {
    public DeathMessages(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerTakeDamage(EntityDamageByEntityEvent entityDamageEvent) {
      Player player = Bukkit.getServer().getOfflinePlayer(entityDamageEvent.getEntity().getUniqueId()).getPlayer();
      Player damager = Bukkit.getServer().getOfflinePlayer(entityDamageEvent.getDamager().getUniqueId()).getPlayer();
      if (damager != null && player != null) {
        if ((player.getHealth() - entityDamageEvent.getFinalDamage()) <= 0) {
          Bukkit.getServer().broadcastMessage(player.getDisplayName() + " was scrambled by " + damager.getDisplayName());
        }
      }
    }
}
