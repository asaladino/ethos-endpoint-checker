package com.ellucian.app.models

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty

import tornadofx.*

class Result(resource: String,
             endpoint: String,
             time: Long,
             message: String,
             numberOf: Int) {

    val resourceProperty = SimpleStringProperty(resource)
    var resource by resourceProperty

    val successProperty = SimpleBooleanProperty()

    val messageProperty = SimpleStringProperty(message)
    var message by messageProperty

    val endpointProperty = SimpleStringProperty(endpoint)
    var endpoint by endpointProperty

    val timeProperty = SimpleLongProperty(time)
    var time by timeProperty

    val numberOfProperty = SimpleIntegerProperty(numberOf)
    var numberOf by numberOfProperty


}