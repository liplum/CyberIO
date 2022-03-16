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
        Building senderBuild = Vars.world.build(sender);
        if (senderBuild instanceof IDataSender) {
            Tile senderT = senderBuild.tile();
            G.drawSurroundingCircle(senderT, R.C.Sender);
            G.drawArrowLine(senderBuild, receiver.getBuilding(), 15f, R.C.Receiver);
        }
    }

    public static void drawSenders(IDataReceiver receiver, Iterable<Integer> senders) {
        for (Integer sender : senders) {
            Building senderBuild = Vars.world.build(sender);
            if (senderBuild instanceof IDataSender) {
                Tile senderT = senderBuild.tile();
                G.drawSurroundingCircle(senderT, R.C.Sender);
                G.drawArrowLine(senderBuild, receiver.getBuilding(), 15f, R.C.Receiver);
            }
        }
    }

    public static void drawReceiver(IDataSender sender, Integer receiver) {
        if (receiver == null) {
            return;
        }
        Building receiverBuild = Vars.world.build(receiver);
        if (receiverBuild instanceof IDataReceiver) {
            Tile receiverT = receiverBuild.tile();
            G.drawSurroundingCircle(receiverT, R.C.Receiver);
            G.drawArrowLine(sender.getBuilding(), receiverBuild, 15f, R.C.Sender);
        }
    }

    public static void drawReceivers(IDataSender sender, Iterable<Integer> receivers) {
        for (Integer receiver : receivers) {
            Building receiverBuild = Vars.world.build(receiver);
            if (receiverBuild instanceof IDataReceiver) {
                Tile receiverT = receiverBuild.tile();
                G.drawSurroundingCircle(receiverT, R.C.Receiver);
                G.drawArrowLine(sender.getBuilding(), receiverBuild, 15f, R.C.Sender);
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
        Tile selectedTile = selected.tile();
        G.drawSurroundingCircle(curBlock, x, y, R.C.Receiver);
        G.drawArrowLine(
                selected.block,
                selectedTile.x, selectedTile.y,
                curBlock,
                (short) x, (short) y,
                15f, R.C.Receiver);
    }

    public static boolean isConfiguringSender() {
        Building selected = Vars.control.input.frag.config.getSelectedTile();
        return selected instanceof IDataSender;
    }
}
