package net.liplum

import arc.Core
import arc.Events
import arc.util.I18NBundle
import arc.util.Log
import mindustry.game.EventType.Trigger
import net.liplum.lib.Res
import net.liplum.lib.toLinkedString
import net.liplum.npc.NpcSystem
import net.liplum.utils.bundle
import net.liplum.utils.createModBundle
import net.liplum.utils.loadMoreFrom
import opengal.core.Interpreter
import opengal.core.NodeTree
import opengal.nl.NodeLang
import java.io.DataInputStream

@ClientOnly
private val EmptyNodeTree: NodeTree = NodeTree(listOf())
@ClientOnly
class Story {
    constructor()
    constructor(name: String) {
        this.name = name
    }

    var storyTree: NodeTree = EmptyNodeTree
    var name: String = ""
    fun getText(): String {
        Core.bundle
        return "".bundle
    }
}

class StoryNotFoundException(msg: String) : RuntimeException(msg)
@ClientOnly
object Script {
    val bundle: I18NBundle = createModBundle()
    val Engine = Interpreter()
    val Name2Story = HashMap<String, Story>()
    var CurStory = Story()
    var EngineThread = Thread()
    @JvmStatic
    @ClientOnly
    fun loadStory(name: String) {
        val story = Name2Story[name] ?: throw StoryNotFoundException(name)
        CurStory = story
        Engine.tree = CurStory.storyTree
        Engine.clearRuntimeStates()
        Engine.start()
    }
    @JvmStatic
    @ClientOnly
    fun init() {
        bundle.loadMoreFrom("stories")
        val scriptStream = Res("/scripts/TestStory-0.node").readAsStream()
        val TestStoryTree = NodeLang.Default.deserialize(DataInputStream(scriptStream))
        Story("TestStory").apply {
            storyTree = TestStoryTree
        }.add()
    }
    @JvmStatic
    fun goNext() {
        Engine.resumeExecution()
    }
    @JvmStatic
    fun execute() {
        if (!Engine.isEnd) {
            Engine.execute()
        }
    }

    var textID = 0
    @JvmStatic
    @ClientOnly
    fun initInterpreter() {
        Engine.addAction("log") {
            Log.info(it.toLinkedString())
        }

        Engine.addAction("text") {
            Engine.blockExecution()
            NpcSystem.curText = bundle["${CurStory.name}.0.text-$textID"]
            textID++
        }

        Engine.onEnd {
            NpcSystem.closeDialog()
        }

        Events.run(Trigger.update) {
            execute()
        }
    }

    fun Story.add() {
        Name2Story[name] = this
    }
}
