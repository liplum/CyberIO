package net.liplum.api.cyber;

import mindustry.gen.Building;
import net.liplum.api.ICyberEntity;

public interface ICyberEntityProv {
    ICyberEntity get(Building b);
}
