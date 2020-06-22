package com.g0ldensp00n.eggsplosion.handlers.Core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
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
      if (projectileHitEvent.getEntity().getName().split(" / ").length >= 2) {

        float explosionPower = Float.parseFloat(projectileHitEvent.getEntity().getName().split(" / ")[1]);
        Entity entityShooter = (Entity) projectileHitEvent.getEntity().getShooter();
        if (entityShooter instanceof Player) {
          Player playerShooter = (Player) entityShooter;
          World world = projectileHitEvent.getEntity().getWorld();
          Location location = projectileHitEvent.getEntity().getLocation();

          world.createExplosion(location, explosionPower, false, true, (Entity) playerShooter);
          world.spawnParticle(Particle.EXPLOSION_HUGE, location, 0);
          world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 3, 1);
        }
      }
    }
  }
}
