@file:JvmName("SmoothH")

package net.liplum.input

import mindustry.gen.Building
import mindustry.world.Block
import plumy.core.math.smooth
import net.liplum.input.Inspector
import net.liplum.input.Inspector.isConfiguring
import net.liplum.input.Inspector.isPlacing
import net.liplum.input.Inspector.isSelected
import plumy.core.math.progressTime

/**
 * @param maxTime how much time to reach the 100%
 * @return [0f,1f]
 */
fun Block.smoothPlacing(maxTime: Float): Float =
    if (isPlacing()) progressTime(Inspector.placingTime, maxTime).smooth
    else 0f
/**
 * @param maxTime how much time to reach the 100%
 * @return [0f,1f]
 */
fun Building.smoothSelect(maxTime: Float): Float =
    if (isSelected()) progressTime(Inspector.selectingTime, maxTime).smooth
    else 0f
/**
 * @param maxTime how much time to reach the 100%
 * @return [0f,1f]
 */
fun Building.smoothConfiguring(maxTime: Float): Float =
    if (isConfiguring()) progressTime(Inspector.configuringTime, maxTime).smooth
    else 0f