package com.g0ldensp00n.eggsplosion.handlers;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class WeaponSelector implements Listener {

    public WeaponSelector(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void selectorTool(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK) || playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_AIR)) {
          if (playerInteractEvent.getItem() != null) {
            if (playerInteractEvent.getItem().getType().equals(Material.EMERALD)) {
                Inventory inventorySelector = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
                playerInteractEvent.getPlayer().openInventory(inventorySelector);
            }
          }
        }
    }
}
