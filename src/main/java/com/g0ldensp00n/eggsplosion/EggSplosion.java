package com.g0ldensp00n.eggsplosion;

import com.g0ldensp00n.eggsplosion.handlers.Core.ArmorRemoveHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.DeathMessages;
import com.g0ldensp00n.eggsplosion.handlers.Core.EggExplode;
import com.g0ldensp00n.eggsplosion.handlers.Core.ExplosionRegen;
import com.g0ldensp00n.eggsplosion.handlers.Core.Food;
import com.g0ldensp00n.eggsplosion.handlers.Core.PickupDropHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.RespawnHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.Weapon;
import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameModeListeners;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyMenuSystem;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EggSplosion extends JavaPlugin {
    private String versionNumber;
    private ExplosionRegen explosionRegen;
    private MapManager mapManager;
    private LobbyManager lobbyManager;
    public String pluginFolder = getDataFolder().getAbsolutePath();

    @Override
    public void onEnable() {
        versionNumber = Bukkit.getServer().getPluginManager().getPlugin("EggSplosion").getDescription().getVersion();
        getLogger().info("Enabled EggSplosion v" + versionNumber);
        explosionRegen = new ExplosionRegen(this);
        mapManager = new MapManager(this, pluginFolder);
        lobbyManager = LobbyManager.getInstance(this, mapManager); 
        new DeathMessages(this, lobbyManager);
        new EggExplode(this);
        new Weapon(this);
        new GameModeListeners(this, lobbyManager);
        new PickupDropHandler(this, lobbyManager);
        new ArmorRemoveHandler(this, lobbyManager);
        new RespawnHandler(this, lobbyManager);
        new LobbyMenuSystem(this, lobbyManager, mapManager);
        new Food(this);

        this.getCommand("lobby").setExecutor(lobbyManager);
        this.getCommand("lobby").setTabCompleter(lobbyManager);
        this.getCommand("map").setExecutor(mapManager);
        this.getCommand("map").setTabCompleter(mapManager);
    }

    @Override
    public void onDisable() {
        explosionRegen.repairAll();
        mapManager.saveMapsToFiles();
        lobbyManager.cleanupLobbies();
        getLogger().info("Disabled EggSplosion v" + versionNumber); 
    }
}
