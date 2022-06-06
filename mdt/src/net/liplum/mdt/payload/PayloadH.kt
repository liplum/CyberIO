@file:JvmName("PayloadH")

package net.liplum.mdt.payload

import arc.util.io.Writes
import mindustry.content.Items
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.world.blocks.payloads.Payload
import net.liplum.lib.EmptyTR

object EmptyPayload : Payload {
    override fun set(x: Float, y: Float, rotation: Float) {
    }

    override fun draw() {
    }

    override fun drawShadow(alpha: Float) {
    }

    override fun size() = 0f
    override fun x(): Float = 0f
    override fun y(): Float = 0f
    private var reqs = emptyArray<ItemStack>()
    override fun requirements() = reqs
    override fun buildTime() = 0f
    override fun write(write: Writes) {
    }

    override fun icon() = EmptyTR
    override fun content(): Item = Items.copper
}
