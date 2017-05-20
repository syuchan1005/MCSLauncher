package com.github.syuchan1005.mcslauncher

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Created by syuchan on 2017/05/19.
 */
class ServerController(vararg cmd: String) {
    var isRunningProperty = SimpleBooleanProperty(false)
    private val processBuilder: ProcessBuilder = ProcessBuilder(*cmd)
    private lateinit var process: Process
    private var obsThread: Thread? = null

    init {
        processBuilder.redirectErrorStream(true)
    }

    @Throws(IOException::class)
    fun start() {
        if (!isRunningProperty.get()) {
            process = processBuilder.start()
            if (obsThread == null) {
                obsThread = Thread({
                    while (true) {
                        isRunningProperty.set(process.isAlive)
                    }
                })
                obsThread!!.start()
            }
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    fun stop() {
        if (isRunningProperty.get()) {
            writeCommand("stop\n")
            process.waitFor()
            forceStop()
        }
    }

    @Throws(IOException::class)
    fun reload() {
        writeCommand("reload\n")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun reboot() {
        stop()
        start()
    }

    @Throws(IOException::class)
    fun forceStop() {
        if (isRunningProperty.get()) {
            process.errorStream.close()
            process.inputStream.close()
            process.outputStream.close()
            process.destroy()
        }
    }

    fun connectOutput(list: ObservableList<Controller.ConsoleModel>) {
        Thread {
            try {
                while (true) {
                    Thread.sleep(10)
                    if (isRunningProperty.get()) {
                        InputStreamReader(process.inputStream).use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { bufferedReader ->
                                while (isRunningProperty.get()) {
                                    var line = bufferedReader.readLine() ?: continue
                                    var time = ""
                                    if (line.startsWith("[")) {
                                        time = line.substring(1, 9)
                                        line = line.substring(line.indexOf("]:") + 2)
                                    }
                                    Platform.runLater { list += Controller.ConsoleModel(time, line) }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    @Throws(IOException::class)
    fun writeCommand(cmd: String) {
        if (isRunningProperty.get()) {
            process.outputStream.write(cmd.toByteArray(StandardCharsets.UTF_8))
            process.outputStream.flush()
        }
    }
}
