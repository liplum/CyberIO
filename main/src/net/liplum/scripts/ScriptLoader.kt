package net.liplum.scripts

import arc.struct.ObjectMap
import arc.util.serialization.JsonValue
import mindustry.io.JsonIO
import net.liplum.lib.Res
import net.liplum.lib.utils.component1
import net.liplum.lib.utils.component2
import opengal.core.NodeTree
import opengal.nl.NodeLang
import java.io.DataInputStream

object ScriptLoader {
    var root = Res("script")
    /**
     * Deserialize a .node file from the path.
     *
     * @param path allows dot '.' to separate path.
     */
    fun nodeLang(path: String): NodeTree {
        val input = root.sub(path.replace('.', '/')+".node").readAsStream()
        val dataInput = DataInputStream(input)
        return NodeLang.Default.deserialize(dataInput)
    }

    fun Story.load() {
        storyTree = nodeLang(fullPath)
    }
    @Suppress("UNCHECKED_CAST")
    fun loadStoryBookFromInfo(infoRes: Res, id2Story: MutableMap<String, Story>): Map<String, StoryBook> {
        val json = JsonIO.json.fromJson(ObjectMap::class.java, infoRes.reader()) as ObjectMap<String, JsonValue>
        val map = HashMap<String, StoryBook>()
        for ((bookName, info) in json.entries()) {
            val book = StoryBook().apply {
                name = bookName
            }
            map[bookName] = book
            for (actorInfo in info) {
                val actor = Actor().apply {
                    name = actorInfo.name
                }
                book[actor.name] = actor
                for (storyName in actorInfo.asStringArray()) {
                    val story = Story().apply {
                        name = storyName
                    }
                    actor[storyName] = story
                    id2Story[story.fullPath] = story
                }
            }
        }
        return map
    }
}