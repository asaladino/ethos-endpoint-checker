package com.ellucian.app.models

import javafx.beans.property.SimpleBooleanProperty

class Result(val resource: String,
             val endpoint: String,
             var time: Long,
             var message: String?,
             var numberOf: Int) {

    val successProperty = SimpleBooleanProperty()
}