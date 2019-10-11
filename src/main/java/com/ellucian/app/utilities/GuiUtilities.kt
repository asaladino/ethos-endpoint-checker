package com.ellucian.app.utilities

import com.ellucian.app.models.Result
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.TableView
import kotlin.concurrent.thread
import kotlin.random.Random

class GuiUtilities {

    private fun startTestScan(results: ObservableList<Result>, scanProgressBar: ProgressBar?, endpointsTableView: TableView<Result>?) {
        thread {
            for (i in 0..20) {
                val randTime = System.nanoTime()
                val result = Result("endpoint$randTime", "resource$randTime", -1, "", 0)
                results.add(result)
            }
            Platform.runLater { scanProgressBar?.progress = 0.0 }
            var count = 0
            results.forEach {
                val startTime = System.nanoTime()
                Platform.runLater {
                    endpointsTableView?.selectionModel?.select(it)
                    endpointsTableView?.scrollTo(it)
                }
                Thread.sleep(100)
                val r = Random.nextInt(0, 100)
                it.time = (System.nanoTime() - startTime) / 1000000
                it.numberOf = r
                it.message = "Something $r"
                it.successProperty.set(r % 2 == 0)

                count++
                Platform.runLater {
                    scanProgressBar?.progress = count.toDouble() / results.size
                }
            }
            Platform.runLater { scanProgressBar?.progress = 0.0 }
        }
    }
}