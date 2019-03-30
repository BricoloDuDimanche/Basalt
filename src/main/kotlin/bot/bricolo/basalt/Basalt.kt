package bot.bricolo.basalt

import andesite.node.NodeState
import andesite.node.Plugin
import bot.bricolo.basalt.clients.Deezer
import bot.bricolo.basalt.clients.Spotify
import bot.bricolo.basalt.sources.DeezerSourceManager
import bot.bricolo.basalt.sources.PornHubSourceManager
import bot.bricolo.basalt.sources.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import org.slf4j.LoggerFactory

class Basalt : Plugin {
    companion object {
        private val logger = LoggerFactory.getLogger("Basalt")
        val HTTP = HTTP()

        lateinit var spotify: Spotify
            private set

        lateinit var deezer: Deezer
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

        if (state.config().getBoolean("basalt.spotify.enabled", false))
            spotify = Spotify(state.config().get("basalt.spotify.clientID", ""), state.config().get("basalt.spotify.clientSecret", ""))

        if (state.config().getBoolean("basalt.deezer.enabled", false))
            deezer = Deezer()
    }

    override fun configurePlayerManager(state: NodeState, manager: AudioPlayerManager) {
        if (state.config().getBoolean("basalt.spotify.enabled", false)) {
            logger.info("Registering SpotifySourceManager source manager")
            manager.registerSourceManager(SpotifySourceManager())
        }

        if (state.config().getBoolean("basalt.pornhub.enabled", false)) {
            logger.info("Registering PornHubSourceManager source manager")
            manager.registerSourceManager(PornHubSourceManager())
        }

        if (state.config().getBoolean("basalt.deezer.enabled", false)) {
            logger.info("Registering DeezerSourceManager source manager")
            manager.registerSourceManager(DeezerSourceManager())
        }
    }
}
