package net.liplum.api.data;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Tile;
import net.liplum.R;
import net.liplum.api.data.IDataReceiver;
import net.liplum.api.data.IDataSender;
import net.liplum.utils.G;

public class CyberU {
    public static void drawSender(IDataReceiver receiver, Integer sender) {
        Tile receiverT = receiver.getTile();
        Building senderBuild = Vars.world.build(sender);
        if (senderBuild instanceof IDataSender) {
            Tile senderT = senderBuild.tile();
            G.drawSurroundingCircle(senderT, R.C.Sender);
            G.drawDashLineBetweenTwoBlocks(receiverT, senderT, R.C.Receiver);
            G.drawArrowBetweenTwoBlocks(senderT, receiverT, R.C.Sender);
        }
    }

    public static void drawSenders(IDataReceiver receiver, Iterable<Integer> senders) {
        Tile receiverT = receiver.getTile();
        for (Integer sender : senders) {
            Building senderBuild = Vars.world.build(sender);
            if (senderBuild instanceof IDataSender) {
                Tile senderT = senderBuild.tile();
                G.drawSurroundingCircle(senderT, R.C.Sender);
                G.drawDashLineBetweenTwoBlocks(receiverT, senderT, R.C.Receiver);
                G.drawArrowBetweenTwoBlocks(senderT, receiverT, R.C.Sender);
            }
        }
    }

    public static void drawReceiver(IDataSender sender, Integer receiver) {
        Tile senderT = sender.getTile();
        Building receiverBuild = Vars.world.build(receiver);
        if (receiverBuild instanceof IDataReceiver) {
            Tile receiverT = receiverBuild.tile();
            G.drawSurroundingCircle(receiverT, R.C.Receiver);
            G.drawDashLineBetweenTwoBlocks(senderT, receiverT, R.C.Sender);
            G.drawArrowBetweenTwoBlocks(senderT, receiverT, R.C.Sender);
        }
    }

    public static void drawReceivers(IDataSender sender, Iterable<Integer> receivers) {
        Tile senderT = sender.getTile();
        for (Integer receiver : receivers) {
            Building receiverBuild = Vars.world.build(receiver);
            if (receiverBuild instanceof IDataReceiver) {
                Tile receiverT = receiverBuild.tile();
                G.drawSurroundingCircle(receiverT, R.C.Receiver);
                G.drawDashLineBetweenTwoBlocks(senderT, receiverT, R.C.Sender);
                G.drawArrowBetweenTwoBlocks(senderT, receiverT, R.C.Sender);
            }
        }
    }
}
