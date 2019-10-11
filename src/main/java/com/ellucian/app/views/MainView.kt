package com.ellucian.app.views

import com.ellucian.app.models.Config
import com.ellucian.app.models.Result
import com.ellucian.app.repositories.EthosRepository
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
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
        title = "Ethos Endpoint Checker"
        prefWidth = 700.0
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
                    results.clear()
                    startScan()
                }
            }
        }
        endpointsTableView = tableview(results) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            column("Resource", Result::successProperty).cellFormat { rowStatus<Boolean>(this, rowItem.resourceProperty.get()) }
            column("Endpoint", Result::successProperty).cellFormat { rowStatus<Boolean>(this, rowItem.endpointProperty.get()) }
            column("Time (ms)", Result::successProperty).cellFormat { rowStatus<Boolean>(this, rowItem.timeProperty.get().toString()) }
            column("Message", Result::successProperty).cellFormat { rowStatus<Boolean>(this, rowItem.messageProperty.get()) }
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

    private fun <T> rowStatus(tableCell: TableCell<Result, T>, value: String) {
        tableCell.text = value
        tableCell.style {
            if (tableCell.rowItem.successProperty.get()) {
                backgroundColor += Color.LIGHTGREEN
                textFill = Color.BLACK
            } else {
                backgroundColor += Color.INDIANRED
                textFill = Color.BLACK
            }
        }
    }

    private fun startScan() {
        thread {
            Platform.runLater { scanProgressBar?.progress = -1.0 }
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
//                    endpointsTableView?.scrollTo(it)
                }
                try {
                    if (ethosRepository.didExpire()) {
                        ethosRepository.ethosAuth()
                    }
                    val checkResponse = ethosRepository.check(it.endpoint)
//                    Platform.runLater {
                        it.timeProperty.set((System.nanoTime() - startTime) / 1000000)
                        it.numberOfProperty.set(checkResponse.model.size)
                        it.messageProperty.set(checkResponse.body)
                        it.successProperty.set(true)
//                    }
                } catch (e: Exception) {
//                    Platform.runLater {
                        it.timeProperty.set((System.nanoTime() - startTime) / 1000000)
                        it.messageProperty.set(e.message)
                        it.successProperty.set(false)
//                    }
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
