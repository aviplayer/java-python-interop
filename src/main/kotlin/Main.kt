package com.ml

import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.IOAccess
import org.graalvm.python.embedding.GraalPyResources
import org.graalvm.python.embedding.VirtualFileSystem
import org.json.JSONObject

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import com.akuleshov7.ktoml.Toml
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Serializable
data class Config(
    val pythonLibsPath: String,
)

fun loadTomlConfig(resourceName: String): Config {
    val inputStream = Main::class.java.getResourceAsStream(resourceName)
        ?: throw IOException("$resourceName not found")
    val reader = BufferedReader(InputStreamReader(inputStream))
    val tomlString = reader.use { it.readText() }
    return Toml.decodeFromString<Config>(tomlString)
}

class Main

private fun getVirtualFileSystem(): VirtualFileSystem {
    return VirtualFileSystem.newBuilder()
        .resourceDirectory("GRAALPY-VFS/com.ml/ml-svc")
        .build()
}



fun main() {
    val cfg = loadTomlConfig("/config.toml")
    GraalPyResources.contextBuilder(getVirtualFileSystem())
        .allowAllAccess(true)
        // allows python to access the java language
        .allowHostAccess(HostAccess.ALL)
        // allow access to the virtual and the host filesystem, as well as sockets
        .allowIO(IOAccess.ALL)
        // allow creating python threads
        .allowCreateThread(true)
        // allow running Python native extensions
        .allowNativeAccess(true)
        // allow exporting Python values to polyglot bindings and accessing Java from Python
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .build().use { ctx ->
            val pythonBindings = ctx.getBindings("python")
            val src =  Main::class.java.getResource("/python/main.py")
                .let { Source.newBuilder("python", it)
                    .build() }

            val importLibsCode = "import sys; sys.path.append('${cfg?.pythonLibsPath}')"

            ctx.eval("python", importLibsCode)

            ctx.eval(src)


            val analyzeSentiment = pythonBindings
                .getMember("analyze_sentiment")

            val text = "This is a negative example."
            val result = analyzeSentiment.execute(text)

            println("Result: ${result.asString()}")

            val jsonString = result.asString()
            val jsonObject = JSONObject(jsonString)
            println("Embedding: ${jsonObject.getJSONArray("embedding")}")
        }
}