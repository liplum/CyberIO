package net.liplum

import arc.Core
import arc.util.Log
import net.liplum.Clog.toLinkedString
import net.liplum.utils.bundle
import net.liplum.utils.loadMore
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
    val Engine = Interpreter()
    val Name2Story = HashMap<String, Story>()
    var CurStory = Story()
    var newLoaded = true
    var awating = false
    @JvmStatic
    @ClientOnly
    fun loadStory(name: String) {
        val story = Name2Story[name] ?: throw StoryNotFoundException(name)
        CurStory = story
        Engine.tree = CurStory.storyTree
        newLoaded = true
    }
    @JvmStatic
    @ClientOnly
    fun init() {
        val storyStream = Res.loadInThisJar("/stories/en.properties")
        val scriptStream = Res.loadInThisJar("/scripts/TestStory-0.node")
        val storyReader = storyStream.reader()
        Core.bundle.loadMore(storyReader)
        val TestStoryTree = NodeLang.Default.deserialize(DataInputStream(scriptStream))
        Story("TestStory").apply {
            storyTree = TestStoryTree
        }.add()
    }
    @JvmStatic
    fun execute() {
        if (newLoaded) {
            Engine.clearRuntimeStates()
            Engine.start()
            newLoaded = false
        }
        while (!awating) {
            Engine.execute()
        }
    }
    @JvmStatic
    @ClientOnly
    fun initInterpreter() {
        Engine.addAction("log") {
            Log.info(it.toLinkedString())
            awating = true
        }
        Engine.onBlocked {
            awating = true
        }
    }

    fun Story.add() {
        Name2Story[name] = this
    }
}
