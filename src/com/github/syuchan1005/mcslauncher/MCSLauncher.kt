package com.github.syuchan1005.mcslauncher

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

/**
 * Created by syuchan on 2017/05/18.
 */
class MCSLauncher : Application() {
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("MCSLauncher.fxml"))
        primaryStage.title = "MCSLauncher (Minecraft Server Launcher)"
        primaryStage.icons.add(Image(MCSLauncher::class.java.getResourceAsStream("icons\\app.png")))
        primaryStage.scene = Scene(root, 600.0, 400.0)
        primaryStage.setOnCloseRequest {
            Platform.exit()
            System.exit(0)
        }
        primaryStage.show()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Application.launch(MCSLauncher::class.java, *args)
        }
    }
}
