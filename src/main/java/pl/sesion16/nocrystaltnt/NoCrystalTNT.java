package pl.sesion16.nocrystaltnt;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoCrystalTNT extends JavaPlugin implements Listener {

    private final Set<Material> protectedMaterials = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadProtectedBlocks();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NoCrystalTNT v1.1.0 zostało pomyślnie włączone!");
    }

    @Override
    public void onDisable() {
        protectedMaterials.clear();
        getLogger().info("NoCrystalTNT zostało wyłączone.");
    }

    private void loadProtectedBlocks() {
        protectedMaterials.clear();
        if (!getConfig().getBoolean("protected-blocks.enabled", true)) return;

        List<String> blockNames = getConfig().getStringList("protected-blocks.list");
        for (String name : blockNames) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                protectedMaterials.add(mat);
            } else {
                getLogger().warning("Nie znaleziono bloku o nazwie: " + name + " w Bukkit Material!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        EntityType type = event.getEntityType();

        if (type == EntityType.ENDER_CRYSTAL && getConfig().getBoolean("settings.disable-crystal-explosions", true)) {
            event.setCancelled(true);
        } else if (type == EntityType.MINECART_TNT && getConfig().getBoolean("settings.disable-tnt-minecart-explosions", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();

        if (type == EntityType.ENDER_CRYSTAL && getConfig().getBoolean("settings.disable-crystal-explosions", true)) {
            event.setCancelled(true);
            event.blockList().clear();
            return;
        } else if (type == EntityType.MINECART_TNT && getConfig().getBoolean("settings.disable-tnt-minecart-explosions", true)) {
            event.setCancelled(true);
            event.blockList().clear();
            return;
        }

        // Usuwanie z listy zniszczeń bloków podanych w configu
        if (getConfig().getBoolean("protected-blocks.enabled", true) && !protectedMaterials.isEmpty()) {
            event.blockList().removeIf(block -> protectedMaterials.contains(block.getType()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (getConfig().getBoolean("protected-blocks.enabled", true) && !protectedMaterials.isEmpty()) {
            event.blockList().removeIf(block -> protectedMaterials.contains(block.getType()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() == null) return;

        EntityType damagerType = event.getDamager().getType();

        if (damagerType == EntityType.ENDER_CRYSTAL && getConfig().getBoolean("settings.disable-crystal-explosions", true)) {
            event.setCancelled(true);
        } else if (damagerType == EntityType.MINECART_TNT && getConfig().getBoolean("settings.disable-tnt-minecart-explosions", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrystalOrMinecartDamage(EntityDamageEvent event) {
        EntityType type = event.getEntityType();

        if (type == EntityType.ENDER_CRYSTAL && getConfig().getBoolean("settings.disable-crystal-explosions", true)) {
            event.setCancelled(true);
            if (getConfig().getBoolean("settings.remove-crystal-on-hit", true)) {
                event.getEntity().remove();
            }
        } else if (type == EntityType.MINECART_TNT && getConfig().getBoolean("settings.disable-tnt-minecart-explosions", true)) {
            if (getConfig().getBoolean("settings.remove-tnt-minecart-on-hit", false)) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
    }
}
