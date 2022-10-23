package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Align
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.gen.Building
import mindustry.gen.Buildingc
import mindustry.gen.Icon
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.Block
import net.liplum.R
import net.liplum.Settings
import net.liplum.Var
import net.liplum.api.ICyberEntity
import plumy.dsl.bundle
import net.liplum.common.util.inViewField
import net.liplum.common.util.isLineInViewField
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import plumy.core.ClientOnly
import plumy.animation.ContextDraw.DrawScale
import net.liplum.render.*
import net.liplum.utils.worldPos
import net.liplum.render.G
import net.liplum.render.Text
import net.liplum.render.postToastTextOn
import net.liplum.render.postToastTextOnXY
import plumy.core.arc.darken
import plumy.core.math.Point2f
import plumy.core.math.isZero
import plumy.core.math.plusAssign
import plumy.core.math.smooth
import plumy.dsl.*

@ClientOnly
val ArrowDensity: Float
    get() = Settings.LinkArrowDensity
@ClientOnly
val ArrowSpeed: Float
    get() = Settings.LinkArrowSpeed
val Alpha: Float
    get() = Settings.LinkOpacity * Var.GlobalLinkDrawerAlpha
private val p1 = Point2f()
private val p2 = Point2f()
private val c1 = Color()
private val c2 = Color()
@ClientOnly
fun ICyberEntity.toOtherInViewField(other: ICyberEntity): Boolean {
    return isLineInViewField(building.worldPos(p1), other.building.worldPos(p2))
}
//<editor-fold desc="Show Self Circle">
@ClientOnly
fun ICyberEntity.canShowSelfCircle(): Boolean =
    building.worldPos(p1).inViewField(block.clipSize)
//</editor-fold>
//<editor-fold desc="Draw Graph">
@JvmOverloads
@ClientOnly
fun IDataSender.drawDataNetGraph(showCircle: Boolean = true) {
    if (receiverConnectionNumber <= 0) return
    if (showCircle && this.canShowSelfCircle()) {
        G.surroundingCircleBreath(tile, senderColor, alpha = Alpha)
    }
    this.drawReceivers(connectedReceivers, showCircle)
}
@JvmOverloads
@ClientOnly
fun IDataReceiver.drawDataNetGraph(showCircle: Boolean = true) {
    if (senderConnectionNumber <= 0) return
    if (showCircle && this.canShowSelfCircle()) {
        G.surroundingCircleBreath(tile, receiverColor, alpha = Alpha)
    }
    this.drawSenders(connectedSenders, showCircle)
}
@ClientOnly
fun IStreamHost.drawStreamGraph(showCircle: Boolean = true) {
    if (clientConnectionNumber <= 0) return
    if (showCircle && this.canShowSelfCircle()) {
        G.surroundingCircleBreath(tile, hostColor, alpha = Alpha)
    }
    this.drawClients(connectedClients, showCircle)
}
@ClientOnly
fun IStreamClient.drawStreamGraph(showCircle: Boolean = true) {
    if (hostConnectionNumber <= 0) return
    if (showCircle && this.canShowSelfCircle()) {
        G.surroundingCircleBreath(tile, clientColor, alpha = Alpha)
    }
    this.drawHosts(connectedHosts, showCircle)
}
@ClientOnly
fun IP2pNode.drawP2PConnection(showCircle: Boolean = true) {
    val other = connected ?: return
    val drawer = if (this.isDrawer) this else other
    if (showCircle && this.canShowSelfCircle()) {
        G.surroundingCircleBreath(tile, color, alpha = Alpha)
    }
    drawer.drawAnother(showCircle)
}
//</editor-fold>
//<editor-fold desc="Draw Requirements">
@ClientOnly
fun IDataReceiver.drawRequirements() {
    val reqs = this.requirements
    if (reqs != null) {
        G.materialIcons(this.building, reqs, Alpha * 0.8f)
    }
}
@ClientOnly
fun IStreamClient.drawRequirements() {
    val reqs = this.requirements
    if (reqs != null) {
        G.materialIcons(this.building, reqs, Alpha * 0.8f)
    }
}
//</editor-fold>
//<editor-fold desc="Draw Connection">
/**
 * Called in Receiver block
 */
@ClientOnly
fun IDataReceiver.drawSenders(senders: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Alpha
    for (sender in senders) {
        val s = sender.ds() ?: continue
        if (this.toOtherInViewField(s)) {
            if (showCircle && s.canShowSelfCircle()) {
                G.surroundingCircleBreath(s.tile, s.senderColor, alpha = opacity)
            }
            if (s.maxRange > 0f)
                G.circle(s.building.x, s.building.y, s.maxRange, this.receiverColor)
            transferArrowLineBreath(
                s.building, this.building,
                arrowColor = this.receiverColor,
                density = ArrowDensity,
                speed = ArrowSpeed,
                alphaMultiplier = opacity
            )
        }
    }
}
/**
 * Called in Sender block
 */
@ClientOnly
fun IDataSender.drawReceivers(receivers: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Alpha
    for (receiver in receivers) {
        val r = receiver.dr() ?: continue
        if (this.toOtherInViewField(r)) {
            if (showCircle && r.canShowSelfCircle()) {
                G.surroundingCircleBreath(r.tile, r.receiverColor, alpha = opacity)
            }
            transferArrowLineBreath(
                this.building, r.building,
                arrowColor = this.senderColor,
                density = ArrowDensity,
                speed = ArrowSpeed,
                alphaMultiplier = opacity
            )
            r.drawRequirements()
        }
    }
}
/**
 * Called in Client block
 */
@ClientOnly
fun IStreamClient.drawHosts(hosts: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Alpha
    for (host in hosts) {
        val h = host.sh() ?: continue
        if (this.toOtherInViewField(h)) {
            if (showCircle && h.canShowSelfCircle()) {
                G.surroundingCircleBreath(h.tile, h.hostColor, alpha = opacity)
            }
            if (h.maxRange > 0f)
                G.circle(h.building.x, h.building.y, h.maxRange, this.clientColor)
            transferArrowLineBreath(
                h.building, this.building,
                arrowColor = this.clientColor,
                density = ArrowDensity,
                speed = ArrowSpeed,
                alphaMultiplier = opacity
            )
        }
    }
}
/**
 * Called in Host block
 */
@ClientOnly
fun IStreamHost.drawClients(clients: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Alpha
    for (client in clients) {
        val c = client.sc() ?: continue
        if (this.toOtherInViewField(c)) {
            if (showCircle && c.canShowSelfCircle()) {
                G.surroundingCircleBreath(c.tile, c.clientColor, alpha = opacity)
            }
            transferArrowLineBreath(
                this.building, c.building,
                arrowColor = this.hostColor,
                density = ArrowDensity,
                speed = ArrowSpeed,
                alphaMultiplier = opacity
            )
            c.drawRequirements()
        }
    }
}
@ClientOnly
fun IP2pNode.drawAnother(showCircle: Boolean = true) {
    val opacity = Alpha
    val other = connected ?: return
    if (!this.toOtherInViewField(other)) return
    if (isDrawer) {
        if (showCircle && other.canShowSelfCircle()) {
            G.surroundingCircleBreath(other.tile, color, alpha = opacity)
        }
        val sender: Building
        val receiver: Building
        if (status == P2pStatus.Sender) {
            sender = this.building
            receiver = other.building
        } else {
            sender = other.building
            receiver = this.building
        }
        transferArrowLineBreath(
            sender, receiver,
            arrowColor = color,
            density = ArrowDensity,
            speed = ArrowSpeed,
            alphaMultiplier = opacity
        )
    }
}
//</editor-fold>
//<editor-fold desc="Draw Configuring Placing Link">
/**
 * Called in an [IDataReceiver] block
 *
 * @param x        tile x
 * @param y        tile y
 */
@ClientOnly
fun Block.drawLinkedLineToReceiverWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.config.isShown) return
    val sender = Vars.control.input.config.selected
    if (sender !is IDataSender) return
    val selectedTile = sender.tile()
    val opacity = Alpha
    val isOverRange = if (sender.maxRange > 0f) selectedTile.dstWorld(x, y) > sender.maxRange else false
    val color = if (isOverRange) R.C.RedAlert else R.C.Receiver
    G.surroundingCircleBreath(this, x, y, color, alpha = opacity)
    transferArrowLineBreath(
        sender.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        arrowColor = color,
        density = ArrowDensity,
        speed = ArrowSpeed,
        alphaMultiplier = opacity
    )
    if (isOverRange)
        this.drawOverRangeOnTile(x, y, color)
}
/**
 * Called in an [IStreamClient] block
 *
 * @param x        tile x
 * @param y        tile y
 */
@ClientOnly
fun Block.drawLinkedLineToClientWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.config.isShown) return
    val host = Vars.control.input.config.selected
    if (host !is IStreamHost) return
    val selectedTile = host.tile()
    val opacity = Alpha
    val isOverRange = if (host.maxRange > 0f) selectedTile.dstWorld(x, y) > host.maxRange else false
    val color = if (isOverRange) R.C.RedAlert else R.C.Client
    G.surroundingCircleBreath(this, x, y, color, alpha = opacity)
    transferArrowLineBreath(
        host.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        arrowColor = color,
        density = ArrowDensity,
        speed = ArrowSpeed,
        alphaMultiplier = opacity
    )
    if (isOverRange)
        this.drawOverRangeOnTile(x, y, color)
}
/**
 * Called in an [IDataReceiver] block
 *
 * @param x        tile x
 * @param y        tile y
 */
@ClientOnly
fun Block.drawLinkedLineToP2pWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.config.isShown) return
    val node = Vars.control.input.config.selected
    if (node !is IP2pNode) return
    val selectedTile = node.tile()
    val opacity = Alpha
    val isOverRange = if (node.maxRange > 0f) selectedTile.dstWorld(x, y) > node.maxRange else false
    val color = if (isOverRange) R.C.RedAlert else R.C.P2P
    G.surroundingCircleBreath(this, x, y, color, alpha = opacity)
    transferArrowLineBreath(
        node.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        arrowColor = color,
        density = ArrowDensity,
        speed = ArrowSpeed,
        alphaMultiplier = opacity
    )
    if (isOverRange)
        this.drawOverRangeOnTile(x, y, color)
}
//</editor-fold>
//<editor-fold desc="Draw and Post Over Range">
fun Building.postOverRangeOn(other: Building) {
    R.Bundle.OverRange.bundle.postToastTextOn(this.id, other, R.C.RedAlert)
}

fun Building.postOverRangeOnTile(x: TileXY, y: TileXY) {
    R.Bundle.OverRange.bundle.postToastTextOnXY(this.id, x.worldXY, y.worldXY, R.C.RedAlert)
}

fun Block.drawOverRangeOnTile(x: TileXY, y: TileXY, color: Color) {
    val text = R.Bundle.OverRange.bundle
    Text.drawText {
        setText(it, text)
        it.color.set(color)
        it.draw(
            text, getCenterWorldXY(x),
            getCenterWorldXY(y) + size * Vars.tilesize / 2f,
            Align.center
        )
    }
}
//</editor-fold>
//<editor-fold desc="Post Full Connection">
fun Building.postFullSenderOn(other: Building) {
    R.Bundle.FullSender.bundle.postToastTextOn(this.id, other, R.C.RedAlert)
}

fun Building.postFullReceiverOn(other: Building) {
    R.Bundle.FullReceiver.bundle.postToastTextOn(this.id, other, R.C.RedAlert)
}

fun Building.postFullHostOn(other: Building) {
    R.Bundle.FullHost.bundle.postToastTextOn(this.id, other, R.C.RedAlert)
}

fun Building.postFullClientOn(other: Building) {
    R.Bundle.FullClient.bundle.postToastTextOn(this.id, other, R.C.RedAlert)
}
//</editor-fold>
//<editor-fold desc="Draw Selected and Configuring Max range">
fun IDataSender.drawSelectedMaxRange() {
    if (maxRange > 0f) {
        G.dashCircleBreath(
            building.x,
            building.y,
            maxRange * building.smoothSelect(
                Var.SelectedCircleTime + maxRange * Var.MaxRangeCircleTimeFactor
            ),
            senderColor,
            stroke = 3f
        )
    }
}

fun IStreamHost.drawSelectedMaxRange() {
    if (maxRange > 0f) {
        G.dashCircleBreath(
            building.x,
            building.y,
            maxRange * building.smoothSelect(
                Var.SelectedCircleTime + maxRange * Var.MaxRangeCircleTimeFactor
            ),
            hostColor,
            stroke = 3f
        )
    }
}

fun IP2pNode.drawSelectedMaxRange() {
    if (maxRange > 0f) {
        G.dashCircleBreath(
            building.x,
            building.y,
            maxRange * building.smoothSelect(
                Var.SelectedCircleTime + maxRange * Var.MaxRangeCircleTimeFactor
            ),
            color,
            stroke = 3f
        )
    }
}

fun drawPlanMaxRange(x: TileXY, y: TileXY, maxRange: Float, color: Color) {
    if (maxRange > 0f) {
        G.dashCircleBreath(
            x.worldXY,
            y.worldXY,
            maxRange,
            color,
            stroke = 3f
        )
    }
}

fun Block.drawPlacingMaxRange(x: TileXY, y: TileXY, maxRange: Float, color: Color) {
    if (maxRange > 0f) {
        G.dashCircleBreath(
            x.worldXY,
            y.worldXY,
            maxRange * smoothPlacing(
                Var.SelectedCircleTime + maxRange * Var.MaxRangeCircleTimeFactor
            ),
            color,
            stroke = 3f
        )
    }
}

fun IDataSender.drawConfiguringMaxRange() {
    if (maxRange > 0f) {
        G.dashCircleBreath(building.x, building.y, maxRange, senderColor, stroke = 3f)
    }
}

fun IStreamHost.drawConfiguringMaxRange() {
    if (maxRange > 0f) {
        G.dashCircleBreath(building.x, building.y, maxRange, hostColor, stroke = 3f)
    }
}

fun IP2pNode.drawConfiguringMaxRange() {
    if (maxRange > 0f) {
        G.dashCircleBreath(building.x, building.y, maxRange, color, stroke = 3f)
    }
}
//</editor-fold>
//<editor-fold desc="Transfer Arrow Line">
fun transferArrowLineBreath(
    startDrawX: WorldXY, startDrawY: WorldXY,
    endDrawX: WorldXY, endDrawY: WorldXY,
    arrowColor: Color = Pal.power,
    density: Float = 15f,
    /** unit per tick */
    speed: Float = 60f,
    alphaMultiplier: Float = 1f,
) {
    if (density.isZero)
        return
    if (alphaMultiplier <= 0f)
        return
    val originalZ = Draw.z()
    if (Settings.LinkBloom)
        Draw.z(Layer.effect)
    val t = Tmp.v2.set(endDrawX, endDrawY)
        .sub(startDrawX, startDrawY)
    val angle = t.angle()
    val length = t.len()
    val count = (Mathf.ceil(length / density)).coerceAtLeast(1)
    val inner = Tmp.c1.set(arrowColor).a(arrowColor.a * alphaMultiplier)
    val outline = Tmp.c2.set(arrowColor).a(arrowColor.a * alphaMultiplier).darken(0.3f)
    var size = 1f + G.sin * 0.15f
    var outlineSize = 1f + G.sin * 0.15f + 0.4f
    if (Vars.mobile) {
        size *= 0.6f
        outlineSize *= 0.6f
    }
    val time = length / speed * 60f
    val moving = if (speed > 0f) Tmp.v3.set(t).setLength((length * (Time.time % time / time)) % length)
    else Tmp.v3.set(0f, 0f)
    val cur = Tmp.v4.set(
        startDrawX + moving.x,
        startDrawY + moving.y
    )
    val per = t.scl(1f / count)
    for (i in 0 until count) {
        val line = Tmp.v5.set(cur).sub(startDrawX, startDrawY)
        val lineLength = (line.len() % length / length).smooth * length
        line.setLength(lineLength)
        line.add(startDrawX, startDrawY)
        val fadeAlpha = when {
            lineLength <= 10f -> (lineLength / 10f).smooth
            length - lineLength <= 10f -> ((length - lineLength) / 10f).smooth
            else -> 1f
        }
        Draw.color(outline)
        Icon.right.region.DrawScale(line.x, line.y, scale = outlineSize * fadeAlpha, rotation = angle)
        Draw.color(inner)
        Icon.right.region.DrawScale(line.x, line.y, scale = size * fadeAlpha, rotation = angle)
        cur += per
    }
    Draw.z(originalZ)
    Draw.reset()
}

fun transferArrowLineBreath(
    startBlock: Block,
    startBlockX: TileXYs, startBlockY: TileXYs,
    endBlock: Block,
    endBlockX: TileXYs, endBlockY: TileXYs,
    arrowColor: Color = Pal.power,
    density: Float = 15f,
    speed: Float = 60f,
    alphaMultiplier: Float = 1f,
) {
    transferArrowLineBreath(
        startBlock.getCenterWorldXY(startBlockX),
        startBlock.getCenterWorldXY(startBlockY),
        endBlock.getCenterWorldXY(endBlockX),
        endBlock.getCenterWorldXY(endBlockY),
        arrowColor = arrowColor,
        density = density,
        speed = speed,
        alphaMultiplier = alphaMultiplier,
    )
}

fun transferArrowLineBreath(
    start: Buildingc,
    end: Buildingc,
    arrowColor: Color = Pal.power,
    density: Float = 15f,
    speed: Float = 60f,
    alphaMultiplier: Float = 1f,
) = transferArrowLineBreath(
    start.x, start.y,
    end.x, end.y,
    arrowColor = arrowColor,
    density = density,
    speed = speed,
    alphaMultiplier = alphaMultiplier,
)
//</editor-fold>
