package net.coreprotect.bukkit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import net.coreprotect.model.BlockGroup;

/**
 * Bukkit adapter implementation for Minecraft 26.3.
 *
 * Handles blocks introduced in the 26.3 "Wilderness Bound" drop:
 * - Straw beds (single-use beds, attached like other beds)
 * - Shelf mushrooms (attach to the sides of poplar logs)
 * - Red shrubs and the dappled forest ground cover (bush, firefly bush,
 *   dry grasses, leaf litter), which are only part of the #replaceable
 *   vanilla tag and are therefore not covered by the tag-based groups
 * - Cushions (placed as entities; mapped back to their entity type here)
 */
public class Bukkit_v26_3 extends Bukkit_v26_2 {

    public Bukkit_v26_3() {
        initializeBlockGroups();
    }

    private void initializeBlockGroups() {
        /* Beds break apart when the block they occupy is removed */
        BlockGroup.TRACK_SIDE.add(Material.STRAW_BED);

        /* Ground cover that pops off when the block below it is broken */
        BlockGroup.TRACK_TOP.add(Material.RED_SHRUB);
        BlockGroup.TRACK_TOP.add(Material.BUSH);
        BlockGroup.TRACK_TOP.add(Material.FIREFLY_BUSH);
        BlockGroup.TRACK_TOP.add(Material.SHORT_DRY_GRASS);
        BlockGroup.TRACK_TOP.add(Material.TALL_DRY_GRASS);
        BlockGroup.TRACK_TOP.add(Material.LEAF_LITTER);

        /* Shelf mushrooms grow on the sides and tops of poplar logs */
        BlockGroup.TRACK_TOP.add(Material.SHELF_MUSHROOM);
        BlockGroup.TRACK_SIDE.add(Material.SHELF_MUSHROOM);

        /* Item frames and paintings cannot be attached to these plants */
        BlockGroup.NON_ATTACHABLE.add(Material.RED_SHRUB);
        BlockGroup.NON_ATTACHABLE.add(Material.BUSH);
        BlockGroup.NON_ATTACHABLE.add(Material.FIREFLY_BUSH);
        BlockGroup.NON_ATTACHABLE.add(Material.SHORT_DRY_GRASS);
        BlockGroup.NON_ATTACHABLE.add(Material.TALL_DRY_GRASS);
        BlockGroup.NON_ATTACHABLE.add(Material.LEAF_LITTER);
        BlockGroup.NON_ATTACHABLE.add(Material.SHELF_MUSHROOM);

        /* Ground cover and mushrooms generate naturally in the dappled forest */
        BlockGroup.NATURAL_BLOCKS.add(Material.RED_SHRUB);
        BlockGroup.NATURAL_BLOCKS.add(Material.BUSH);
        BlockGroup.NATURAL_BLOCKS.add(Material.FIREFLY_BUSH);
        BlockGroup.NATURAL_BLOCKS.add(Material.SHORT_DRY_GRASS);
        BlockGroup.NATURAL_BLOCKS.add(Material.TALL_DRY_GRASS);
        BlockGroup.NATURAL_BLOCKS.add(Material.LEAF_LITTER);
        BlockGroup.NATURAL_BLOCKS.add(Material.SHELF_MUSHROOM);
    }

    @Override
    public EntityType getEntityType(Material material) {
        if (material != null && material.name().endsWith("_CUSHION")) {
            return EntityType.CUSHION;
        }

        return super.getEntityType(material);
    }

}
