import mindustry.io.JsonIO
import mindustry.world.modules.ItemModule

fun main(args: Array<String>) {
    val items = ItemModule()
    val jsonText = JsonIO.json.toJson(items)
    println(jsonText)
}