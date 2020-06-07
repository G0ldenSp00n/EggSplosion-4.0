package com.g0ldensp00n.eggsplosion.handlers;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Chest;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Comparator;
import java.util.Collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ExplosionRegen implements Listener {

    private Location location;
    private BlockData blockData;
    private Material material;
    private byte data;
    private Inventory inventory;
    private List<ExplosionRegen> blockQueue = new ArrayList<>();
    private List<ExplosionRegen> transparentQueue = new ArrayList<>();
    private List<Inventory> blockInventory = new ArrayList<>();
    private Random random;

    private EggSplosion plugin;

    public ExplosionRegen(EggSplosion plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private ExplosionRegen(Block block) {
        this.location = block.getLocation();
        this.material = block.getType();
        this.blockData = block.getBlockData();
        this.data = block.getData();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            inventory = Bukkit.createInventory(null, 27, "" + chest.getLocation().getBlockX());
            if (chest.getInventory().getSize() <= 27) {
                inventory.setContents(chest.getBlockInventory().getContents());
            } else {
                Bukkit.getLogger().info("Double chest -> WIP");
            }

        }
    }


    /*
    private ExplosionRegen(Block block, Bisected.Half bisectedHalf) {
        this.location = block.getLocation();
        this.material = block.getType();
        this.bisectedHalf = bisectedHalf;
        this.data = block.getData();
    }
    */

    public void update() {
      if (material != Material.AIR) {
        if (material.hasGravity()) {
          Block blockBelow = location.clone().add(0, -1, 0).getBlock();
          if (!blockBelow.getType().isSolid()) {
            blockBelow.setType(Material.SANDSTONE);
          }
        }
        Block block = location.getBlock();
        block.setType(material);
        block.setBlockData(blockData);
        try {
          Block blockBelow = location.clone().add(0, -1, 0).getBlock();
          if (blockBelow.getType() != material) {
            Bisected testBisected = (Bisected) blockData;

            if (!blockBelow.getType().isSolid()) {
              blockBelow.setType(Material.GRASS_BLOCK);
            }

            // Upper Block
            Block upperBlock = location.clone().add(0, 1, 0).getBlock();
            upperBlock.setType(material, false);

            //Lower Block
            BlockData lowerBlockData = block.getBlockData();
            ((Bisected) lowerBlockData).setHalf(Bisected.Half.BOTTOM);
            block.setBlockData(lowerBlockData);

            //Upper Block
            Bisected upperBlockData = (Bisected) upperBlock.getBlockData();
            upperBlockData.setHalf(Bisected.Half.TOP);
            upperBlock.setBlockData((BlockData) upperBlockData);
          }
        } catch (ClassCastException e) {
          // Do Nothing not a Bisected Block
        }
        if (location.getBlock().getType().equals(Material.CHEST)) {
            Chest chest = (Chest) location.getBlock().getState();
            chest.getInventory().setContents(inventory.getContents());
        }
      }
    }

    @EventHandler
    public void explosionRegeneration(EntityExplodeEvent event) {
      event.setCancelled(true);

      final int delay = 20 * 3;
      List<ExplosionRegen> blockInfo = new ArrayList<>();
      List<ExplosionRegen> transparentBlocks = new ArrayList<>();
      List<Block> blocks = event.blockList();
      Collections.sort(blocks, new Comparator<Block>() {
        @Override
        public int compare(Block blockA, Block blockB) {
          if ((int) blockA.getLocation().getY() == blockB.getLocation().getY()) {
            return 0;
          }
          return (((int)blockA.getLocation().getY()) > ((int)blockB.getLocation().getY())) ? -1 : 1;
        }
      });


      blocks.forEach(block -> {
        // Handle Gravity Blocks
        Block blockAbove = block.getLocation().add(0, 1, 0).getBlock();
        if (blockAbove.getType().hasGravity()) {
          blockInfo.add(new ExplosionRegen(blockAbove));
          blockAbove.setType(Material.AIR, false);

          int blockAboveY = (int) blockAbove.getLocation().getY();
          // Handle Stacks of Gravity Blocks
          for (int blockY = 1; blockY <= (255 - blockAboveY); blockY++) {
            Block blockStackCheck = blockAbove.getLocation().add(0, blockY, 0).getBlock();
            if (blockStackCheck.getType().hasGravity()) {
              blockInfo.add(new ExplosionRegen(blockStackCheck));
              blockStackCheck.setType(Material.AIR, false);
            } else {
              break;
            }
          }
        }

        // Handle Solid Block
          if (block.getType().isSolid()) {
              if (!block.getType().equals(Material.TNT) && !block.getType().equals(Material.BLUE_BANNER)) {
                  blockInfo.add(new ExplosionRegen(block));
                  block.setType(Material.AIR, false);
              } else if (block.getType().equals(Material.TNT)) {
                  blockInfo.add(new ExplosionRegen(block));
                  block.setType(Material.AIR, false);
                  TNTPrimed tntPrimed = block.getWorld().spawn(block.getLocation(), TNTPrimed.class);
                  tntPrimed.setFuseTicks(5);
              }
        // Handle Non-Solid Blocks
          } else {
            Bisected bisectedBlock = null;
            try {
              bisectedBlock = (Bisected) block.getBlockData();
            } catch (ClassCastException exception) {
              // Do Nothing - Not Bisected
            }
            if (bisectedBlock != null) {
              Block blockBelow = block.getLocation().add(0, -1, 0).getBlock();
              transparentBlocks.add(new ExplosionRegen(blockBelow));
              blockBelow.setType(Material.AIR, false);
            } else {
              transparentBlocks.add(new ExplosionRegen(block));
            }
            block.setType(Material.AIR, false);
          }
      });

      // Add to Cleanup Queue
      blockQueue.addAll(blockInfo);
      Collections.sort(blockInfo, new Comparator<ExplosionRegen>() {
        @Override
        public int compare(ExplosionRegen blockA, ExplosionRegen blockB) {
          if ((int) blockA.location.getY() == blockB.location.getY()) {
            return 0;
          }
          return (((int)blockA.location.getY()) < ((int)blockB.location.getY())) ? -1 : 1;
        }
      });
      transparentQueue.addAll(transparentBlocks);
      Collections.sort(transparentBlocks, new Comparator<ExplosionRegen>() {
        @Override
        public int compare(ExplosionRegen blockA, ExplosionRegen blockB) {
          if ((int) blockA.location.getY() == blockB.location.getY()) {
            return 0;
          }
          return (((int)blockA.location.getY()) < ((int)blockB.location.getY())) ? -1 : 1;
        }
      });

      // Combine Solid and Non-Solid to a single list
      List<ExplosionRegen> allBlocks = new ArrayList(blockInfo);
      allBlocks.addAll(transparentBlocks);

      // Runnable to Delay Regen
      new BukkitRunnable() {
          Iterator<ExplosionRegen> allBlocksQueue = allBlocks.iterator();

          @Override
          public void run() {
              for (int blocksPlaced = 0; blocksPlaced < 10; blocksPlaced++) {
                if (allBlocksQueue.hasNext()) {
                  ExplosionRegen blockToPlace = allBlocksQueue.next();
                  blockToPlace.update();
                  allBlocksQueue.remove();
                } else {
                  blockInfo.clear();
                  transparentBlocks.clear();
                  cancel();
                  break;
                }
              }
          }
      }.runTaskTimer(this.plugin, delay, (long) 0.01);
    }

    public void repairAll() {
        Iterator<ExplosionRegen> blocksQueue = blockQueue.iterator();
        while (blocksQueue.hasNext()) {
            ExplosionRegen explosionRegenBlock = blocksQueue.next();
            explosionRegenBlock.update();
            blocksQueue.remove();
        }
        Iterator<ExplosionRegen> transparentsQueue = transparentQueue.iterator();
        while (blocksQueue.hasNext()) {
            ExplosionRegen explosionRegenTrans = transparentsQueue.next();
            explosionRegenTrans.update();
            transparentsQueue.remove();
        }
    }
}
