package com.ellucian.app.utilities

import com.ellucian.app.models.AvailableResources
import com.ellucian.app.models.Config

class CliUtilities {

    companion object {
        /**
         * Find padding for pretty printing.
         * @return the padding value.
         */
        fun findPadding(config: Config, availableResources: List<AvailableResources>): Int {
            var padding = 50
            for (availableResource in availableResources) {
                val resources = availableResource.getResourcesMapped().filter { r ->
                    config.onlyCheck == null || config.onlyCheck!!.isEmpty() || config.onlyCheck!!.contains(r.name)
                }

                val paddingOptional = resources.map { r -> r.name!!.length }.max()
                if (paddingOptional != null) {
                    padding = paddingOptional
                }
            }
            return padding
        }
    }
}