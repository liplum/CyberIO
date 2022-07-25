@file:JvmName("SmoothH")

package net.liplum.mdt.render

import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.lib.math.progressT
import net.liplum.lib.math.smooth
import net.liplum.mdt.advanced.Inspector
import net.liplum.mdt.advanced.Inspector.isConfiguring
import net.liplum.mdt.advanced.Inspector.isPlacing
import net.liplum.mdt.advanced.Inspector.isSelected

/**
 * @param maxTime how much time to reach the 100%
 * @return [0f,1f]
 */
fun Block.smoothPlacing(maxTime: Float): Float =
    if (isPlacing()) progressT(Inspector.placingTime, maxTime).smooth
    else 0f
/**
 * @param maxTime how much time to reach the 100%
 * @return [0f,1f]
 */
fun Building.smoothSelect(maxTime: Float): Float =
    if (isSelected()) progressT(Inspector.selectingTime, maxTime).smooth
    else 0f
/**
 * @param maxTime how much time to reach the 100%
 * @return [0f,1f]
 */
fun Building.smoothConfiguring(maxTime: Float): Float =
    if (isConfiguring()) progressT(Inspector.configuringTime, maxTime).smooth
    else 0f