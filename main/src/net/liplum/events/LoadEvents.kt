package net.liplum.events
import mindustry.mod.Mod
/**
 * It will be raised at the head of [Mod.init]
 */
class CioInitEvent
/**
 * It will be raised at the head of [Mod.loadContent]
 * ,before all Contents in Cyber are loaded.
 * ### Before:
 * 1. Contents
 * 2. Techtree
 */
class CioLoadContentEvent