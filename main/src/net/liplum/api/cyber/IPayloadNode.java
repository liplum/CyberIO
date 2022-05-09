package net.liplum.api.cyber;

import arc.struct.Seq;

public interface IPayloadNode extends ICyberEntity {
    Seq<Integer> getConnections();
}
