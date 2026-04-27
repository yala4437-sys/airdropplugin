package com.airdrop;

import org.bukkit.plugin.java.JavaPlugin;

public class AirDropPlugin extends JavaPlugin {

    private AirDropManager airDropManager;

    @Override
    public void onEnable() {
        getLogger().info("AirDrop Plugin enabled!");
        
        // Initialize the air drop manager
        airDropManager = new AirDropManager(this);
        airDropManager.startAirDropScheduler();
        
        // Register commands
        new AirDropCommand(this, airDropManager);
    }

    @Override
    public void onDisable() {
        getLogger().info("AirDrop Plugin disabled!");
    }

    public AirDropManager getAirDropManager() {
        return airDropManager;
    }
}