package bot.bricolo.basalt

import okhttp3.*
import org.json.JSONObject
import okhttp3.internal.Version as OkVersion
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class HTTP {
    private val logger = LoggerFactory.getLogger("Basalt::HTTP")
    private val userAgent = "Basalt/${Version.VERSION} ${OkVersion.userAgent()} (https://github.com/BricoloDuDimanche/Basalt)"
    private val hiddenUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0"

    private val httpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(200, 5L, TimeUnit.MINUTES))
            .retryOnConnectionFailure(false)
            .build()

    inner class PendingRequest(private val request: Request) {
        fun queue(success: (Response?) -> Unit) {
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("An error occurred during a request to ${call.request().url()}", e)
                    success(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    success(response)
                }
            })
        }

        fun execute(): Response? {
            return try {
                httpClient.newCall(request).execute()
            } catch (e: IOException) {
                logger.error("An error occurred during a request to ${request.url()}", e)
                null
            }
        }
    }

    fun get(url: String, headers: Headers = Headers.of(), hideUserAgent: Boolean = false): PendingRequest {
        return makeRequest("GET", url, null, headers, hideUserAgent)
    }

    fun post(url: String, body: RequestBody, headers: Headers = Headers.of(), hideUserAgent: Boolean = false): PendingRequest {
        return makeRequest("POST", url, body, headers, hideUserAgent)
    }

    private fun makeRequest(method: String, url: String, body: RequestBody? = null, headers: Headers, hideUserAgent: Boolean): PendingRequest {
        val request = Request.Builder()
                .method(method.toUpperCase(), body)
                .header("User-Agent", if (hideUserAgent) hiddenUserAgent else userAgent)
                .headers(headers)
                .url(url)

        return PendingRequest(request.build())
    }
}

fun createHeaders(vararg kv: Pair<String, String>): Headers {
    val builder = Headers.Builder()

    for (header in kv) {
        builder.add(header.first, header.second)
    }

    return builder.build()
}

fun Response.json(): JSONObject? {
    val body = body()

    body.use {
        return if (isSuccessful && body != null) JSONObject(body.string()) else null
    }
}
