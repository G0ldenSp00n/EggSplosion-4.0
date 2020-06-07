package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.Plugin;

public class EggExplode implements Listener {

public EggExplode(Plugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
}

@EventHandler
public void entityCollide(ProjectileHitEvent projectileHitEvent) {
    if (projectileHitEvent.getEntity().getType() == EntityType.EGG) {
      if (projectileHitEvent.getEntity().getName().split(" / ").length > 0) {

        int explosionPower = Integer.parseInt(projectileHitEvent.getEntity().getName().split(" / ")[1]);
        Entity entityShooter = (Entity) projectileHitEvent.getEntity().getShooter();
        if (Bukkit.getOfflinePlayer(entityShooter.getUniqueId()) != null) {
          Player playerShooter = Bukkit.getOfflinePlayer(entityShooter.getUniqueId()).getPlayer();
          World world = projectileHitEvent.getEntity().getWorld();
          Location location = projectileHitEvent.getEntity().getLocation();

          location.getWorld().createExplosion(location, explosionPower, false, true, (Entity) playerShooter);
        }
      }
    }
  }
}
