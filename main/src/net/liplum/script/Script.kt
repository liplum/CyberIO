package net.liplum.script

import arc.Events
import arc.util.I18NBundle
import mindustry.Vars
import mindustry.core.GameState.State
import mindustry.game.EventType.Trigger
import plumy.core.ClientOnly
import net.liplum.CLog
import net.liplum.common.Res
import net.liplum.common.util.createModBundle
import net.liplum.common.util.loadMoreFrom
import net.liplum.common.toLinkedString
import net.liplum.script.ScriptLoader.load
import opengal.core.Interpreter
import opengal.core.NodeTree
import opengal.tree.StopNode

val EmptyNodeTree: NodeTree = NodeTree(listOf(StopNode.X))

class StoryNotFoundException(msg: String) : RuntimeException(msg)
@ClientOnly
object Script {
    private val EmptyStory = Story()
    val bundle: I18NBundle = createModBundle()
    val Engine = Interpreter()
    val Id2Story = HashMap<String, Story>()
    var CurStory: Story = EmptyStory
    val HasStory: Boolean
        get() = CurStory != EmptyStory
    var EngineThread = Thread()
    var StoryBooks: Map<String, StoryBook> = mapOf()
    @JvmStatic
    fun loadStory(id: String) {
        loadStory(Id2Story[id] ?: throw StoryNotFoundException(id))
    }
    @JvmStatic
    fun loadStory(story: Story) {
        if (!story.isLoaded)
            story.load()
        CurStory = story
        Engine.tree = story.storyTree
        Engine.clearRuntimeStates()
        Engine.start()
    }
    @JvmStatic
    fun goNext() {
        if (Engine.isEnd) {
            Engine.clearRuntimeStates()
            Engine.start()
            Engine.execute()
        } else {
            Engine.resumeExecution()
        }
    }
    @JvmStatic
    fun execute() {
        if (!Engine.isEnd) {
            Engine.execute()
        }
    }
    @JvmStatic
    @ClientOnly
    fun init() {
        bundle.loadMoreFrom("stories")
        ScriptLoader.root = Res("script")
        val scriptInfo = Res("ScriptInfo.json")
        StoryBooks = ScriptLoader.loadStoryBookFromInfo(scriptInfo, Id2Story)
    }
    @JvmStatic
    @ClientOnly
    fun initInterpreter() {
        Engine.addAction("log") {
            CLog.info("[CIO]OpeGAL:${it.toLinkedString()}")
        }

        Engine.addAction("scriptLoaded") {
            CLog.info("[CIO]OpeGAL:${it.toLinkedString()} Loaded.")
        }

        Engine.addAction("text") {
            Engine.blockExecution()
            val textID = it[0]
            NpcSystem.curText = bundle["${CurStory.fullPath}.text-$textID"]
        }

        Engine.addAction("pause") {
            ClientOnly {
                Vars.state.set(State.paused)
            }
        }

        Engine.addAction("resume") {
            ClientOnly {
                Vars.state.set(State.playing)
            }
        }

        Engine.onEnd {
            NpcSystem.closeDialog()
        }

        Events.run(Trigger.update) {
            execute()
        }
    }

    fun Story.add() {
        Id2Story[name] = this
    }
}