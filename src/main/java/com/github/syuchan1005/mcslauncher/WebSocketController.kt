package com.github.syuchan1005.mcslauncher

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*

/**
 * Created by syuchan on 2017/05/23.
 */
@WebSocket
class WebSocketController() {
    val serverController: ServerController
    val consoleList = FXCollections.observableArrayList<Controller.ConsoleModel>()
    val playerList = FXCollections.observableArrayList<Controller.PlayerModel>()

    val sessions = ArrayList<Session>()

    init {
        val list = ArrayList<String>()
        if (isWindows()) {
            list += "cmd"
            list += "/c"
        }
        list.addAll(listOf("cd", "Server", "&&", "java", "-jar", "spigot-1.11.2.jar"))
        serverController = ServerController(*list.toTypedArray())
        serverController.connectOutput(consoleList)
        serverController.isRunningProperty.addListener({ observable, oldValue, newValue ->
            broadCastMessage("{\"type\":\"change\",\"model\":\"running\",\"status\":\"${newValue}\"}")
        })
        consoleList.addListener(ListChangeListener { c ->
            while (c.next()) {
                if (c.wasAdded()) {
                    c.addedSubList.forEach({ model ->
                        broadCastMessage("{\"type\":\"add\",\"model\":\"console\",\"time\":\"${model.time}\", \"msg\":\'${model.msg}\'}")
                        val loginIndex = model.msg.indexOf("logged in with entity")
                        if (loginIndex != -1) {
                            val split = model.msg.substring(1, loginIndex - 2).split("[")
                            playerList += Controller.PlayerModel(split[0], split[1])
                            broadCastMessage("{\"type\":\"add\",\"model\":\"player\",\"name\":\"${split[0]}\", \"ip\":\"${split[1]}\"}")
                        }
                        val logoutIndex = model.msg.indexOf("lost connection:")
                        if (logoutIndex != -1) {
                            val name = model.msg.substring(1, logoutIndex - 1)
                            playerList.forEach({ player ->
                                if (player.name == name) {
                                    playerList.remove(player)
                                    broadCastMessage("{\"type\":\"remove\",\"model\":\"player\",\"name\":\"${player.name}\"}")
                                }
                            })
                        }
                    })
                }
            }
        })
    }

    @OnWebSocketConnect
    fun onConnect(session: Session) {
        sessions.add(session)
        var send = "{\"type\":\"init\",\"running\":\"" + serverController.isRunningProperty.get() + "\", \"players\": ["
        if (playerList.size == 0) {
            send += ","
        } else {
            playerList.forEach({ player ->
                send += "{\"name\":\"" + player.name + "\",\"ip\":\"" + player.ip + "},"
            })
        }
        session.remote.sendStringByFuture(send.substring(0, send.length - 1) + "]}")
    }

    @OnWebSocketError
    fun onError(session: Session, error: Throwable) {
        sessions.remove(session)
        if (session.isOpen) session.close()
    }

    @OnWebSocketClose
    fun onClose(session: Session, statusCode: Int, reason: String) {
        sessions.remove(session)
    }

    @OnWebSocketMessage
    fun onMessage(session: Session, message: String) {
        if (message.length < 4) return
        if (message.substring(3, 4) == "-") {
            val msg = message.substring(4)
            when (message.substring(0, 3)) {
                "cmd" -> {
                    serverController.writeCommand(msg)
                }
                "btn" -> {
                    when (msg) {
                        "start" -> serverController.start()
                        "stop" -> serverController.stop()
                        "reload" -> serverController.reload()
                        "reboot" -> serverController.reboot()
                        "forceStop" -> serverController.forceStop()
                    }
                }
            }
        }
    }

    fun broadCastMessage(message: String) {
        sessions.filter { it.isOpen }.forEach({
            println(it.isOpen)
            it.remote.sendStringByFuture(message)
        })
    }
}
