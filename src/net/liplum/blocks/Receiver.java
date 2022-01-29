package net.liplum.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
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
import net.liplum.animations.AniConfig;
import net.liplum.animations.AniState;
import net.liplum.animations.AutoAnimation;
import net.liplum.animations.IAnimated;
import net.liplum.api.IDataReceiver;
import net.liplum.api.IDataSender;
import net.liplum.utils.AtlasUtil;
import net.liplum.utils.GraphicUtl;

import static mindustry.Vars.*;

public class Receiver extends AniBlock<Receiver, Receiver.ReceiverBuild> {
    private final int DownloadAnimFrameNumber = 7;
    public TextureRegion coverTR;
    public TextureRegion downArrowTR;
    public TextureRegion unconnectedTR;
    private AniState<Receiver, ReceiverBuild> downloadAni;
    private AniState<Receiver, ReceiverBuild> unconnectedAni;
    private AniState<Receiver, ReceiverBuild> blockedAni;
    private IAnimated DownloadAnim;

    public Receiver(String name) {
        super(name);
        hasItems = true;
        update = true;
        solid = true;
        itemCapacity = 10;
        group = BlockGroup.transportation;
        configurable = true;
        saveConfig = true;
        noUpdateDisabled = true;
        acceptsItems = false;

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
        Tile selectedTile = selected.tile();
        GraphicUtl.drawDashLineBetweenTwoBlocks(selected.block, selectedTile.x, selectedTile.y
                , this, x, y);
    }

    public void genAnimState() {
        downloadAni = addAniState(new AniState<>("Download", (sender, build) -> {
            if (build.getOutputItem() != null) {
                DownloadAnim.draw(Color.green, build.x, build.y);
            }
        }));
        unconnectedAni = addAniState(new AniState<>("Unconnected", ((sender, build) -> {
            Draw.color(Color.white);
            Draw.rect(sender.unconnectedTR,
                    build.x, build.y
            );
            Draw.color();
        })));
        blockedAni = addAniState(new AniState<>("Blocked", ((sender, build) -> {
            Draw.color(Color.red);
            Draw.rect(sender.downArrowTR,
                    build.x, build.y
            );
            Draw.color();
        })));
    }

    public void genAniConfig() {
        aniConfig = new AniConfig<>();
        aniConfig.defaultState(unconnectedAni);
        aniConfig.enter(unconnectedAni, downloadAni, (block, build) ->
                build.getOutputItem() != null
        );

        aniConfig.enter(blockedAni, unconnectedAni, ((block, build) ->
                build.getOutputItem() == null)
        ).enter(blockedAni, downloadAni, ((block, build) ->
                build.isOutputting() || build.lastFullDataDelta < 60
        ));

        aniConfig.enter(downloadAni, unconnectedAni, (block, build) ->
                build.getOutputItem() == null
        ).enter(downloadAni, blockedAni, (block, build) ->
                !build.isOutputting() && build.lastFullDataDelta > 60
        );

        aniConfig.build();
    }

    @Override
    public void load() {
        super.load();
        coverTR = AtlasUtil.sub(this, "cover");
        downArrowTR = AtlasUtil.sub(this, "down-arrow");
        unconnectedTR = AtlasUtil.sub(this, "unconnected");
        loadAnimation();
    }

    public void loadAnimation() {
        DownloadAnim = new AutoAnimation(30f, AtlasUtil.animation(this, "down-arrow", true, DownloadAnimFrameNumber));
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list) {
        drawRequestConfigCenter(req, req.config, "center", true);
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    public class ReceiverBuild extends AniBuild implements IDataReceiver {
        private Item outputItem;
        private boolean isOutputting = false;
        private float lastOutputDelta = 0;
        private float lastFullDataDelta = 0;

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
        public void drawSelect() {
            Item outputItem = getOutputItem();
            if (outputItem != null) {
                float dx = x - size * tilesize / 2f, dy = y + size * tilesize / 2f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(outputItem.icon(Cicon.small), dx, dy - 1);
                Draw.reset();
                Draw.rect(outputItem.icon(Cicon.small), dx, dy);
            }
        }

        @Override
        public void fixedUpdateTile() {
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

        public boolean acceptData(IDataSender source, Item item) {
            return items.get(item) < getMaximumAccepted(item) &&
                    getOutputItem() == item;
        }

        @Override
        public boolean canAcceptAnyData(IDataSender sender) {
            Item outputItem = getOutputItem();
            if (outputItem == null) {
                return false;
            }
            return items.get(outputItem) < getMaximumAccepted(outputItem);
        }

        @Override
        public void receiveData(IDataSender sender, Item item) {
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
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            outputItem = content.item(read.s());
            isOutputting = read.bool();
        }

        @Override
        public Building getBuilding() {
            return this;
        }

        @Override
        public Tile getTile() {
            return tile();
        }

        @Override
        public Block getBlock() {
            return block();
        }
    }
}
