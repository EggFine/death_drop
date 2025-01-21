package com.blbilink.mc.death_drop;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeathDropListener implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World.Environment worldType = player.getWorld().getEnvironment();

        // 清除默认掉落
        event.getDrops().clear();
        event.setKeepInventory(true);
        
        // 获取玩家背包中非空的物品
        List<ItemStack> nonEmptyItems = new ArrayList<>();
        List<Integer> nonEmptySlots = new ArrayList<>();
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null) {
                nonEmptyItems.add(item);
                nonEmptySlots.add(i);
            }
        }

        // 根据世界类型设置掉落数量和经验百分比
        int itemDropCount;
        double expPercentage;

        switch (worldType) {
            case NORMAL:
                itemDropCount = Math.min(3, nonEmptyItems.size());
                expPercentage = 0.2;
                break;
            case NETHER:
                itemDropCount = Math.min(8, nonEmptyItems.size());
                expPercentage = 0.5;
                break;
            case THE_END:
                itemDropCount = Math.min(15, nonEmptyItems.size());
                expPercentage = 0.8;
                break;
            default:
                itemDropCount = Math.min(3, nonEmptyItems.size());
                expPercentage = 0.2;
                break;
        }

        // 随机选择物品掉落
        for (int i = 0; i < itemDropCount && !nonEmptyItems.isEmpty(); i++) {
            int randomIndex = random.nextInt(nonEmptyItems.size());
            ItemStack itemToDrop = nonEmptyItems.get(randomIndex);
            event.getDrops().add(itemToDrop.clone());
            
            // 从背包中移除被选中掉落的物品
            int slot = nonEmptySlots.get(randomIndex);
            player.getInventory().setItem(slot, null);
            
            // 从列表中移除已处理的物品
            nonEmptyItems.remove(randomIndex);
            nonEmptySlots.remove(randomIndex);
        }

        // 设置经验掉落
        int originalExp = event.getDroppedExp();
        event.setDroppedExp((int) (originalExp * expPercentage));
    }
} 