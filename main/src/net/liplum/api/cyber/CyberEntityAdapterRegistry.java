package net.liplum.api.cyber;

import mindustry.world.Block;
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
}
