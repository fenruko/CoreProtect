package net.coreprotect.listener.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.utility.EntitySpawnTracking;
import net.coreprotect.utility.EntityUtils;

public final class EntityPlaceListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        Entity entity = event.getEntity();

        // Cushions (Minecraft 26.3+) are logged as block placements, like armor stands
        if (EntityUtils.isCushion(entity)) {
            if (Config.getConfig(entity.getWorld()).BLOCK_PLACE) {
                Player player = event.getPlayer();
                String user = player == null ? "#dispenser" : player.getName();
                Material cushionMaterial = EntityUtils.getCushionMaterial(entity);
                if (cushionMaterial != null) {
                    Location location = entity.getLocation();
                    Block block = location.getBlock();
                    Queue.queueBlockPlace(user, block.getState(), block.getType(), block.getState(), cushionMaterial, (int) location.getYaw(), 1, null);
                }
            }

            return;
        }

        if (!EntitySpawnTracking.isPlacedEntity(entity) || EntitySpawnTracking.isTracked(entity) || !Config.getConfig(entity.getWorld()).ENTITY_SPAWNS) {
            return;
        }

        Player player = event.getPlayer();
        String user = player == null ? "#dispenser" : player.getName();
        EntitySpawnTracking.track(entity);
        Queue.queueEntitySpawnLog(user, entity.getUniqueId(), entity.getType(), entity.getLocation());
    }
}
