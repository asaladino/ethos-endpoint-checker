package com.ellucian.app.views

import com.ellucian.app.models.Config
import com.ellucian.app.models.Result
import com.ellucian.app.repositories.EthosRepository
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.scene.control.ProgressBar
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.concurrent.thread

class MainView : View() {

    private val results = FXCollections.observableArrayList<Result>()

    private var ethosApiKey: TextField? = null
    private var endpointsTableView: TableView<Result>? = null

    private var scanProgressBar: ProgressBar? = null

    override val root = vbox {
        style {
            padding = box(5.px)
        }
        hbox {
            vboxConstraints {
                marginBottom = 5.0
            }
            ethosApiKey = textfield {
                hboxConstraints {
                    marginRight = 5.0
                    hGrow = Priority.ALWAYS
                }
                promptText = "ethos api key..."
                text = ""
            }
            button("Scan") {
                action {
                    startScan()
                }
            }
        }
        endpointsTableView = tableview(results) {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            column("[ ]", Result::successProperty).useCheckbox()
            readonlyColumn("Resource", Result::resource)
            readonlyColumn("Endpoint", Result::endpoint)
            readonlyColumn("Time (ms)", Result::time)
            readonlyColumn("Message", Result::message)
        }
        hbox {
            vboxConstraints {
                marginTop = 5.0
            }
            label("Waiting")
            scanProgressBar = progressbar {
                hboxConstraints {
                    marginLeft = 5.0
                    hGrow = Priority.ALWAYS
                }
                progress = 0.0
                useMaxWidth = true
            }
        }
    }

    private fun startScan() {
        thread {
            val config = Config(ethosApiKey?.text)
            val ethosRepository = EthosRepository(config)
            ethosRepository.ethosAuth()
            val endpoints = ethosRepository.retrieveEndpoints()
            for (endpoint in endpoints) {
                val resources = endpoint.getResourcesMapped().map { resource ->
                    Result(endpoint.name!!, resource.name!!, -1, "", 0)
                }
                results.addAll(resources)
            }
            Platform.runLater { scanProgressBar?.progress = 0.0 }
            var count = 0
            results.forEach {
                val startTime = System.nanoTime()
                Platform.runLater {
                    endpointsTableView?.selectionModel?.select(it)
                    endpointsTableView?.scrollTo(it)
                }
                try {
                    if (ethosRepository.didExpire()) {
                        ethosRepository.ethosAuth()
                    }
                    val checkResponse = ethosRepository.check(it.endpoint)
                    Platform.runLater {
                        it.time = (System.nanoTime() - startTime) / 1000000
                        it.numberOf = checkResponse.model.size
                        it.message = checkResponse.body
                        it.successProperty.set(true)
                    }
                } catch (e: Exception) {
                    Platform.runLater {
                        it.time = (System.nanoTime() - startTime) / 1000000
                        it.message = e.message
                        it.successProperty.set(false)
                    }
                }
                count++
                Platform.runLater {
                    scanProgressBar?.progress = count.toDouble() / results.size
                }
            }
            Platform.runLater { scanProgressBar?.progress = 0.0 }
        }
    }
}
