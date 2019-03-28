package bot.bricolo.basalt.clients

import bot.bricolo.basalt.Basalt
import okhttp3.Headers
import okhttp3.Response
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

abstract class AbstractClient {
    protected fun request(endpoint: String, headers: Headers = Headers.of()): Response? {
        val promise = CompletableFuture<Response?>()
        Basalt.HTTP.get(endpoint, headers).queue { promise.complete(it) }
        return promise.get(10, TimeUnit.SECONDS)
    }
}
