## Script Info

As a description of *ScriptInfo.json*, there is a specification below.

```json
{
  "StoryBookA": {
    "ActorA": [
      "StoryA",
      "StoryB"
    ],
    "ActorB": [
      "StoryA",
      "StoryB"
    ]
  },
  "StoryBookB": {
    "ActorA": [
      "StoryA",
      "StoryB"
    ],
    "ActorB": [
      "StoryA",
      "StoryB"
    ]
  }
}
```

It indicates that a StoryBook object contains all information a scene requires. Thus, even though the name *ActorA*
appears many times, it is different every time. Similarly, the story name *StoryA* is different every time.

Let's say how to use it.
```python
json = getJson()
storyBooks = {}
for bookName, info in json:
    book = StoryBook(bookName)
    storyBooks[bookName] = book
    for actorName, storyNames in info:
        actor = Actor(actorName)
        actor.stories = storyNames
        book.actors[actorName] = actor
```