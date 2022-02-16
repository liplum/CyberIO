package net.liplum.blocks.rs;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.OrderedSet;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.ui.Cicon;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;
import net.liplum.R;
import net.liplum.animations.anims.IAnimated;
import net.liplum.animations.anis.AniConfig;
import net.liplum.animations.anis.AniState;
import net.liplum.api.data.IDataReceiver;
import net.liplum.api.data.IDataSender;
import net.liplum.blocks.AniedBlock;
import net.liplum.utils.*;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;

public class Receiver extends AniedBlock<Receiver, Receiver.ReceiverBuild> {
    private final int DownloadAnimFrameNumber = 7;
    public TextureRegion CoverTR;
    public TextureRegion DownArrowTR;
    public TextureRegion UnconnectedTR;
    public TextureRegion NoPowerTR;
    private AniState<Receiver, ReceiverBuild> DownloadAni;
    private AniState<Receiver, ReceiverBuild> UnconnectedAni;
    private AniState<Receiver, ReceiverBuild> BlockedAni;
    private AniState<Receiver, ReceiverBuild> NoPowerAni;
    private IAnimated DownloadAnim;
    public int maxConnection = -1;

    public Receiver(String name) {
        super(name);
        hasItems = true;
        update = true;
        solid = true;
        itemCapacity = 20;
        group = BlockGroup.transportation;
        configurable = true;
        saveConfig = true;
        noUpdateDisabled = true;
        acceptsItems = false;
        canOverdrive = false;

        config(Item.class, ReceiverBuild::setOutputItem);
        configClear((ReceiverBuild tile) -> tile.setOutputItem(null));

    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (!control.input.frag.config.isShown()) return;
        Building selected = control.input.frag.config.getSelectedTile();
        if (selected == null ||
                !(selected.block instanceof Sender)) {
            return;
        }
        G.init();
        Tile selectedTile = selected.tile();
        G.drawDashLineBetweenTwoBlocks(
                selected.block, selectedTile.x, selectedTile.y,
                this, x, y,
                R.C.Sender
        );
        G.drawArrowBetweenTwoBlocks(
                selected.block, selectedTile.x, selectedTile.y, this, x, y,
                R.C.Sender
        );
    }

    public void genAnimState() {
        DownloadAni = addAniState("Download", (sender, build) -> {
            if (build.getOutputItem() != null) {
                DownloadAnim.draw(Color.green, build.x, build.y);
            }
        });
        UnconnectedAni = addAniState("Unconnected", ((sender, build) -> {
            Draw.color(Color.white);
            Draw.rect(sender.UnconnectedTR,
                    build.x, build.y
            );
            Draw.color();
        }));
        BlockedAni = addAniState("Blocked", ((sender, build) -> {
            Draw.color(Color.red);
            Draw.rect(sender.DownArrowTR,
                    build.x, build.y
            );
            Draw.color();
        }));
        NoPowerAni = addAniState("NoPower", (sender, build) -> {
            Draw.rect(sender.NoPowerTR,
                    build.x, build.y);
        });
    }

    public void genAniConfig() {
        aniConfig = new AniConfig<>();
        aniConfig.defaultState(UnconnectedAni);
        // UnconnectedAni
        aniConfig.enter(UnconnectedAni, DownloadAni, (block, build) ->
                build.getOutputItem() != null
        ).enter(UnconnectedAni, NoPowerAni, (block, build) ->
                Mathf.zero(build.power.status)
        );

        // BlockedAni
        aniConfig.enter(BlockedAni, UnconnectedAni, ((block, build) ->
                build.getOutputItem() == null)
        ).enter(BlockedAni, DownloadAni, ((block, build) ->
                build.isOutputting() || build.lastFullDataDelta < 60
        )).enter(BlockedAni, NoPowerAni, (block, build) ->
                Mathf.zero(build.power.status)
        );

        // DownloadAni
        aniConfig.enter(DownloadAni, UnconnectedAni, (block, build) ->
                build.getOutputItem() == null
        ).enter(DownloadAni, BlockedAni, (block, build) ->
                !build.isOutputting() && build.lastFullDataDelta > 60
        ).enter(DownloadAni, NoPowerAni, (block, build) ->
                Mathf.zero(build.power.status)
        );
        // NoPower
        aniConfig.enter(NoPowerAni, UnconnectedAni, (block, build) ->
                !Mathf.zero(build.power.status) && build.getOutputItem() == null
        ).enter(NoPowerAni, DownloadAni, (block, build) ->
                !Mathf.zero(build.power.status) && build.getOutputItem() != null
        );
        aniConfig.build();
    }

    @Override
    public void load() {
        super.load();
        CoverTR = AtlasUtil.cio("rs-cover");
        DownArrowTR = AtlasUtil.cio("rs-down-arrow");
        UnconnectedTR = AtlasUtil.cio("rs-unconnected");
        NoPowerTR = AtlasUtil.cio("rs-no-power");
        loadAnimation();
    }

    public void loadAnimation() {
        DownloadAnim = AnimUtil.autoCio("rs-down-arrow", DownloadAnimFrameNumber, 30f);
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list) {
        drawRequestConfigCenter(req, req.config, "center", true);
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    public class ReceiverBuild extends AniedBuild implements IDataReceiver {
        private Item outputItem;
        private boolean isOutputting = false;
        private float lastOutputDelta = 0;
        private float lastFullDataDelta = 0;
        OrderedSet<Integer> sendersPos = new OrderedSet<>();

        @Nullable
        public Item getOutputItem() {
            return outputItem;
        }

        public void setOutputItem(@Nullable Item item) {
            this.outputItem = item;
        }

        @Override
        public boolean isOutputting() {
            return lastOutputDelta < 30f;
        }

        @Override
        public void connect(@NotNull IDataSender sender) {
            sendersPos.add(sender.getBuilding().pos());
        }

        @Override
        public void disconnect(@NotNull IDataSender sender) {
            sendersPos.remove(sender.getBuilding().pos());
        }

        public void checkSenderPos() {
            OrderedSet<Integer>.OrderedSetIterator it = sendersPos.iterator();
            while (it.hasNext) {
                Integer curSenderPos = it.next();
                Building sBuild = world.build(curSenderPos);
                if (!(sBuild instanceof IDataSender)) {
                    it.remove();
                }
            }
        }

        @Override
        public void drawSelect() {
            Item outputItem = getOutputItem();
            G.init();
            G.drawSurroundingCircle(tile, R.C.Receiver);
            if (outputItem != null) {
                float dx = x - size * tilesize / 2f, dy = y + size * tilesize / 2f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(outputItem.icon(Cicon.small), dx, dy - 1);
                Draw.reset();
                Draw.rect(outputItem.icon(Cicon.small), dx, dy);
            }
            CyberUtil.drawSenders(this, sendersPos);
        }

        @Override
        public void fixedUpdateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkSenderPos();
            }
            Item outputItem = getOutputItem();
            float deltaT = Time.delta;
            if (outputItem != null) {
                boolean isFullData = items.get(outputItem) < getMaximumAccepted(outputItem);
                if (isFullData) {
                    lastFullDataDelta = 0;
                } else {
                    lastFullDataDelta += deltaT;
                }
            }
            if (!Mathf.zero(power.status) && outputItem != null) {
                if (dump(outputItem)) {
                    lastOutputDelta = 0;
                } else {
                    lastOutputDelta += deltaT;
                }
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(table, content.items(), this::getOutputItem, this::configure);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other) {
                deselect();
                configure(null);
                return false;
            }
            return true;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        public boolean acceptData(@NotNull IDataSender source, Item item) {
            return items.get(item) < getMaximumAccepted(item) &&
                    getOutputItem() == item;
        }

        @Override
        public boolean canAcceptAnyData(@NotNull IDataSender sender) {
            Item outputItem = getOutputItem();
            if (outputItem == null) {
                return false;
            }
            return items.get(outputItem) < getMaximumAccepted(outputItem);
        }

        @Override
        public void receiveData(@NotNull IDataSender sender, Item item, int amount) {
            this.items.add(item, 1);
        }

        @Override
        public Item config() {
            return getOutputItem();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(outputItem == null ? -1 : outputItem.id);
            write.bool(isOutputting);
            RWUtil.writeIntSet(write, sendersPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            outputItem = content.item(read.s());
            isOutputting = read.bool();
            sendersPos = RWUtil.readIntSet(read);
        }

        @Override
        public @NotNull ObjectSet<Integer> connectedSenders() {
            return sendersPos;
        }

        @Override
        public Integer connectedSender() {
            return sendersPos.first();
        }

        @Override
        public boolean acceptConnection(@NotNull IDataSender sender) {
            if (maxConnection == -1) {
                return true;
            } else {
                return sendersPos.size < maxConnection;
            }
        }

        @NotNull
        @Override
        public Building getBuilding() {
            return this;
        }

        @NotNull
        @Override
        public Tile getTile() {
            return tile();
        }

        @NotNull
        @Override
        public Block getBlock() {
            return block();
        }
    }
}
