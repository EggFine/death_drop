package com.blbilink.mc.death_drop;

import org.bukkit.plugin.java.JavaPlugin;

public class DeathDrop extends JavaPlugin {
    @Override
    public void onEnable() {
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new DeathDropListener(), this);
        getLogger().info("DeathDrop plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DeathDrop plugin has been disabled!");
    }
} 