package net.liplum.processor.events

import arc.Events
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import net.liplum.processor.plusAssign
import net.liplum.processor.simpleName
import java.util.*

class SubscriptionProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val subscribeFullName = options["Event.SubscribeQualifiedName"] ?: "net.liplum.annotations.Subscribe"
        val subscribeShortName = subscribeFullName.simpleName()
        val symbols = resolver
            .getSymbolsWithAnnotation(subscribeFullName)
            .filterIsInstance<KSFunctionDeclaration>()
        if (!symbols.iterator().hasNext()) return emptyList()
        val packageName = options["Event.PackageName"] ?: "net.liplum.gen"
        val fileName = options["Event.FileName"] ?: "GeneratedEventFile"
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = packageName,
            fileName = fileName
        )
        if (packageName.isNotEmpty())
            file += "package $packageName\n"
        val spec = options["Event.GenerateSpec"] ?: ""
        val useTopLevel = spec.isEmpty()
        if (!useTopLevel) {
            // Start object $spec
            file += "object $spec{\n"
        }
        val event2Subscribers: Multimap<String, Subscriber> = ArrayListMultimap.create()
        // Visit all functions and add them
        class Visitor : KSVisitorVoid() {
            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                if (function.parameters.isNotEmpty()) {
                    logger.error("Only allow zero-argument Function in @Subscribe", function)
                    return
                }
                val annotation = function.annotations.first {
                    it.shortName.asString() == subscribeShortName
                }
                val eventArg = annotation.arguments.first {
                    it.name?.asString() == "eventType"
                }

                fun findParam(name: String) = annotation.arguments.first { it.name?.asString() == name }.value as Boolean
                val eventTypeName = (eventArg.value as KSType).declaration.qualifiedName?.asString()
                val curFuncFullName = function.qualifiedName?.asString()
                if (curFuncFullName != null && eventTypeName != null) {
                    event2Subscribers[eventTypeName] += Subscriber(
                        curFuncFullName,
                        clientOnly = findParam("clientOnly"),
                        debugOnly = findParam("debugOnly"),
                        headlessOnly = findParam("headlessOnly"),
                        steamOnly = findParam("steamOnly"),
                        unsteamOnly = findParam("unsteamOnly"),
                        desktopOnly = findParam("desktopOnly"),
                        mobileOnly = findParam("mobileOnly"),
                    )
                    logger.info("Function ${function.simpleName.asString()} subscribes ${eventTypeName.simpleName()}")
                }
            }
        }
        symbols.forEach { it.accept(Visitor(), Unit) }
        val EventsName = Events::class.java.name
        val e2s = event2Subscribers.asMap()
        for (event in e2s.keys) {
            file += "// $event\n"
        }
        val funcList = LinkedList<String>()
        // Add all subscribers
        for ((event, subscribers) in e2s) {
            // Start function event.name.pascalCase()
            val registerFunc = "register${event.simpleName().pascalCase()}"
            file += "fun ${registerFunc}(){\n"
            funcList += registerFunc
            for (subscriber in subscribers) {
                subscriber.apply {
                    if (clientOnly) file += "net.liplum.ClientOnly{\n"
                    if (debugOnly) file += "net.liplum.DebugOnly{\n"
                    if (headlessOnly) file += "net.liplum.HeadlessOnly{\n"
                    if (steamOnly) file += "net.liplum.SteamOnly{\n"
                    if (unsteamOnly) file += "net.liplum.UnsteamOnly{\n"
                    if (desktopOnly) file += "net.liplum.DesktopOnly{\n"
                    if (mobileOnly) file += "net.liplum.MobileOnly{\n"
                }
                // Start subscription
                file += "$EventsName.run(${event}){\n"
                // Call the subscriber
                file += "${subscriber.funcName}()"
                // End subscription
                file += "}\n"
                subscriber.apply {
                    if (clientOnly) file += "}\n"
                    if (debugOnly) file += "}\n"
                    if (headlessOnly) file += "}\n"
                    if (steamOnly) file += "}\n"
                    if (unsteamOnly) file += "}\n"
                    if (desktopOnly) file += "}\n"
                    if (mobileOnly) file += "}\n"
                }
            }
            file += "}\n"
        }
        file += "fun registerAll(){\n"
        for (func in funcList) {
            file += "$func()\n"
        }
        file += "}\n"
        // End object $spec
        if (!useTopLevel) {
            file += "}\n"
        }
        file.close()
        return symbols.filterNot { it.validate() }.toList()
    }
}

class Subscriber(
    val funcName: String,
    val clientOnly: Boolean,
    val debugOnly: Boolean,
    val headlessOnly: Boolean,
    val steamOnly: Boolean,
    val unsteamOnly: Boolean,
    val desktopOnly: Boolean,
    val mobileOnly: Boolean,
)

fun String.pascalCase(): String {
    if (this.isEmpty()) return ""
    val sb = StringBuilder()
    sb += this[0].uppercase()
    if (this.length > 1)
        sb += this.substring(1)
    return sb.toString()
}

operator fun StringBuilder.plusAssign(str: String) {
    this.append(str)
}

operator fun StringBuilder.plusAssign(c: Char) {
    this.append(c)
}