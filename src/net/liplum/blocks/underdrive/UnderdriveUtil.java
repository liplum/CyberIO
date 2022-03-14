package net.liplum.blocks.underdrive;

import mindustry.gen.Building;

public class UnderdriveUtil {
    public static void applyBoostOrSlow(Building build, float laxityOrIntensity, float duration, OverwriteRule rule) {
        float oldTS = build.timeScale;
        float oldTSD = build.timeScaleDuration;
        switch (rule) {
            case Coerce:
                build.timeScale = laxityOrIntensity;
                break;
            case KeepMin:
                build.timeScale = Math.min(oldTS, laxityOrIntensity);
                break;
            case KeepMax:
                build.timeScale = Math.max(oldTS, laxityOrIntensity);
                break;
        }
        build.timeScaleDuration = duration;
    }

    public static void resetBoost(Building build) {
        build.timeScale = 1f;
        build.timeScaleDuration = 0;
    }

    public enum OverwriteRule {
        KeepMin, KeepMax, Coerce
    }
}
