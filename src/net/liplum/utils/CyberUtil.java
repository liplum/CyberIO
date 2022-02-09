package net.liplum.utils;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Tile;
import net.liplum.R;
import net.liplum.api.data.IDataReceiver;
import net.liplum.api.data.IDataSender;

public class CyberUtil {
    public static void drawSenders(IDataReceiver receiver, Iterable<Integer> sendersPos) {
        Tile rt = receiver.getTile();
        for (Integer senderPos : sendersPos) {
            Building sBuild = Vars.world.build(senderPos);
            if (sBuild instanceof IDataSender) {
                Tile st = sBuild.tile();
                G.drawSurroundingCircle(st, R.C.Sender);
                G.drawDashLineBetweenTwoBlocks(rt, st, R.C.Receiver);
                G.drawArrowBetweenTwoBlocks(st, rt, R.C.Sender);
            }
        }
    }
}
