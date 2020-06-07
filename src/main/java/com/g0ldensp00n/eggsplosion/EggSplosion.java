package com.g0ldensp00n.eggsplosion;

import com.g0ldensp00n.eggsplosion.handlers.RespawnHandler;
import com.g0ldensp00n.eggsplosion.handlers.EggExplode;
import com.g0ldensp00n.eggsplosion.handlers.ExplosionRegen;
import com.g0ldensp00n.eggsplosion.handlers.Weapon;
import com.g0ldensp00n.eggsplosion.handlers.WeaponSelector;
import com.g0ldensp00n.eggsplosion.handlers.DeathMessages;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EggSplosion extends JavaPlugin{
    private String versionNumber;
    private ExplosionRegen explosionRegen;

    @Override
    public void onEnable() {
        versionNumber = Bukkit.getServer().getPluginManager().getPlugin("EggSplosion").getDescription().getVersion();
        getLogger().info("Enabled EggSplosion v" + versionNumber);
        explosionRegen = new ExplosionRegen(this);
        EggExplode eggExplode = new EggExplode(this);
        Weapon weapon = new Weapon(this);
        RespawnHandler death = new RespawnHandler(this);
        WeaponSelector shop = new WeaponSelector(this);
        DeathMessages deathMessages = new DeathMessages(this);
    }

    @Override
    public void onDisable() {
        explosionRegen.repairAll();
        getLogger().info("Disabled EggSplosion v" + versionNumber); 
    }
}
