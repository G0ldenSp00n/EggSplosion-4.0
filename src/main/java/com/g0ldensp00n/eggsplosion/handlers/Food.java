package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.Plugin;

public class Food implements Listener {

  public Food(Plugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }
  @EventHandler
  public void playerGetHungery(FoodLevelChangeEvent event) {
    event.setCancelled(true);
  }
}
