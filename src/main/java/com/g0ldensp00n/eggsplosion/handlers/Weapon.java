package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Weapon implements Listener {
    private ArrayList<Player> reloadingPlayers = new ArrayList<>();
    private Plugin plugin;

    private long timeMilis;

    public Weapon(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
      if (playerInteractEvent.getItem() != null) {
        if((playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_AIR) || playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
            switch(playerInteractEvent.getItem().getType()){
                case DIAMOND_HOE:
                        playerInteractEvent.setCancelled(true);
                        launchWeapon(playerInteractEvent.getPlayer(), 2, 6, 20);
                    break;
                case GOLDEN_HOE:
                        playerInteractEvent.setCancelled(true);
                        launchWeapon(playerInteractEvent.getPlayer(), 2, 7, 25);
                    break;
                case IRON_HOE:
                        playerInteractEvent.setCancelled(true);
                        launchWeapon(playerInteractEvent.getPlayer(), 2, 5, 30);
                    break;
                case STONE_HOE:
                        playerInteractEvent.setCancelled(true);
                        launchWeapon(playerInteractEvent.getPlayer(), 1, 5, 35);
                    break;
                case WOODEN_HOE:
                        playerInteractEvent.setCancelled(true);
                        launchWeapon(playerInteractEvent.getPlayer(), 1, 4, 40);
                    break;
                default:
                    break;
            }
        }
      }
    }

    private void launchWeapon(Player player, int velocityMultiplier,  int explosionPower, long reloadTime) {
        if(timeMilis + (reloadTime * 50) < System.currentTimeMillis()){
            reloadingPlayers.remove(player);
            timeMilis = System.currentTimeMillis();
        }

        if (!reloadingPlayers.contains(player)) {
            fireWeapon(player, velocityMultiplier, explosionPower);
            reloadingPlayers.add(player);
            xpLoading(player, reloadTime);
        }
    }

    private void xpLoading(Player player, long delayLength) {
        new BukkitRunnable() {
            float maxXP = 1.0F;
            float divideXP = maxXP / delayLength;

            @Override
            public void run() {
                if (maxXP > 0) {
                    player.setExp(maxXP);
                    maxXP = maxXP - divideXP;
                } else {
                    player.setExp(0);
                    player.playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_CRACK, 1, 1);
                    cancel();
                }
            }
        }.runTaskTimer(this.plugin, 0, 1);
    }

    private void fireWeapon(Player player, int velocityMultiplier, int explosionSize){
      player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.2f, 1f);
        Egg egg = player.launchProjectile(Egg.class);
        egg.setCustomName(player.getUniqueId() + " / " + explosionSize);
        egg.setVelocity(egg.getVelocity().multiply(velocityMultiplier));
    }
}
