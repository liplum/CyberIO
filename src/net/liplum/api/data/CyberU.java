package net.liplum.api.data;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.liplum.R;
import net.liplum.utils.G;

public class CyberU {
    public static void drawSender(IDataReceiver receiver, Integer sender) {
        if (sender == null) {
            return;
        }
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
        if (receiver == null) {
            return;
        }
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

    public static void drawLinkedLineToReceiverWhenConfiguring(Block curBlock, int x, int y) {
        if (!Vars.control.input.frag.config.isShown())
            return;
        Building selected = Vars.control.input.frag.config.getSelectedTile();
        if (!(selected instanceof IDataSender)
        ) {
            return;
        }
        G.init();
        Tile selectedTile = selected.tile();
        G.drawDashLineBetweenTwoBlocks(
                selected.block, selectedTile.x, selectedTile.y,
                curBlock, (short) x, (short) y,
                R.C.Receiver
        );
        G.drawArrowBetweenTwoBlocks(
                selected.block, selectedTile.x, selectedTile.y,
                curBlock, (short) x, (short) y,
                R.C.Sender
        );
        G.drawSurroundingCircle(curBlock, x, y, R.C.Receiver);
    }
}
