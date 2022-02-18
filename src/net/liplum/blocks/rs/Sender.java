package net.liplum.blocks.rs;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import net.liplum.R;
import net.liplum.animations.anims.IAnimated;
import net.liplum.animations.anis.AniConfig;
import net.liplum.animations.anis.AniState;
import net.liplum.api.data.IDataReceiver;
import net.liplum.api.data.IDataSender;
import net.liplum.blocks.AniedBlock;
import net.liplum.utils.AnimUtil;
import net.liplum.utils.AtlasUtil;
import net.liplum.utils.G;
import org.jetbrains.annotations.NotNull;

public class Sender extends AniedBlock<Sender, Sender.SenderBuild> {
    private final int UploadAnimFrameNumber = 7;
    public TextureRegion CoverTR;
    public TextureRegion UpArrowTR;
    public TextureRegion CrossTR;
    public TextureRegion NoPowerTR;
    public TextureRegion UnconnectedTR;
    private AniState<Sender, SenderBuild> IdleAni;
    private AniState<Sender, SenderBuild> UploadAni;
    private AniState<Sender, SenderBuild> BlockedAni;
    private AniState<Sender, SenderBuild> NoPowerAni;
    private IAnimated UploadAnim;

    public Sender(String name) {
        super(name);
        solid = true;
        update = true;
        acceptsItems = true;
        configurable = true;
        group = BlockGroup.transportation;
        canOverdrive = false;

        config(Integer.class, SenderBuild::setReceiverPackedPos);
    }

    public void genAniState() {
        IdleAni = addAniState("Idle", ((sender, build) -> {

        }));
        UploadAni = addAniState("Upload", (sender, build) -> {
            UploadAnim.draw(Color.green, build.x, build.y);
        });
        BlockedAni = addAniState("Blocked", ((sender, build) -> {
            Draw.color(Color.red);
            Draw.rect(sender.UpArrowTR,
                    build.x, build.y
            );
            Draw.color();
        }));
        NoPowerAni = addAniState("NoPower", ((sender, build) -> {
            Draw.rect(sender.NoPowerTR,
                    build.x, build.y);
        }));
    }

    public void genAniConfig() {
        aniConfig = new AniConfig<>();
        aniConfig.defaultState(IdleAni);
        // Idle
        aniConfig.enter(IdleAni, UploadAni, (block, build) -> {
            IDataReceiver reb = build.getReceiverBuilding();
            if (reb != null) {
                return reb.canAcceptAnyData(build);
            }
            return false;
        }).enter(IdleAni, BlockedAni, (block, build) -> {
            IDataReceiver reb = build.getReceiverBuilding();
            if (reb != null) {
                return !reb.isOutputting() && !reb.canAcceptAnyData(build);
            }
            return false;
        }).enter(IdleAni, NoPowerAni, (block, build) ->
                Mathf.zero(build.power.status)
        );

        // Upload
        aniConfig.enter(UploadAni, IdleAni, (block, build) ->
                build.getReceiverPackedPos() == -1
        ).enter(UploadAni, BlockedAni, (block, build) -> {
            IDataReceiver reb = build.getReceiverBuilding();
            if (reb != null) {
                return !reb.isOutputting() && !reb.canAcceptAnyData(build);
            }
            return false;
        }).enter(UploadAni, NoPowerAni, (block, build) ->
                Mathf.zero(build.power.status)
        );

        // Blocked
        aniConfig.enter(BlockedAni, IdleAni, (block, build) ->
                build.getReceiverPackedPos() == -1
        ).enter(BlockedAni, UploadAni, ((block, build) -> {
            IDataReceiver reb = build.getReceiverBuilding();
            if (reb != null) {
                return reb.isOutputting() || reb.canAcceptAnyData(build);
            }
            return false;
        })).enter(BlockedAni, NoPowerAni, (block, build) ->
                Mathf.zero(build.power.status)
        );

        // NoPower
        aniConfig.enter(NoPowerAni, IdleAni, (block, build) ->
                !Mathf.zero(build.power.status)
        ).enter(NoPowerAni, UploadAni, (block, build) -> {
            if (Mathf.zero(build.power.status)) {
                return false;
            }
            IDataReceiver reb = build.getReceiverBuilding();
            if (reb != null) {
                return reb.canAcceptAnyData(build);
            }
            return false;
        });

        aniConfig.build();
    }

    @Override
    public void load() {
        super.load();
        CoverTR = AtlasUtil.cio("rs-cover");
        UpArrowTR = AtlasUtil.cio("rs-up-arrow");
        CrossTR = AtlasUtil.cio("rs-cross");
        UnconnectedTR = AtlasUtil.cio("rs-unconnected");
        NoPowerTR = AtlasUtil.cio("rs-no-power");
        loadAnimation();
    }

    public void loadAnimation() {
        UploadAnim = AnimUtil.autoCio("rs-up-arrow", UploadAnimFrameNumber, 30f);
    }

    public class SenderBuild extends AniedBuild implements IDataSender {
        private int receiverPackedPos = -1;

        public int getReceiverPackedPos() {
            return receiverPackedPos;
        }

        public void setReceiverPackedPos(int receiverPackedPos) {
            IDataReceiver curBuild = getReceiverBuilding();
            if (curBuild != null) {
                curBuild.disconnect(this);
            }
            this.receiverPackedPos = receiverPackedPos;
            curBuild = getReceiverBuilding();
            if (curBuild != null) {
                curBuild.connect(this);
            }
        }

        private void checkReceiverPos() {
            if (getReceiverPackedPos() != -1 && getReceiverBuilding() == null) {
                setReceiverPackedPos(-1);
            }
        }

        @Override
        public void fixedUpdateTile() {
            checkReceiverPos();
        }

        @Override
        public String toString() {
            return super.toString() + "(receiverPackedPos:" + receiverPackedPos + ")";
        }

        @Override
        public void sendData(@NotNull IDataReceiver receiver, @NotNull Item item, int amount) {
            receiver.receiveData(this, item, amount);
        }

        @Override
        @Nullable
        public Integer connectedReceiver() {
            return receiverPackedPos == -1 ? null : receiverPackedPos;
        }

        @Override
        public void drawSelect() {
            G.init();
            G.drawSurroundingCircle(tile, R.C.Sender);

            IDataReceiver dr = this.getReceiverBuilding();
            if (dr != null) {
                Tile ret = dr.getTile();
                G.drawSurroundingCircle(ret, R.C.Receiver);

                G.drawDashLineBetweenTwoBlocks(this.tile, ret, R.C.Sender);

                G.drawArrowBetweenTwoBlocks(this.tile, ret, R.C.Sender);
            }
        }

        @Override
        public void handleItem(Building source, Item item) {
            IDataReceiver reb = this.getReceiverBuilding();
            if (reb != null && !Mathf.zero(power.status)) {
                this.sendData(reb, item, 1);
            }
        }

        @Nullable
        public IDataReceiver getReceiverBuilding() {
            if (getReceiverPackedPos() != -1) {
                Building rBuild = Vars.world.build(getReceiverPackedPos());
                if (rBuild instanceof IDataReceiver) {
                    return (IDataReceiver) rBuild;
                }
            }
            return null;
        }

        @Override
        public void drawConfigure() {
            G.init();
            Lines.stroke(1f);
            G.drawSurroundingCircle(tile, R.C.Sender);

            IDataReceiver dr = this.getReceiverBuilding();
            if (dr != null) {
                Tile ret = dr.getTile();
                G.drawSurroundingCircle(ret, R.C.Receiver);

                G.drawDashLineBetweenTwoBlocks(this.tile, ret, R.C.Sender);

                G.drawArrowBetweenTwoBlocks(this.tile, ret, R.C.Sender);
            }
        }

        public void clearReceiver() {
            configure(-1);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other) {
                deselect();
                this.clearReceiver();
                return false;
            }

            if (this.getReceiverPackedPos() == other.pos()) {
                deselect();
                this.clearReceiver();
                return false;
            }

            if (other instanceof IDataReceiver) {
                deselect();
                IDataReceiver receiver = (IDataReceiver) other;
                if (receiver.acceptConnection(this)) {
                    this.setReceiver(receiver);
                }
                return false;
            }
            return true;
        }

        public void setReceiver(IDataReceiver receiver) {
            configure(receiver.getBuilding().pos());
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (Mathf.zero(power.status)) {
                return false;
            }
            IDataReceiver reb = this.getReceiverBuilding();
            if (reb != null) {
                return reb.acceptData(this, item);
            }
            return false;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(this.receiverPackedPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            this.receiverPackedPos = read.i();
        }

        @Override
        public Object config() {
            return getReceiverPackedPos();
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