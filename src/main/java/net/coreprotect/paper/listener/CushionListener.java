package net.coreprotect.paper.listener;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.papermc.paper.event.entity.EntityBreakByEntityEvent;
import io.papermc.paper.event.entity.EntityBreakEvent;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Lookup;
import net.coreprotect.listener.entity.HangingBreakByEntityListener;
import net.coreprotect.utility.EntityUtils;

/**
 * Logs cushion removals (Minecraft 26.3+).
 *
 * Cushions are non-living, block-attached entities, so they do not fire
 * EntityDamageEvent/EntityDeathEvent or hanging events. Paper 26.3 introduced
 * EntityBreakEvent and EntityBreakByEntityEvent for their removal, which this
 * listener consumes. Placement is handled by the shared EntityPlaceListener.
 *
 * This listener is only registered when io.papermc.paper.event.entity.EntityBreakEvent
 * is present (Paper 26.3+), mirroring how other Paper-only listeners are gated.
 */
public final class CushionListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    protected void onCushionBreakByEntity(EntityBreakByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!EntityUtils.isCushion(entity)) {
            return;
        }

        Entity remover = event.getRemover();
        BlockState blockEvent = entity.getLocation().getBlock().getState();
        boolean inspecting = false;

        if (remover instanceof Player) {
            Player player = (Player) remover;
            if (Boolean.TRUE.equals(ConfigHandler.inspecting.get(player.getName()))) {
                HangingBreakByEntityListener.inspectEntity(blockEvent, player, null);
                event.setCancelled(true);
                inspecting = true;
            }
        }

        if (event.isCancelled() || inspecting) {
            return;
        }

        if (!Config.getConfig(entity.getWorld()).BLOCK_BREAK) {
            return;
        }

        String culprit = "#entity";
        if (remover != null && remover.getType() != null) {
            if (remover instanceof Player) {
                culprit = ((Player) remover).getName();
            }
            else {
                culprit = "#" + remover.getType().name().toLowerCase(Locale.ROOT);
            }
        }

        Material material = EntityUtils.getCushionMaterial(entity);
        if (material == null) {
            return;
        }

        Queue.queueBlockBreak(culprit, blockEvent, material, null, (int) entity.getLocation().getYaw());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onCushionBreak(EntityBreakEvent event) {
        if (event instanceof EntityBreakByEntityEvent) {
            return; // handled by onCushionBreakByEntity
        }

        Entity entity = event.getEntity();
        if (!EntityUtils.isCushion(entity) || event.isCancelled()) {
            return;
        }

        Block block = entity.getLocation().getBlock();
        EntityBreakEvent.RemoveCause cause = event.getCause();
        String causeName = "#explosion";
        Block attachedBlock = null;

        if (cause == EntityBreakEvent.RemoveCause.PHYSICS) {
            causeName = "#physics";
        }
        else if (cause == EntityBreakEvent.RemoveCause.OBSTRUCTION) {
            causeName = "#obstruction";
        }
        else if (cause == EntityBreakEvent.RemoveCause.DEFAULT) {
            causeName = "#physics";
        }

        if (cause != EntityBreakEvent.RemoveCause.EXPLOSION) {
            // Attribute the removal to the player that modified the support block, if known
            attachedBlock = cause == EntityBreakEvent.RemoveCause.OBSTRUCTION ? block : block.getRelative(BlockFace.DOWN);
            String removed = Lookup.whoRemovedCache(attachedBlock.getState());
            if (removed.length() > 0) {
                causeName = removed;
            }
        }

        if (!Config.getConfig(entity.getWorld()).NATURAL_BREAK) {
            return;
        }

        Material material = EntityUtils.getCushionMaterial(entity);
        if (material == null) {
            return;
        }

        Queue.queueNaturalBlockBreak(causeName, block.getState(), attachedBlock, material, null, (int) entity.getLocation().getYaw());
    }
}
