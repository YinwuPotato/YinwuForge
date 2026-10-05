package com.yinwu.manager;

import com.yinwu.YinwuForgePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 浓缩材料掉落：玩家击杀对应怪物 / 挖掘对应方块，低概率额外掉落。
 * 参考 YinwuEnchant mob-drops 实现，结构镜像 EnchantmentAcquisitionManager。
 */
public class MaterialDropManager implements Listener {
    private final YinwuForgePlugin plugin;
    private final MaterialConfig materialConfig;
    private Map<String, MaterialAcq> acquisitionMap = Map.of();

    private record MobDrop(String entity, double chance, int amountMin, int amountMax) {}
    private record BlockDrop(String block, double chance, int amountMin, int amountMax) {}
    private record MaterialAcq(boolean enabled, List<MobDrop> mobDrops, List<BlockDrop> blockDrops) {}

    public MaterialDropManager(YinwuForgePlugin plugin, MaterialConfig materialConfig) {
        this.plugin = plugin;
        this.materialConfig = materialConfig;
        reload();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {
        Map<String, MaterialAcq> map = new HashMap<>();
        ConfigurationSection acq = plugin.getConfig().getConfigurationSection("acquisition");
        if (acq != null) {
            for (String id : acq.getKeys(false)) {
                ConfigurationSection s = acq.getConfigurationSection(id);
                if (s == null || !s.getBoolean("enabled", true)) continue;

                List<MobDrop> mobDrops = new ArrayList<>();
                List<?> raw = s.getList("mob-drops");
                if (raw != null) {
                    for (Object o : raw) {
                        if (!(o instanceof Map<?, ?> m)) continue;
                        mobDrops.add(new MobDrop(
                            str(m, "entity"),
                            num(m, "chance", 0.0).doubleValue(),
                            num(m, "amount-min", 1).intValue(),
                            num(m, "amount-max", 1).intValue()
                        ));
                    }
                }

                List<BlockDrop> blockDrops = new ArrayList<>();
                List<?> raw2 = s.getList("block-drops");
                if (raw2 != null) {
                    for (Object o : raw2) {
                        if (!(o instanceof Map<?, ?> m)) continue;
                        blockDrops.add(new BlockDrop(
                            str(m, "block"),
                            num(m, "chance", 0.0).doubleValue(),
                            num(m, "amount-min", 1).intValue(),
                            num(m, "amount-max", 1).intValue()
                        ));
                    }
                }

                if (mobDrops.isEmpty() && blockDrops.isEmpty()) continue;
                map.put(id, new MaterialAcq(true, mobDrops, blockDrops));
            }
        }
        acquisitionMap = map;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;
        EntityType type = event.getEntityType();
        var rand = ThreadLocalRandom.current();

        for (Map.Entry<String, MaterialAcq> entry : acquisitionMap.entrySet()) {
            String id = entry.getKey();
            MaterialAcq acq = entry.getValue();
            if (acq.mobDrops().isEmpty()) continue;

            for (MobDrop drop : acq.mobDrops()) {
                EntityType target;
                try { target = EntityType.valueOf(drop.entity().toUpperCase()); } catch (IllegalArgumentException e) { continue; }
                if (type != target) continue;
                if (rand.nextDouble() >= drop.chance()) continue;

                ItemStack item = materialConfig.createItem(id, rollAmount(drop.amountMin(), drop.amountMax(), rand));
                if (item != null) event.getDrops().add(item);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();
        var rand = ThreadLocalRandom.current();

        for (Map.Entry<String, MaterialAcq> entry : acquisitionMap.entrySet()) {
            String id = entry.getKey();
            MaterialAcq acq = entry.getValue();
            if (acq.blockDrops().isEmpty()) continue;

            for (BlockDrop drop : acq.blockDrops()) {
                Material target;
                try { target = Material.valueOf(drop.block().toUpperCase()); } catch (IllegalArgumentException e) { continue; }
                if (blockType != target) continue;
                if (rand.nextDouble() >= drop.chance()) continue;

                ItemStack item = materialConfig.createItem(id, rollAmount(drop.amountMin(), drop.amountMax(), rand));
                if (item != null) {
                    event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation().add(0.5, 0.5, 0.5), item);
                }
                break;
            }
        }
    }

    private int rollAmount(int min, int max, ThreadLocalRandom rand) {
        return min == max ? min : min + rand.nextInt(max - min + 1);
    }

    @SuppressWarnings("unchecked")
    private static String str(Map<?, ?> m, String key) { Object v = m.get(key); return v != null ? v.toString() : ""; }
    @SuppressWarnings("unchecked")
    private static Number num(Map<?, ?> m, String key, Number def) { Object v = m.get(key); return v instanceof Number n ? n : def; }
}
