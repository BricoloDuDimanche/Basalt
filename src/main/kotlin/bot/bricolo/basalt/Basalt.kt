package bot.bricolo.basalt

import andesite.node.NodeState
import andesite.node.Plugin
import andesite.node.util.RequestUtils
import bot.bricolo.basalt.clients.Deezer
import bot.bricolo.basalt.clients.Spotify
import bot.bricolo.basalt.clients.Tidal
import bot.bricolo.basalt.sources.DeezerSourceManager
import bot.bricolo.basalt.sources.PornHubSourceManager
import bot.bricolo.basalt.sources.SpotifySourceManager
import bot.bricolo.basalt.sources.TidalSourceManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams

class Basalt : Plugin {
    companion object {
        private val logger = LoggerFactory.getLogger("Basalt")
        val HTTP = HTTP()

        lateinit var spotify: Spotify
            private set

        lateinit var deezer: Deezer
            private set

        lateinit var tidal: Tidal
            private set

        lateinit var jedis: Jedis
            private set

        fun setTimeout(runnable: () -> Unit, delay: Int) {
            Thread {
                try {
                    Thread.sleep(delay.toLong())
                    runnable()
                } catch (e: Throwable) {
                    logger.error("Uncaught error in a scheduled task", e)
                }
            }.start()
        }
    }

    override fun init(state: NodeState) {
        println(state.config().entrySet())
        logger.info("Starting Basalt version ${Version.VERSION}, commit ${Version.COMMIT}")
        if (state.config().getBoolean("basalt.source.spotify"))
            spotify = Spotify(state.config().getString("basalt.spotify.clientID"), state.config().getString("basalt.spotify.clientSecret"))

        if (state.config().getBoolean("basalt.source.deezer"))
            deezer = Deezer()

        if (state.config().getBoolean("basalt.source.tidal"))
            tidal = Tidal(state.config().getString("basalt.tidal-countryCode"))

        if (state.config().getBoolean("basalt.cache.enabled")) {
            logger.info("Connecting to Redis")
            // @todo: Allow connection pooling
            jedis = Jedis(state.config().getString("basalt.cache.host"), state.config().getInt("basalt.cache.port"), state.config().getBoolean("basalt.cache.ssl"))
        }
    }

    // Thx Natan <3
    override fun configureRouter(state: NodeState, router: Router) {
        if (!state.config().getBoolean("basalt.cache.enabled")) return

        router.get("/loadtracks").handler { context ->
            val identifiers = context.queryParam("identifier")
            if (identifiers == null || identifiers.isEmpty()) {
                error(context, 400, "Missing identifier query param")
                return@handler
            }

            val identifier = identifiers[0]
            val cacheIdentifier = computeCacheIdentifier(identifier)
            val nocache = context.queryParam("nocache")
            if (nocache == null || nocache.isEmpty() || nocache[0] != "1") {
                val cache = jedis.get("Basalt:$cacheIdentifier")
                if (cache != null) {
                    context.response().end(JsonObject(cache).put("cacheStatus", "HIT").toBuffer())
                    return@handler
                }
            }

            state.requestHandler().resolveTracks(identifier)
                    .thenAccept { json ->
                        if (json.getString("loadType") != "NO_MATCHES") {
                            jedis.set("Basalt:$cacheIdentifier", json.encode(), SetParams().ex(state.config().getInt("basalt.cache.ttl")))
                            json.put("cacheStatus", "MISS")
                        }
                        context.response().end(json.toBuffer())
                    }
                    .exceptionally { e ->
                        if (e is FriendlyException) {
                            context.response().end(
                                    JsonObject()
                                            .put("loadType", "LOAD_FAILED")
                                            .put("cause", RequestUtils.encodeThrowable(context, e))
                                            .put("severity", e.severity.name)
                                            .toBuffer()
                            )
                        } else {
                            context.fail(e)
                        }
                        null
                    }
        }
    }

    override fun configurePlayerManager(state: NodeState, manager: AudioPlayerManager) {
        if (state.config().getBoolean("basalt.source.spotify")) {
            logger.info("Registering SpotifySourceManager source manager")
            manager.registerSourceManager(SpotifySourceManager(state.config().getInt("basalt.max-heavy-tracks")))
        }

        if (state.config().getBoolean("basalt.source.deezer")) {
            logger.info("Registering DeezerSourceManager source manager")
            manager.registerSourceManager(DeezerSourceManager(state.config().getInt("basalt.max-heavy-tracks")))
        }

        if (state.config().getBoolean("basalt.source.tidal")) {
            logger.info("Registering TidalSourceManager source manager")
            manager.registerSourceManager(TidalSourceManager(state.config().getInt("basalt.max-heavy-tracks")))
        }

        if (state.config().getBoolean("basalt.source.pornhub")) {
            logger.info("Registering PornHubSourceManager source manager")
            manager.registerSourceManager(PornHubSourceManager())
        }
    }

    private fun computeCacheIdentifier(identifier: String): String {
        // @todo: make this smart lol
        return identifier
    }

    @Suppress("SameParameterValue")
    private fun error(context: RoutingContext, code: Int, message: String) {
        context.response()
                .setStatusCode(code).setStatusMessage(message)
                .putHeader("Content-Type", "application/json")
                .end(JsonObject()
                        .put("code", code)
                        .put("message", message)
                        .toBuffer()
                )
    }
}
