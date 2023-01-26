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
import net.liplum.annotations.OnlySpec
import net.liplum.processor.*
import java.io.OutputStream
import java.util.*

class SubscriptionProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Filter target
        val triggerFName = options["Event.SubscribeQualifiedName"] ?: "net.liplum.annotations.Subscribe"
        val triggerSName = triggerFName.simpleName()
        val eventFName = options["Event.SubscribeEventQualifiedName"] ?: "net.liplum.annotations.SubscribeEvent"
        val eventSName = eventFName.simpleName()
        val triggerSymbls = resolver
            .getSymbolsWithAnnotation(triggerFName)
            .filterIsInstance<KSFunctionDeclaration>()
        val eventSymbls = resolver
            .getSymbolsWithAnnotation(eventFName)
            .filterIsInstance<KSFunctionDeclaration>()
        if (
            !triggerSymbls.iterator().hasNext() &&
            !eventSymbls.iterator().hasNext()
        ) return emptyList()
        // Init file
        val packageName = options["Event.PackageName"] ?: "net.liplum.gen"
        val fileName = options["Event.FileName"] ?: "GeneratedEventFile"
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = packageName,
            fileName = fileName
        )
        // File head
        if (packageName.isNotEmpty())
            file += "package $packageName\n"
        val spec = options["Event.GenerateSpec"] ?: ""
        val useTopLevel = spec.isEmpty()
        if (!useTopLevel) {
            // Start object $spec
            file += "object $spec{\n"
        }
        // Iterate
        val trigger2Subscribers: Multimap<TriggerType, TriggerSubscriber> = ArrayListMultimap.create()
        // Visit all functions and add them
        class TriggerVisitor : KSVisitorVoid() {
            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                if (function.parameters.isNotEmpty()) {
                    logger.warn("Only allow zero-argument Function in @Subscribe", function)
                    return
                }
                val annotation = function.annotations.first {
                    it.shortName.asString() == triggerSName
                }
                val triggerArg = annotation.findParam("triggerType")
                val onlyArg = annotation.findParam("only")
                val triggerName = (triggerArg.value as KSType).declaration.qualifiedName?.asString()
                val only = onlyArg.value as? Int
                val curFuncFullName = function.qualifiedName?.asString()
                if (curFuncFullName != null && triggerName != null && only != null) {
                    trigger2Subscribers[TriggerType(triggerName)] += TriggerSubscriber(
                        curFuncFullName, OnlySpec(only)
                    )
                    logger.info("Function ${function.simpleName.asString()} subscribes trigger ${triggerName.simpleName()}")
                }
            }
        }
        triggerSymbls.forEach { it.accept(TriggerVisitor(), Unit) }
        val event2Subscribers: Multimap<EventType, EventSubscriber> = ArrayListMultimap.create()

        class EventVisitor : KSVisitorVoid() {
            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                val paraNumber = function.parameters.size
                if (paraNumber != 1 && paraNumber != 0) {
                    logger.warn("Only allow trigger-instance-argument or zero-argument Function in @SubscribeEvent", function)
                    return
                }
                val annotation = function.annotations.first {
                    it.shortName.asString() == eventSName
                }
                val eventArg = annotation.findParam("eventClz")
                val onlyArg = annotation.findParam("only")
                val eventName = (eventArg.value as KSType).declaration.qualifiedName?.asString()
                val only = onlyArg.value as? Int
                val curFuncFullName = function.qualifiedName?.asString()
                if (curFuncFullName != null && eventName != null && only != null) {
                    event2Subscribers[EventType(eventName)] += EventSubscriber(
                        curFuncFullName, OnlySpec(only), isZeroArg = function.parameters.isEmpty()
                    )
                    logger.info("[SubscriptionProcessor] Function ${function.qualifiedName?.asString()} subscribes Event ${eventName.simpleName()}")
                }
            }
        }
        eventSymbls.forEach { it.accept(EventVisitor(), Unit) }
        val EventsName = Events::class.java.name
        // Head comments
        val t2s = trigger2Subscribers.asMap()
        val e2s = event2Subscribers.asMap()
        for (trigger in t2s.keys) {
            file += "// ${trigger.fName}\n"
        }
        for (event in e2s.keys) {
            file += "// ${event.fName}\n"
        }
        // Add all trigger subscribers
        val funcList = LinkedList<String>()
        for ((trigger, subscribers) in t2s) {
            // Start function trigger.name.pascalCase()
            val registerFunc = "register${trigger.simpleName.pascalCase()}"
            file += "fun ${registerFunc}(){\n"
            funcList += registerFunc
            for (subscriber in subscribers) {
                subscriber.onlySpec.addHead(file)
                // Start subscription
                file += "$EventsName.run(${trigger.fName}){\n"
                // Call the subscriber
                file += "${subscriber.funcName}()"
                // End subscription
                file += "}\n"
                subscriber.onlySpec.addTail(file)
            }
            file += "}\n"
        }
        // Add all event subscribers
        for ((event, subscribers) in e2s) {
            // Start function trigger.name.pascalCase()
            val registerFunc = "registerOn${event.simpleName.pascalCase()}"
            file += "fun ${registerFunc}(){\n"
            funcList += registerFunc
            for (subscriber in subscribers) {
                subscriber.onlySpec.addHead(file)
                // Start subscription
                file += "$EventsName.on(${event.fName}::class.java){\n"
                // Call the subscriber
                file += if (subscriber.isZeroArg) "${subscriber.funcName}()"
                else "${subscriber.funcName}(it)"
                // End subscription
                file += "}\n"
                subscriber.onlySpec.addTail(file)
            }
            file += "}\n"
        }
        // Register all subscribers
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
        return eventSymbls.filterNot { it.validate() }.toList()
    }
}
@JvmInline
value class TriggerType(
    val fName: String,
) {
    val simpleName: String
        get() = fName.simpleName()
}
@JvmInline
value class EventType(
    val fName: String,
) {
    val simpleName: String
        get() = fName.simpleName()
}

fun OnlySpec.addHead(file: OutputStream) {
    if (client) file += "plumy.core.ClientOnly{\n"
    if (debug) file += "net.liplum.DebugOnly{\n"
    if (headless) file += "plumy.core.HeadlessOnly{\n"
    if (steam) file += "plumy.core.SteamOnly{\n"
    if (unsteam) file += "plumy.core.NonSteamOnly{\n"
    if (desktop) file += "plumy.core.DesktopOnly{\n"
    if (mobile) file += "plumy.core.MobileOnly{\n"
}

fun OnlySpec.addTail(file: OutputStream) {
    val braceNumber = only.countOneBits()
    file += "}" * braceNumber
    file += "\n"
}

class TriggerSubscriber(
    val funcName: String,
    val onlySpec: OnlySpec,
)

class EventSubscriber(
    val funcName: String,
    val onlySpec: OnlySpec,
    val isZeroArg: Boolean,
)
