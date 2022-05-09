package net.liplum.scripts

import opengal.core.NodeTree

class KeyNotFoundException(msg: String) : RuntimeException(msg)
class StoryBook {
    var name = ""
    val actors: MutableMap<String, Actor> = HashMap()
    operator fun set(name: String, actor: Actor) {
        actor.bookIn = this
        actors[name] = actor
    }

    operator fun get(name: String): Actor {
        return actors[name] ?: throw KeyNotFoundException("$name not found.")
    }
}

class Actor {
    lateinit var bookIn: StoryBook
    var name = ""
    val stories: MutableMap<String, Story> = HashMap()
    operator fun set(name: String, story: Story) {
        story.actorIn = this
        stories[name] = story
    }

    operator fun get(name: String): Story {
        return stories[name] ?: throw KeyNotFoundException("$name not found.")
    }
}

class Story {
    lateinit var actorIn: Actor
    var name = ""
    var storyTree: NodeTree = EmptyNodeTree
    val fullPath: String
        get() = "${actorIn.bookIn.name}.${actorIn.name}.$name"
    val isLoaded: Boolean
        get() = storyTree != EmptyNodeTree
}