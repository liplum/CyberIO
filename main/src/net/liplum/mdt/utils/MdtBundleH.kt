@file:JvmName("MdtBundleH")

package net.liplum.mdt.utils

import mindustry.world.Block
import net.liplum.lib.utils.bundle

/**
 * Localize the bundle by the key: ContentType.Name.key
 *
 * E.g.: block.test-block.tip --> "tip" is [key]
 */
fun Block.subBundle(key: String): String =
    "$contentType.$name.$key".bundle
/**
 * Localize the bundle with arguments by the key: ContentType.Name.key
 *
 * E.g.: block.test-block.tip --> "tip" is [key]
 */
fun Block.subBundle(key: String, vararg args: Any): String =
    "$contentType.$name.$key".bundle(*args)