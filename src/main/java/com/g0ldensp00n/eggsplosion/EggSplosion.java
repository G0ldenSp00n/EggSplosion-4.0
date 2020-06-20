package com.g0ldensp00n.eggsplosion;

import com.g0ldensp00n.eggsplosion.handlers.RespawnHandler;
import com.g0ldensp00n.eggsplosion.handlers.EggExplode;
import com.g0ldensp00n.eggsplosion.handlers.ExplosionRegen;
import com.g0ldensp00n.eggsplosion.handlers.Weapon;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.LobbyMenuSystem;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.GameModeListeners;
import com.g0ldensp00n.eggsplosion.handlers.ArmorRemoveHandler;
import com.g0ldensp00n.eggsplosion.handlers.DeathMessages;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.GameModeListeners;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.Food;
import com.g0ldensp00n.eggsplosion.handlers.PickupDropHandler;
import com.g0ldensp00n.eggsplosion.handlers.Lobby.MapManager;
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
        DeathMessages deathMessages = new DeathMessages(this, lobbyManager);
        EggExplode eggExplode = new EggExplode(this);
        Weapon weapon = new Weapon(this);
        GameModeListeners gameModeListeners = new GameModeListeners(this, lobbyManager);
        PickupDropHandler pickupDropHandler = new PickupDropHandler(this, lobbyManager);
        ArmorRemoveHandler armorRemoveHandler = new ArmorRemoveHandler(this, lobbyManager);
        RespawnHandler death = new RespawnHandler(this, lobbyManager);
        LobbyMenuSystem shop = new LobbyMenuSystem(this, lobbyManager, mapManager);
        Food food = new Food(this);

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
