package net.coreprotect.utility;

import java.util.Locale;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Colorable;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import org.bukkit.entity.ThrowableProjectile;

public class EntityUtils extends Queue {

    private static final String NAMESPACE = "minecraft:";

    private EntityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static int getEntityId(EntityType type) {
        if (type == null) {
            return -1;
        }

        return getEntityId(type.name(), true);
    }

    public static int getEntityId(String name, boolean internal) {
        int id = -1;
        name = name.toLowerCase(Locale.ROOT).trim();

        if (ConfigHandler.databaseType.isClickHouse()) {
            return ConfigHandler.resolveIdentifierId(ConfigHandler.CacheType.ENTITIES, name, internal);
        }
        if (ConfigHandler.entities.get(name) != null) {
            id = ConfigHandler.entities.get(name);
        }
        else if (internal) {
            // Check if another server has already added this entity (multi-server setup)
            id = ConfigHandler.reloadAndGetId(ConfigHandler.CacheType.ENTITIES, name);
            if (id != -1) {
                return id;
            }

            int entityID = ConfigHandler.entityId + 1;
            ConfigHandler.entities.put(name, entityID);
            ConfigHandler.entitiesReversed.put(entityID, name);
            ConfigHandler.entityId = entityID;
            Queue.queueEntityInsert(entityID, name);
            id = ConfigHandler.entities.get(name);
        }

        return id;
    }

    public static Material getEntityMaterial(final Entity entity) {
        if (entity instanceof ThrowableProjectile) {
            return ((ThrowableProjectile) entity).getItem().getType();
        }

        return getEntityMaterial(entity.getType());
    }

    /**
     * Returns true if the entity is a cushion (Minecraft 26.3+).
     * Uses name-based checks so this is safe to call on any server version.
     */
    public static boolean isCushion(final Entity entity) {
        return entity != null && entity.getType().name().equals("CUSHION");
    }

    /**
     * Returns true if the material is a cushion item (e.g. WHITE_CUSHION, Minecraft 26.3+).
     */
    public static boolean isCushion(final Material material) {
        return material != null && material.name().endsWith("_CUSHION");
    }

    /**
     * Gets the cushion material matching the entity's color (e.g. WHITE_CUSHION).
     * Cushions exist as entities, but are logged against their item materials.
     */
    public static Material getCushionMaterial(final Entity entity) {
        if (!isCushion(entity) || !(entity instanceof Colorable)) {
            return null;
        }

        DyeColor color = ((Colorable) entity).getColor();
        if (color == null) {
            return Material.getMaterial("WHITE_CUSHION");
        }

        return Material.getMaterial(color.name() + "_CUSHION");
    }

    /**
     * Gets the color of a cushion material (e.g. WHITE_CUSHION -> WHITE).
     */
    public static DyeColor getCushionColor(final Material material) {
        if (!isCushion(material)) {
            return null;
        }

        String name = material.name();
        String colorName = name.substring(0, name.length() - "_CUSHION".length());
        try {
            return DyeColor.valueOf(colorName);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Material getEntityMaterial(EntityType type) {
        switch (type.name()) {
            case "ARMOR_STAND":
                return Material.ARMOR_STAND;
            case "ITEM_FRAME":
                return Material.ITEM_FRAME;
            case "END_CRYSTAL":
            case "ENDER_CRYSTAL":
                return Material.END_CRYSTAL;
            case "ENDER_PEARL":
                return Material.ENDER_PEARL;
            case "POTION":
            case "SPLASH_POTION":
                return Material.SPLASH_POTION;
            case "EXPERIENCE_BOTTLE":
            case "THROWN_EXP_BOTTLE":
                return Material.EXPERIENCE_BOTTLE;
            case "TRIDENT":
                return Material.TRIDENT;
            case "FIREWORK_ROCKET":
            case "FIREWORK":
                return Material.FIREWORK_ROCKET;
            case "EGG":
                return Material.EGG;
            case "SNOWBALL":
                return Material.SNOWBALL;
            case "WIND_CHARGE":
                return Material.valueOf("WIND_CHARGE");
            default:
                return BukkitAdapter.ADAPTER.getFrameType(type);
        }
    }

    public static String getEntityName(int id) {
        // Internal ID pulled from DB
        if (ConfigHandler.databaseType.isClickHouse()) {
            String entityName = ConfigHandler.getIdentifierValue(ConfigHandler.CacheType.ENTITIES, id);
            return entityName == null ? "" : entityName;
        }
        String entityName = "";
        String cachedName = ConfigHandler.entitiesReversed.get(id);
        if (cachedName != null) {
            entityName = cachedName;
        }
        return entityName;
    }

    public static EntityType getEntityType(int id) {
        // Internal ID pulled from DB
        EntityType entitytype = EntityType.UNKNOWN;
        String name = getEntityName(id);
        if (!name.isEmpty()) {
            if (name.contains(NAMESPACE)) {
                name = name.split(":")[1];
            }
            entitytype = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
        }
        return entitytype;
    }

    public static EntityType getEntityType(String name) {
        // Name entered by user
        EntityType type = null;
        name = name.toLowerCase(Locale.ROOT).trim();
        if (name.contains(NAMESPACE)) {
            name = (name.split(":"))[1];
        }

        if (getEntityId(name, false) != -1) {
            type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
        }

        return type;
    }

    public static int getSpawnerType(EntityType type) {
        int result = getEntityId(type);
        if (result == -1) {
            result = 0; // default to pig
        }

        return result;
    }

    public static EntityType getSpawnerType(int type) {
        EntityType result = getEntityType(type);
        if (result == null) {
            result = EntityType.PIG;
        }

        return result;
    }
}
