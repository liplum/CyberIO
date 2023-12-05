package net.liplum.blocks.decentralizer

import arc.util.Time
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.world.Block
import net.liplum.CLog
import net.liplum.DebugOnly
import net.liplum.Var
import net.liplum.blocks.decentralizer.RecipeCenter.AllRecipes
import net.liplum.blocks.decentralizer.RecipeCenter.calcuID
import net.liplum.common.util.getF
import net.liplum.render.postToastTextOn
import plumy.dsl.AddBar
import net.liplum.utils.ItemTypeAmount
import plumy.dsl.ID
import java.util.*

open class Decentralizer(name: String) : Block(name) {
    var acceptInputTime = 3 * 60f
    var acceptInputReload = 3 * 60f
    // Timer
    var miningTimer = timers++

    init {
        solid = true
        update = true
        hasItems = true
        itemCapacity = 50
    }

    open inner class DecentralizerBuild : Building() {
        var acceptInputTimer = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        var acceptInputCounter = 0f
        val canAcceptItem: Boolean
            get() = acceptInputTimer > 0f
        var bitset: BitSet = BitSet()
        var tested = ArrayList<Pair<Int, Recipe>>(ItemTypeAmount())
        var prepareForMining = false
        var curIndex = 0
        fun validateIndex() {
            curIndex = curIndex.coerceIn(0, AllRecipes.size - 1)
        }

        var inventory: IntArray? = null
        override fun create(block: Block, team: Team): Building {
            val returned = super.create(block, team)
            inventory = this.items.getF("items")
            return returned
        }

        var curRecipe: Recipe? = null
        open fun mine() {
            val curTest = AllRecipes[curIndex]
            if (hasAllKind(curTest.ingredients)) {
                try {
                    val dst = HammingDistance.dst(bitset, curTest.id)
                    tested.add(Pair(dst, curTest))
                } catch (e: Exception) {
                    CLog.err("cur:$bitset <=> test:${curTest.id}", e)
                }
            }
        }

        open fun hasAllKind(reqs: Array<ItemStack>): Boolean {
            for (req in reqs) {
                if (items[req.item.ID] <= 0) return false
            }
            return true
        }

        open fun check() {
            // Find the one that has the shortest distance
            val shortest = tested.minByOrNull { it.first }
            if (shortest != null)
                curRecipe = shortest.second
        }

        open fun runTask() {
            if (inventory == null) return
            // Reach end
            if (curIndex >= AllRecipes.size - 1) {
                check()
                curIndex = 0
            } else {
                // Mining
                validateIndex()
                mine()
                curIndex++
            }
        }

        override fun updateTile() {
            spendTime()
            if (!canAcceptItem) {
                if (!prepareForMining) prepareMining()
                if (timer(miningTimer, 1f)) {
                    runTask()
                }
            } else {
                prepareForMining = false
            }
        }

        open fun prepareMining() {
            tested.clear()
            calcuBitSet()
            prepareForMining = true
        }

        open fun calcuBitSet() {
            val inventory = inventory ?: return
            bitset = inventory.calcuID()
        }

        open fun spendTime() {
            if (!canAcceptItem)
                acceptInputCounter += edelta()
            if (acceptInputCounter > acceptInputReload) {
                acceptInputTimer = acceptInputTime
                acceptInputCounter = 0f
            }
            if (canAcceptItem) {
                acceptInputTimer -= Time.delta
            }
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            return canAcceptItem && items[item] < itemCapacity
        }

        override fun draw() {
            super.draw()
            DebugOnly {
                "$curRecipe".postToastTextOn(this, Var.Hologram, faded = false)
            }
        }
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<DecentralizerBuild>("counter", {
                "Counter ${acceptInputCounter.toInt()}"
            }, {
                Pal.power
            }, {
                acceptInputCounter / acceptInputReload
            })
            AddBar<DecentralizerBuild>("input-time", {
                "Time ${acceptInputTimer.toInt()}"
            }, {
                Pal.gray
            }, {
                acceptInputTimer / acceptInputTime
            })
            AddBar<DecentralizerBuild>("index", {
                "Index $curIndex"
            }, {
                Pal.range
            }, {
                curIndex / (AllRecipes.size - 1f)
            })
        }
    }
}