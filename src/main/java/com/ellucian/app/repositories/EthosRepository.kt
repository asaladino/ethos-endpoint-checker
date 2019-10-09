package com.ellucian.app.repositories

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.ellucian.app.models.AvailableResources
import com.ellucian.app.models.CheckResponse
import com.ellucian.app.models.Config
import com.ellucian.app.models.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import java.util.concurrent.TimeUnit

class EthosRepository(private val config: Config) {

    private val gson = Gson()
    private var requestKey: String? = null
    private var decodedJWT: DecodedJWT? = null
    private val expirationThreshold = 20

    fun ethosAuth(): DecodedJWT? {
        val client = OkHttpClient.Builder().build()
        val type = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = "".toRequestBody(type)
        val request = Request.Builder()
                .url("https://integrate.elluciancloud.com/auth")
                .addHeader("authorization", "Bearer " + config.ethosApiKey)
                .addHeader("cache-control", "no-cache")
                .post(body)
                .build()
        val response = client.newCall(request).execute()
        requestKey = response.body?.string()
        decodedJWT = JWT.decode(requestKey!!)
        return decodedJWT
    }

    fun retrieveEndpoints(): List<AvailableResources> {
        val endpoint = "https://integrate.elluciancloud.com/admin/available-resources"
        val client = OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build()

        val request = Request.Builder()
                .url(endpoint)
                .header("accept", "application/json")
                .addHeader("accept-charset", "UTF-8")
                .addHeader("authorization", "Bearer " + requestKey!!)
                .addHeader("cache-control", "no-cache")
                .get()
                .build()

        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val type = TypeToken.getParameterized(List::class.java, AvailableResources::class.java).type
        return gson.fromJson<List<AvailableResources>>(json, type)
    }

    fun check(resource: String): CheckResponse {
        val client = OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build()

        val request = Request.Builder()
                .url("https://integrate.elluciancloud.com/api/$resource")
                .header("accept", "application/json")
                .addHeader("accept-charset", "UTF-8")
                .addHeader("authorization", "Bearer " + requestKey!!)
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/vnd.hedtech.integration.v6+json")
                .get()
                .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
        val listType = object : TypeToken<ArrayList<Any>>() {

        }.type
        val models = gson.fromJson<List<*>>(body, listType)
        return CheckResponse(body, models)
    }

    /**
     * Check to see if the token has expired.
     *
     * @return true if it did expire.
     */
    fun didExpire(): Boolean {
        val now = Calendar.getInstance()
        now.add(Calendar.SECOND, expirationThreshold)
        return decodedJWT!!.expiresAt.before(now.time)
    }
}