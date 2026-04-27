package com.airdrop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AirDropCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final AirDropManager airDropManager;

    public AirDropCommand(JavaPlugin plugin, AirDropManager airDropManager) {
        this.plugin = plugin;
        this.airDropManager = airDropManager;
        plugin.getCommand("airdrop").setExecutor(this);
        plugin.getCommand("airdropstatus").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("airdrop")) {
            if (sender.hasPermission("airdrop.admin")) {
                airDropManager.createAirDrop();
                sender.sendMessage("§e[AirDrop] Дроп создан вручную!");
                return true;
            } else {
                sender.sendMessage("§cУ вас нет прав на эту команду!");
                return true;
            }
        }
        
        if (label.equalsIgnoreCase("airdropstatus")) {
            int activeCount = airDropManager.getActiveDrops().size();
            sender.sendMessage("§e[AirDrop] Активных дропов: §f" + activeCount);
            return true;
        }
        
        return false;
    }
}