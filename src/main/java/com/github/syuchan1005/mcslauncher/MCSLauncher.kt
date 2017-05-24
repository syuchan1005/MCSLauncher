package com.github.syuchan1005.mcslauncher

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import spark.Spark
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile

/**
 * Created by syuchan on 2017/05/18.
 */
class MCSLauncher : Application() {
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("MCSLauncher.fxml"))
        primaryStage.title = "MCSLauncher (Minecraft Server Launcher)"
        primaryStage.icons.add(Image(javaClass.classLoader.getResourceAsStream("web/icons/app.png")))
        primaryStage.scene = Scene(root, 600.0, 400.0)
        primaryStage.setOnCloseRequest {
            Platform.exit()
            System.exit(0)
        }
        primaryStage.show()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            if (args.isNotEmpty() && args[0] == "-w") {
                copyFile("web/")
                var port = 3000;
                if (args.size >= 2) port = args[1].toInt()
                Spark.port(port)
                Spark.externalStaticFileLocation(File("web/").absolutePath)
                Spark.webSocketIdleTimeoutMillis(180000) // 3min
                Spark.webSocket("/sock", WebSocketController::class.java)
                Spark.init()
                println("PageURL: localhost:" + port)
            } else {
                Application.launch(MCSLauncher::class.java, *args)
            }
        }
    }
}

fun copyF(baseURI: URI, folder: File) {
    val listFiles = folder.listFiles()
    listFiles.forEach {
        val file = File(baseURI.relativize(it.toURI()).path)
        if (!it.isDirectory) {
            Files.copy(it.toPath(), file.absoluteFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } else {
            file.mkdirs()
            copyF(baseURI, File(folder.parentFile, file.toString()))
        }
    }
}

fun copyFile(folder: String) {
    val folderFile = File(MCSLauncher::class.java.classLoader.getResource(folder).file)
    if (folderFile.isDirectory) {
        File(folder).mkdirs()
        copyF(folderFile.parentFile.toURI(), folderFile)
    } else {
        JarFile(MCSLauncher::class.java.protectionDomain.codeSource.location.toURI().path).use {
            it.entries().toList().forEach({ e ->
                if (e.name.startsWith(folder)) {
                    val file = File(e.name)
                    if (!e.isDirectory) {
                        val inputStream = it.getInputStream(e)
                        if (!file.exists()) file.createNewFile()
                        FileOutputStream(file).use {
                            it.write(inputStream.readBytes())
                            it.flush()
                        }
                    } else {
                        file.mkdirs()
                    }
                }
            })
        }
    }
}

fun isWindows(): Boolean {
    return System.getProperty("os.name").startsWith("Windows");
}