@file:JvmName("MdtBundleH")

package net.liplum.utils

import mindustry.world.Block
import plumy.dsl.bundle
import plumy.core.ClientOnly

/**
 * Localize the bundle by the key: ContentType.Name.key
 *
 * E.g.: block.test-block.tip --> "tip" is [key]
 */
@ClientOnly
fun Block.subBundle(key: String): String =
    "$contentType.$name.$key".bundle
/**
 * Localize the bundle with arguments by the key: ContentType.Name.key
 *
 * E.g.: block.test-block.tip --> "tip" is [key]
 */
@ClientOnly
fun Block.subBundle(key: String, vararg args: Any): String =
    "$contentType.$name.$key".bundle(*args)