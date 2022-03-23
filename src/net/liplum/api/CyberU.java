package net.liplum.api;

import arc.graphics.g2d.Draw;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.liplum.R;
import net.liplum.api.cyber.IDataReceiver;
import net.liplum.api.cyber.IDataSender;
import net.liplum.api.cyber.IStreamClient;
import net.liplum.api.cyber.IStreamHost;
import net.liplum.utils.G;

public class CyberU {
    public static float ArrowDensity = 15f;
    public static void drawRequirements(IDataReceiver r) {
        Item[] reqs = r.getRequirements();
        if (reqs != null) {
            G.drawMaterialIcons(r.getBuilding(), reqs);
        }
    }

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
        Building receiverB = Vars.world.build(receiver);
        if (receiverB instanceof IDataReceiver) {
            Tile receiverT = receiverB.tile();
            IDataReceiver rb = (IDataReceiver) receiverB;
            G.drawSurroundingCircle(receiverT, R.C.Receiver);
            G.drawArrowLine(sender.getBuilding(), receiverB, ArrowDensity, R.C.Sender);
            drawRequirements(rb);
        }
    }

    /**
     * Called in Sender block
     */
    public static void drawReceivers(IDataSender sender, Iterable<Integer> receivers) {
        float original = Draw.z();
        for (Integer receiver : receivers) {
            Building receiverB = Vars.world.build(receiver);
            if (receiverB instanceof IDataReceiver) {
                Tile receiverT = receiverB.tile();
                IDataReceiver rb = (IDataReceiver) receiverB;
                Draw.z(original);
                G.drawSurroundingCircle(receiverT, R.C.Receiver);
                G.drawArrowLine(sender.getBuilding(), receiverB, ArrowDensity, R.C.Sender);
                drawRequirements(rb);
            }
        }
        Draw.z(original);
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

    public static void drawRequirements(IStreamClient c) {
        Liquid[] reqs = c.getRequirements();
        if (reqs != null) {
            G.drawMaterialIcons(c.getBuilding(), reqs);
        }
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
                drawRequirements(sc);
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
