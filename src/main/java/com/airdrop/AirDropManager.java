package com.airdrop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Barrel;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class AirDropManager {
    private final JavaPlugin plugin;
    private BukkitTask airDropTask;
    private final int INTERVAL_MINUTES = 60; // 60 минут между дропами
    private final int TIMER_SECONDS = 300; // 5 минут = 300 секунд
    private final double RARE_CHANCE = 0.20; // 20% шанс редкого дропа
    private final int RTP_RADIUS = 1000; // Радиус RTP в блоках
    
    private Map<Location, AirDropData> activeDrops = new HashMap<>();

    public AirDropManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startAirDropScheduler() {
        // Запускаем первый дроп через 1 минуту, потом каждые 60 минут
        airDropTask = new BukkitRunnable() {
            @Override
            public void run() {
                createAirDrop();
            }
        }.runTaskTimer(plugin, 20L * 60, 20L * 60 * INTERVAL_MINUTES); // 20 ticks = 1 sec
    }

    public void createAirDrop() {
        World world = Bukkit.getWorlds().get(0); // Основной мир
        boolean isRare = Math.random() < RARE_CHANCE;
        
        // Генерируем случайные координаты RTP
        Location dropLocation = generateRTPLocation(world, isRare);
        
        // Размещаем бочку на 200 блоков выше
        Location barrelLocation = dropLocation.clone().add(0, 200, 0);
        
        // Создаём бочку
        Block barrelBlock = barrelLocation.getBlock();
        barrelBlock.setType(Material.BARREL);
        
        // Получаем состояние бочки
        Barrel barrel = (Barrel) barrelBlock.getState();
        
        // Заполняем бочку предметами
        if (isRare) {
            fillRareDrops(barrel);
        } else {
            fillCommonDrops(barrel);
        }
        barrel.update();
        
        // Сохраняем информацию о дропе
        AirDropData data = new AirDropData(barrelLocation, isRare);
        activeDrops.put(barrelLocation, data);
        
        // Выводим сообщение в чат
        String type = isRare ? "§6§lРЕДКИЙ" : "§7обычный";
        Bukkit.broadcastMessage("§e[AirDrop] " + type + " §eдроп! §7Координаты: §fX: " + dropLocation.getBlockX() + ", Y: " + dropLocation.getBlockY() + ", Z: " + dropLocation.getBlockZ());
        
        // Запускаем таймер 5 минут
        startDropTimer(barrelLocation, TIMER_SECONDS);
    }

    private void startDropTimer(Location barrelLocation, int seconds) {
        new BukkitRunnable() {
            int remainingTime = seconds;
            
            @Override
            public void run() {
                Block barrelBlock = barrelLocation.getBlock();
                
                // Проверяем, жива ли бочка
                if (barrelBlock.getType() != Material.BARREL) {
                    this.cancel();
                    activeDrops.remove(barrelLocation);
                    return;
                }
                
                remainingTime -= 1;
                
                if (remainingTime <= 0) {
                    // Взрываем бочку
                    explodeBarrel(barrelLocation);
                    this.cancel();
                } else if (remainingTime % 60 == 0) {
                    // Выводим сообщение каждую минуту
                    int minutesLeft = remainingTime / 60;
                    Bukkit.broadcastMessage("§e[AirDrop] Взрыв через §f" + minutesLeft + " мин");
                }
            }
        }.runTaskTimer(plugin, 0, 20L); // 20 ticks = 1 сек
    }

    private void explodeBarrel(Location barrelLocation) {
        Block barrelBlock = barrelLocation.getBlock();
        
        if (barrelBlock.getType() == Material.BARREL) {
            // Получаем содержимое бочки
            Barrel barrel = (Barrel) barrelBlock.getState();
            ItemStack[] contents = barrel.getInventory().getContents();
            
            // Удаляем бочку
            barrelBlock.setType(Material.AIR);
            
            // Взрыв эффект
            barrelLocation.getWorld().createExplosion(barrelLocation, 3.0f, false, true);
            
            // Разбрасываем предметы в разные стороны
            for (ItemStack item : contents) {
                if (item != null && item.getType() != Material.AIR) {
                    Location dropLoc = barrelLocation.clone().add(
                        (Math.random() - 0.5) * 10,
                        Math.random() * 5 + 1,
                        (Math.random() - 0.5) * 10
                    );
                    barrelLocation.getWorld().dropItem(dropLoc, item.clone());
                }
            }
            
            // Выводим сообщение
            Bukkit.broadcastMessage("§c[AirDrop] Дроп взорвался на координатах X: " + barrelLocation.getBlockX() + ", Y: " + barrelLocation.getBlockY() + ", Z: " + barrelLocation.getBlockZ());
        }
        
        activeDrops.remove(barrelLocation);
    }

    private Location generateRTPLocation(World world, boolean isRare) {
        Random random = new Random();
        int x = random.nextInt(RTP_RADIUS * 2) - RTP_RADIUS;
        int z = random.nextInt(RTP_RADIUS * 2) - RTP_RADIUS;
        
        // Получаем высоту на уровне земли
        int y = world.getHighestBlockYAt(x, z);
        
        return new Location(world, x, y, z);
    }

    private void fillCommonDrops(Barrel barrel) {
        barrel.getInventory().addItem(
            new ItemStack(Material.IRON_INGOT, 32),
            new ItemStack(Material.GOLD_INGOT, 16),
            new ItemStack(Material.COPPER_INGOT, 24),
            new ItemStack(Material.COAL, 32),
            new ItemStack(Material.EMERALD, 8)
        );
    }

    private void fillRareDrops(Barrel barrel) {
        barrel.getInventory().addItem(
            new ItemStack(Material.DIAMOND, 16),
            new ItemStack(Material.EMERALD, 24),
            new ItemStack(Material.GOLD_INGOT, 32),
            new ItemStack(Material.NETHERITE_INGOT, 4),
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4)
        );
    }

    public void stopScheduler() {
        if (airDropTask != null) {
            airDropTask.cancel();
        }
    }

    public Map<Location, AirDropData> getActiveDrops() {
        return activeDrops;
    }

    public static class AirDropData {
        public Location location;
        public boolean isRare;
        public long createdTime;

        public AirDropData(Location location, boolean isRare) {
            this.location = location;
            this.isRare = isRare;
            this.createdTime = System.currentTimeMillis();
        }
    }
}