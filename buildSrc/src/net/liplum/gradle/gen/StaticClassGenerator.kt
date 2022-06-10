package net.liplum.gradle.gen

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import java.io.OutputStream

class StaticClassGenerator {
    /**
     * Generate a class with static final fields by [json]
     */
    fun generateClass(json: String, context: IGeneratorContext) {
        val data = JSON.parseObject(json)
        val packageName = data["Package"] as String
        val className = data["Class"] as String
        val fields = data["Fields"] as JSONArray
        val indent = context.args["Indent"]?.toIntSafe() ?: 4
        val condition = context.args["Condition"]
        context.fileHandler.createJavaFile(packageName, className).use { file ->
            FieldGenerator(file, indent).declarePackage(packageName).wrapClass(className) {
                fields.forEach {
                    val field = it as JSONObject
                    val name = field["Name"] as String
                    val value = when (val maybeValue = field["Value"]) {
                        is JSONObject -> {// use settings
                            if (condition == null) throw NoConditionException("No condition given but meet conditional value.")
                            maybeValue[condition]
                        }
                        else -> maybeValue
                    }
                    val customConverter = field["Converter"] as? String
                    if (customConverter != null) {
                        val converter = context.converters[customConverter]
                            ?: throw NoSuchConverterException("Can't found the converter of $customConverter.")
                        declareField(converter.qualifiedClassName, name, converter.convert(this, value))
                    } else {
                        when (value) {
                            is Int -> declareField("int", name, "$value")
                            is Float -> declareField("float", name, "${value}f")
                            is Double -> declareField("double", name, "${value}d")
                            is Long -> declareField("long", name, "${value}l")
                            is Boolean -> declareField("boolean", name, "$value")
                            is String -> declareField("String", name, "\"$value\"")
                        }
                    }
                }
            }
        }
    }
}

class NoConditionException(msg: String) : RuntimeException(msg)
class NoSuchConverterException(msg: String) : RuntimeException(msg)
class FieldGenerator(
    val file: OutputStream,
    val indent: Int = 4,
) : IConvertContext {
    var curIndent = 0
    fun declarePackage(name: String): FieldGenerator {
        file += "package $name"
        file.end()
        return this
    }

    inline fun wrapClass(clzName: String, writing: FieldGenerator.() -> Unit): FieldGenerator {
        file += "${curIndentText}public class $clzName{\n"
        curIndent += indent
        writing()
        curIndent -= indent
        file += "${curIndentText}}\n"
        return this
    }
    @Synchronized
    fun declareField(type: String, fieldName: String, init: String): FieldGenerator {
        file += "${curIndentText}public static final $type $fieldName = $init"
        file.end()
        return this
    }

    override fun newObject(qualifiedName: String, vararg args: String): String =
        StringBuilder().run {
            append("new $qualifiedName(")
            for ((i, arg) in args.withIndex()) {
                append(arg)
                if (i < args.size - 1) append(",")
            }
            append(")")
            toString()
        }

    val curIndentText: String
        get() = " ".repeat(curIndent)
}

interface IGeneratorContext {
    val fileHandler: IFileHandler
    val converters: Map<String, IClassConvert>
    val args: Map<String, String>
}

interface IFileHandler {
    fun createJavaFile(`package`: String, file: String): OutputStream
}

interface IClassConvert {
    val qualifiedClassName: String
    fun convert(context: IConvertContext, value: Any): String
}

interface IConvertContext {
    /**
     * New an object [qualifiedName] specifies with [args].
     * @param qualifiedName such as `java.lang.String`
     * @param args such as `"1","2","3"`
     */
    fun newObject(qualifiedName: String, vararg args: String): String
}

class EasyConverter(override val qualifiedClassName: String) : IClassConvert {
    override fun convert(context: IConvertContext, value: Any): String =
        context.newObject(qualifiedClassName, value.toString())
}

abstract class ClassConverter(override val qualifiedClassName: String) : IClassConvert

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

fun OutputStream.line() {
    this += "\n"
}

fun OutputStream.end() {
    this += ";\n"
}

fun String.repeat(times: Int) = StringBuilder().run {
    for (i in 0 until times)
        append(this@repeat)
    toString()
}

fun String.toIntSafe(): Int? =
    runCatching { this.toInt() }.getOrDefault(null)