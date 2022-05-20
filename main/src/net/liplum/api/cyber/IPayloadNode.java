package net.liplum.api.cyber;

import arc.struct.Seq;
import net.liplum.api.ICyberEntity;

public interface IPayloadNode extends ICyberEntity {
    Seq<Integer> getConnections();
}
