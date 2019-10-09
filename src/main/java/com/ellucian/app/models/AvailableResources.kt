package com.ellucian.app.models

import com.google.gson.internal.LinkedTreeMap
import java.util.ArrayList

data class AvailableResources(val name: String? = null,
                              val resources: List<*>? = null) {

    fun getResourcesMapped(): List<Resource> {
        val checkedResources = ArrayList<Resource>()
        for (resource in resources!!) {
            val resource1 = Resource()
            if (resource is String) {
                resource1.name = resource
                checkedResources.add(resource1)
            } else {
                val name = (resource as LinkedTreeMap<String, String>)["name"]!!
                resource1.name = name
                checkedResources.add(resource1)
            }
        }
        return checkedResources
    }

    fun getResourcesChecked(check: List<String>?): List<Resource> {
        return getResourcesMapped().filter { check == null || check.isEmpty() || check.contains(it.name) }
    }
}
