package net.liplum.api.cyber;

import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.api.ICyberEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CyberEntityAdapterRegistry {
    private final static Map<Block, ICyberEntityProv> registry = new HashMap<>();

    public static void set(Block block, ICyberEntityProv mapping) {
        registry.put(block, mapping);
    }

    public static boolean has(Block block) {
        return registry.containsKey(block);
    }

    @Nullable
    public static ICyberEntityProv get(Block block) {
        return registry.get(block);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends ICyberEntity> T getCyberEntity(Building b,Class<T> clz) {
        if (b instanceof ICyberEntity) {
            return ((T) b);
        } else {
            ICyberEntityProv prov = get(b.block);
            if (prov != null) {
                ICyberEntity entity = prov.get(b);
                if(clz.isInstance(entity))
                    return (T) entity;
                else
                    return null;
            }
        }
        return null;
    }
}
