@file:Suppress("UNCHECKED_CAST")

package net.liplum.api.data

import net.liplum.utils.build
import net.liplum.utils.exists

fun Int.db(): IDataBuilding?=
    this.build as? IDataBuilding

fun Int.dr(): IDataReceiver? =
    this.build as? IDataReceiver

fun Int.ds(): IDataSender? =
    this.build as? IDataSender

val IDataBuilding?.exists: Boolean
    get() = this != null && this.building.exists
