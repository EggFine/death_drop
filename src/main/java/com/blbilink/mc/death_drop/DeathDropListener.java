package com.blbilink.mc.death_drop;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class DeathDropListener implements Listener {
    private final Random random = new Random();
    private final DeathDrop plugin;

    public DeathDropListener(DeathDrop plugin) {
        this.plugin = plugin;
    }

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

        // 根据世界类型设置掉落数量范围和经验百分比
        int minDropCount;
        int maxDropCount;
        double expPercentage;

        switch (worldType) {
            case NORMAL:
                minDropCount = 3;
                maxDropCount = 8;
                expPercentage = 0.2;
                break;
            case NETHER:
                minDropCount = 8;
                maxDropCount = 15;
                expPercentage = 0.5;
                break;
            case THE_END:
                minDropCount = 15;
                maxDropCount = 20;
                expPercentage = 0.8;
                break;
            default:
                minDropCount = 3;
                maxDropCount = 8;
                expPercentage = 0.2;
                break;
        }

        // 在范围内随机选择掉落数量
        int itemDropCount = random.nextInt(maxDropCount - minDropCount + 1) + minDropCount;
        itemDropCount = Math.min(itemDropCount, nonEmptyItems.size());

        // 用于记录掉落物品及其数量
        Map<ItemStack, Integer> droppedItems = new HashMap<>();

        // 随机选择物品掉落
        for (int i = 0; i < itemDropCount && !nonEmptyItems.isEmpty(); i++) {
            int randomIndex = random.nextInt(nonEmptyItems.size());
            ItemStack itemToDrop = nonEmptyItems.get(randomIndex);
            event.getDrops().add(itemToDrop.clone());
            
            // 记录掉落物品
            droppedItems.merge(itemToDrop.clone(), itemToDrop.getAmount(), Integer::sum);
            
            // 从背包中移除被选中掉落的物品
            int slot = nonEmptySlots.get(randomIndex);
            player.getInventory().setItem(slot, null);
            
            // 从列表中移除已处理的物品
            nonEmptyItems.remove(randomIndex);
            nonEmptySlots.remove(randomIndex);
        }

        // 设置经验掉落
        int originalExp = event.getDroppedExp();
        int droppedExp = (int) (originalExp * expPercentage);
        event.setDroppedExp(droppedExp);

        // 构建掉落物品列表的悬浮文本
        Component hoverContent = Component.text("掉落物品列表：").color(NamedTextColor.GOLD);
        
        for (Map.Entry<ItemStack, Integer> entry : droppedItems.entrySet()) {
            ItemStack item = entry.getKey();
            Component itemName;
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemName = item.getItemMeta().displayName();
            } else {
                itemName = Component.translatable(item.getType().translationKey());
            }
            
            hoverContent = hoverContent.append(Component.newline())
                .append(Component.text("- ").color(NamedTextColor.WHITE))
                .append(itemName.color(NamedTextColor.YELLOW))
                .append(Component.text(" x ").color(NamedTextColor.WHITE))
                .append(Component.text(entry.getValue()).color(NamedTextColor.GREEN));
        }
        
        hoverContent = hoverContent.append(Component.newline())
            .append(Component.newline())
            .append(Component.text("掉落经验：").color(NamedTextColor.GOLD))
            .append(Component.text(droppedExp).color(NamedTextColor.GREEN))
            .append(Component.text(" 点").color(NamedTextColor.WHITE));

        // 发送带有悬浮提示的消息
        Component message = Component.text("您已死亡，掉落了 ").color(NamedTextColor.YELLOW)
            .append(Component.text(droppedExp).color(NamedTextColor.GREEN))
            .append(Component.text(" 点经验与部分物品").color(NamedTextColor.YELLOW))
            .append(Component.newline())
            .append(Component.text("[查看掉落物品]")
                .color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(hoverContent)));

        player.sendMessage(message);
    }
} 