package net.liplum.registries;

import arc.graphics.gl.Shader;
import arc.util.Nullable;
import net.liplum.ClientOnly;
import net.liplum.shaders.BlockShader;

import java.util.LinkedList;

import static net.liplum.CioMod.IsClient;

public class ShaderRegistry {
    @Nullable
    @ClientOnly
    private static LinkedList<Shader> allShaders;

    @Nullable
    @ClientOnly
    public static Shader test;

    private static boolean isLoaded = false;

    public static void load() {
        if (IsClient) {
            allShaders = new LinkedList<>();
            test = with(new BlockShader("test"));
            isLoaded = true;
        }
    }

    @ClientOnly
    public static void dispose() {
        if (IsClient && isLoaded) {
            for (Shader shader : allShaders) {
                shader.dispose();
            }
        }
    }

    @ClientOnly
    public static Shader with(Shader shader) {
        allShaders.add(shader);
        return shader;
    }
}
