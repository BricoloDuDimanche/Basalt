package bot.bricolo.basalt

import andesite.node.NodeState
import andesite.node.Plugin
import bot.bricolo.basalt.clients.Spotify
import bot.bricolo.basalt.clients.DuckDuckGo
import bot.bricolo.basalt.sources.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import org.slf4j.LoggerFactory

@Suppress("UnstableApiUsage")
class Basalt : Plugin {
    companion object {
        private val logger = LoggerFactory.getLogger("Basalt")
        val HTTP = HTTP()

        lateinit var spotify: Spotify
            private set
        lateinit var ddg: DuckDuckGo
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
        logger.info("Starting Basalt version ${Version.VERSION}, commit ${Version.COMMIT}")

        spotify = Spotify(state.config().get("basalt.spotify.clientID", ""), state.config().get("basalt.spotify.clientSecret", ""))
        ddg = DuckDuckGo()
    }

    override fun configurePlayerManager(state: NodeState, manager: AudioPlayerManager) {
        if (state.config().getBoolean("basalt.spotify.enabled", false)) {
            logger.info("Registering SpotifySourceManager source manager")
            manager.registerSourceManager(SpotifySourceManager())
        }
    }
}
