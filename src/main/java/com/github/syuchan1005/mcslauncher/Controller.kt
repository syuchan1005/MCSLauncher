package com.github.syuchan1005.mcslauncher

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class Controller : Initializable {
    data class ConsoleModel(var time: String, var msg: String)
    data class PlayerModel(var name: String, var ip: String)

    lateinit var server: ServerController
    val consoleModels = FXCollections.observableArrayList<ConsoleModel>()
    val playerModels = FXCollections.observableArrayList<PlayerModel>()

    @FXML
    lateinit var startButton: Button
    @FXML
    lateinit var stopButton: Button
    @FXML
    lateinit var reloadButton: Button
    @FXML
    lateinit var rebootButton: Button
    @FXML
    lateinit var forceStopButton: Button

    @FXML
    lateinit var consoleArea: TableView<ConsoleModel>
    @FXML
    lateinit var timeColumn: TableColumn<ConsoleModel, String>
    @FXML
    lateinit var msgColumn: TableColumn<ConsoleModel, String>
    @FXML
    lateinit var playerListView: ListView<PlayerModel>
    @FXML
    lateinit var sayCheckBox: JFXCheckBox
    @FXML
    lateinit var commandBox: JFXTextField
    @FXML
    lateinit var sendButton: JFXButton

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val list = ArrayList<String>()
        if (isWindows()) {
            list += "cmd"
            list += "/c"
        }
        list.addAll(listOf("cd", "Server", "&&", "java", "-jar", "spigot-1.11.2.jar"))
        server = ServerController(*list.toTypedArray())
        consoleArea.items = consoleModels
        consoleModels.addListener(ListChangeListener { c ->
            while (c.next()) {
                if (c.wasAdded()) {
                    c.addedSubList.forEach({ model ->
                        val loginIndex = model.msg.indexOf("logged in with entity")
                        if (loginIndex != -1) {
                            val split = model.msg.substring(1, loginIndex - 2).split("[")
                            playerModels += PlayerModel(split[0], split[1])
                        }
                        val logoutIndex = model.msg.indexOf("lost connection:")
                        if (logoutIndex != -1) {
                            val name = model.msg.substring(1, logoutIndex - 1)
                            playerModels.forEach { model ->
                                if (model.name == name) Platform.runLater {playerModels.remove(model)}
                            }
                        }
                    })
                }
            }
        })
        timeColumn.setProperty("time")
        msgColumn.setProperty("msg")
        consoleArea.items.addListener(ListChangeListener {
            consoleArea.scrollTo(Math.max(consoleArea.items.size - 1, 0))
        })
        playerListView.items = playerModels
        playerListView.setCellFactory { PlayerModelCell() }
        server.connectOutput(consoleModels)
        startButton.disableProperty().bind(server.isRunningProperty)
        stopButton.disableProperty().bind(server.isRunningProperty.not())
        reloadButton.disableProperty().bind(server.isRunningProperty.not())
        rebootButton.disableProperty().bind(server.isRunningProperty.not())
        forceStopButton.disableProperty().bind(server.isRunningProperty.not())
    }

    fun <T, S> TableColumn<S, T>.setProperty(property: String) {
        cellValueFactory = PropertyValueFactory<S, T>(property)
    }

    @FXML
    fun startClick() {
        Thread({
            server.start()
        }).start()
    }

    @FXML
    fun stopClick() {
        Thread({
            server.stop()
        }).start()
    }

    @FXML
    fun reloadClick() {
        Thread({
            server.reload()
        }).start()
    }

    @FXML
    fun rebootClick() {
        Thread({
            server.reboot()
        }).start()
    }

    @FXML
    fun forceStopClick() {
        Thread({
            server.forceStop()
        }).start()
    }

    @FXML
    fun sendClick() {
        Thread({
            val prefix = if (sayCheckBox.isSelected) "say " else ""
            server.writeCommand(prefix + commandBox.text + "\n")
        }).start()
    }

    @FXML
    fun commandPress(event: KeyEvent) {
        if (event.code == KeyCode.ENTER) sendButton.fire()
    }
}
