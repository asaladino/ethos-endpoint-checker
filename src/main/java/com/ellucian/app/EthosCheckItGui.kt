package com.ellucian.app

import com.ellucian.app.views.MainView
import tornadofx.*

fun main(args: Array<String>) {
    launch<EthosCheckItGui>(args)
}

class EthosCheckItGui: App(MainView::class)