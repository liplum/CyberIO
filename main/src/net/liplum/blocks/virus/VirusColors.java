package net.liplum.blocks.virus;

import arc.graphics.Color;
import arc.math.Mathf;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VirusColors {
    public static Color[] Colors = new Color[]{
        null,
        Color.red,
        Color.black,
        Color.blue,
        Color.gold,
        Color.green,
        Color.orange,
        Color.white
    };

    @Nullable
    public static Color randomColor() {
        return Colors[Mathf.random(Colors.length - 1)];
    }

    @Nullable
    public static Color randomColor(Color notThis) {
        int len = Colors.length;
        if (len == 0) {
            return notThis;
        }
        int max = len - 1;
        if (len == 1) {
            return Colors[0];
        }
        Color color;
        do {
            color = Colors[Mathf.random(max)];
        } while (Objects.equals(color, notThis));
        return color;
    }
}
