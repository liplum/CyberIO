package net.liplum.common.shader

import arc.Core
import arc.files.Fi
import arc.graphics.gl.Shader

class ShaderCompileException(message: String, cause: Throwable) : RuntimeException(message, cause)

fun preprocessFragment(file: Fi): String =
    preprocessFragment(file.readString())

fun preprocessFragment(source: String): String {
    val preprocessed = preprocess(source, true)
    var res = preprocessed
    if (Shader.prependFragmentCode != null && Shader.prependFragmentCode.isNotEmpty())
        res = Shader.prependFragmentCode + preprocessed
    return res
}
/**
 * Copied from [Shader.preprocess].
 * Please check whether the original was updated.
 */
@Suppress("SpellCheckingInspection")
fun preprocess(sourceCode: String, fragment: Boolean): String {
    var source = sourceCode
    //add GL_ES precision qualifiers
    source = if (fragment) {
        """#ifdef GL_ES
precision ${if (source.contains("#define HIGHP") && !source.contains("//#define HIGHP")) "highp" else "mediump"} float;
precision mediump int;
#else
#define lowp  
#define mediump 
#define highp 
#endif
$source"""
    } else {
        //strip away precision qualifiers
        """
     #ifndef GL_ES  
     #define lowp  
     #define mediump 
     #define highp 
     #endif
     $source
     """.trimIndent()
    }
    //preprocess source to function correctly with OpenGL 3.x core
    //note that this is required on Mac
    if (Core.gl30 != null) {
        //if there already is a version, do nothing
        //if on a desktop platform, pick 150 or 130 depending on supported version
        //if on anything else, it's GLES, so pick 300 ES
        val version = if (source.contains("#version "))
            ""
        else if (Core.app.isDesktop)
            if (Core.graphics.glVersion.atLeast(3, 2))
                "150"
            else
                "130"
        else
            "300 es"
        return ("""
    #version $version
    ${if (fragment) "out lowp vec4 fragColor;\n" else ""}
    """.trimIndent()
                + source
            .replace("varying", if (fragment) "in" else "out")
            .replace("attribute", if (fragment) "???" else "in")
            .replace("texture2D(", "texture(")
            .replace("textureCube(", "texture(")
            .replace("gl_FragColor", "fragColor"))
    }
    return source
}