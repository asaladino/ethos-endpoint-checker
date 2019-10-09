package com.ellucian.app

import com.auth0.jwt.interfaces.DecodedJWT
import com.ellucian.app.models.AvailableResources
import com.ellucian.app.models.Config
import com.ellucian.app.repositories.EthosRepository
import com.ellucian.app.utilities.CliUtilities
import com.google.gson.Gson

import java.io.FileReader
import java.io.IOException
import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.lang3.StringUtils
import tornadofx.launch

class EthosCheckIt {
    private val gson = Gson()
    private lateinit var config: Config
    private var decodedJWT: DecodedJWT? = null
    private var availableResources: List<AvailableResources>? = null
    private lateinit var ethosRepository: EthosRepository

    /**
     * Load the configuration file that will hold the key.
     */
    @Throws(IOException::class)
    private fun loadConfig(configFile: String) {
        println("Loading: $configFile")
        config = gson.fromJson(FileReader(configFile), Config::class.java)
        ethosRepository = EthosRepository(config)
    }

    /**
     * Get the ethos api key.
     */
    @Throws(IOException::class)
    private fun ethosAuth() {
        println("Getting ethos api key.")
        decodedJWT = ethosRepository.ethosAuth()
    }

    /**
     * Get all the endpoints.
     */
    @Throws(IOException::class)
    private fun retrieveEndpoints() {
        println("Getting all endpoints.")
        availableResources = ethosRepository.retrieveEndpoints()
    }

    /**
     * Check all the endpoints to see what is working.
     */
    private fun checkingEndpoints() {
        println("Found " + availableResources!!.size + " available resources.")

        val good = if (OS.contains("win")) "+" else "\u2705"
        val bad = if (OS.contains("win")) "x" else "\u274c"

        val errorsFound = AtomicInteger()
        val totalEndpointsFound = AtomicInteger()

        val padding = CliUtilities.findPadding(config, availableResources!!)

        for (availableResource in availableResources!!) {
            val resources = availableResource.getResourcesChecked(config.onlyCheck)

            println("Found " + resources.size + " in " + availableResource.name)
            totalEndpointsFound.addAndGet(resources.size)

            for (resource in resources) {
                val startTime = System.nanoTime()
                try {
                    if (ethosRepository.didExpire()) {
                        ethosAuth()
                    }

                    val checkResponse = ethosRepository.check(resource.name!!)
                    val timeElapsed = (System.nanoTime() - startTime) / 1000000
                    val dataModel = checkResponse.model
                    val body = checkResponse.body

                    println(good + " | " + StringUtils.rightPad(resource.name, padding) +
                            " | " + StringUtils.leftPad(timeElapsed.toString() + "", 6) + " ms" +
                            " | " + StringUtils.leftPad("(" + dataModel.size + ")", 5) +
                            " | " + if (body !== null && body.length < 40) body else body?.substring(0, 40))

                } catch (e: Exception) {
                    errorsFound.getAndIncrement()
                    val timeElapsed = (System.nanoTime() - startTime) / 1000000
                    println(bad + " | " + StringUtils.rightPad(resource.name, padding) +
                            " | " + StringUtils.leftPad(timeElapsed.toString() + "", 6) + " ms" +
                            " | " + StringUtils.leftPad("", 5) +
                            " | " + e.localizedMessage)
                }

            }
        }
        println("Errors Found: $errorsFound")
        val percent = if (totalEndpointsFound.get() != 0) (totalEndpointsFound.get() - errorsFound.get()) / totalEndpointsFound.get().toFloat() else 0
        val defaultFormat = NumberFormat.getPercentInstance()
        println("Operational: " + defaultFormat.format(percent))
    }

    companion object {

        private val OS = System.getProperty("os.name").toLowerCase()

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val ethosCheckIt = EthosCheckIt()
            if(args.isNotEmpty()) {
                println("Starting")

                ethosCheckIt.loadConfig(args[0])
                ethosCheckIt.ethosAuth()
                ethosCheckIt.retrieveEndpoints()
                ethosCheckIt.checkingEndpoints()

                println("Finished")
            } else {
                launch<EthosCheckItGui>(args)
            }
        }
    }
}
