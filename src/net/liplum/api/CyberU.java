package net.liplum.api;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.liplum.R;
import net.liplum.api.data.IDataReceiver;
import net.liplum.api.data.IDataSender;
import net.liplum.api.stream.IStreamClient;
import net.liplum.api.stream.IStreamHost;
import net.liplum.utils.G;

public class CyberU {
    public static float ArrowDensity = 15f;

    /**
     * Called in Receiver block
     */
    public static void drawSender(IDataReceiver receiver, Integer sender) {
        if (sender == null) {
            return;
        }
        Building senderBuild = Vars.world.build(sender);
        if (senderBuild instanceof IDataSender) {
            Tile senderT = senderBuild.tile();
            G.drawSurroundingCircle(senderT, R.C.Sender);
            G.drawArrowLine(senderBuild, receiver.getBuilding(), ArrowDensity, R.C.Receiver);
        }
    }

    /**
     * Called in Receiver block
     */
    public static void drawSenders(IDataReceiver receiver, Iterable<Integer> senders) {
        for (Integer sender : senders) {
            Building senderBuild = Vars.world.build(sender);
            if (senderBuild instanceof IDataSender) {
                Tile senderT = senderBuild.tile();
                G.drawSurroundingCircle(senderT, R.C.Sender);
                G.drawArrowLine(senderBuild, receiver.getBuilding(), ArrowDensity, R.C.Receiver);
            }
        }
    }

    /**
     * Called in Sender block
     */
    public static void drawReceiver(IDataSender sender, Integer receiver) {
        if (receiver == null) {
            return;
        }
        Building receiverBuild = Vars.world.build(receiver);
        if (receiverBuild instanceof IDataReceiver) {
            Tile receiverT = receiverBuild.tile();
            G.drawSurroundingCircle(receiverT, R.C.Receiver);
            G.drawArrowLine(sender.getBuilding(), receiverBuild, ArrowDensity, R.C.Sender);
        }
    }

    /**
     * Called in Sender block
     */
    public static void drawReceivers(IDataSender sender, Iterable<Integer> receivers) {
        for (Integer receiver : receivers) {
            Building receiverBuild = Vars.world.build(receiver);
            if (receiverBuild instanceof IDataReceiver) {
                Tile receiverT = receiverBuild.tile();
                G.drawSurroundingCircle(receiverT, R.C.Receiver);
                G.drawArrowLine(sender.getBuilding(), receiverBuild, ArrowDensity, R.C.Sender);
            }
        }
    }

    /**
     * Called in Receiver block
     *
     * @param curBlock whose building is an {@link IDataReceiver} object.
     * @param x        tile x
     * @param y        tile y
     */
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
                ArrowDensity, R.C.Receiver);
    }

    public static boolean isConfiguringSender() {
        Building selected = Vars.control.input.frag.config.getSelectedTile();
        return selected instanceof IDataSender;
    }

    /**
     * Called in Client block
     */
    public static void drawHosts(IStreamClient client, Iterable<Integer> hosts) {
        for (Integer host : hosts) {
            Building hostB = Vars.world.build(host);
            if (hostB instanceof IStreamHost) {
                Tile hostT = hostB.tile();
                IStreamHost sh = (IStreamHost) hostB;
                G.drawSurroundingCircle(hostT, sh.getHostColor());
                G.drawArrowLine(hostB, client.getBuilding(), ArrowDensity, client.getClientColor());
            }
        }
    }

    /**
     * Called in Host block
     */
    public static void drawClients(IStreamHost host, Iterable<Integer> clients) {
        for (Integer client : clients) {
            Building clientB = Vars.world.build(client);
            if (clientB instanceof IStreamClient) {
                Tile clientT = clientB.tile();
                IStreamClient sc = (IStreamClient) clientB;
                G.drawSurroundingCircle(clientT, sc.getClientColor());
                G.drawArrowLine(host.getBuilding(), clientB, ArrowDensity, host.getHostColor());
                Liquid[] reqs = sc.getRequirements();
                if (reqs != null && reqs.length == 1) {
                    G.drawMaterialIcon(sc.getBuilding(), reqs[0]);
                }
            }
        }
    }

    public static boolean isConfiguringHost() {
        Building selected = Vars.control.input.frag.config.getSelectedTile();
        return selected instanceof IStreamHost;
    }

    /**
     * Called in Client block
     *
     * @param curBlock whose building is an {@link IStreamClient} object.
     * @param x        tile x
     * @param y        tile y
     */
    public static void drawLinkedLineToClientWhenConfiguring(Block curBlock, int x, int y) {
        if (!Vars.control.input.frag.config.isShown())
            return;
        Building selected = Vars.control.input.frag.config.getSelectedTile();
        if (!(selected instanceof IStreamHost)
        ) {
            return;
        }
        IStreamHost host = (IStreamHost) selected;
        Tile selectedTile = selected.tile();
        G.drawSurroundingCircle(curBlock, x, y, R.C.Client);
        G.drawArrowLine(
                selected.block,
                selectedTile.x, selectedTile.y,
                curBlock,
                (short) x, (short) y,
                ArrowDensity, host.getHostColor());
    }

}
