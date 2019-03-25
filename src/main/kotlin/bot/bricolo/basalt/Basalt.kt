package bot.bricolo.basalt

import andesite.node.NodeState
import andesite.node.Plugin
import bot.bricolo.basalt.clients.Spotify
import bot.bricolo.basalt.sources.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import org.slf4j.LoggerFactory

@Suppress("UnstableApiUsage")
class Basalt : Plugin {
    companion object {
        val HTTP = HTTP()

        fun setTimeout(runnable: () -> Unit, delay: Int) {
            Thread {
                try {
                    Thread.sleep(delay.toLong())
                    runnable()
                } catch (ignored: Exception) {
                }
            }.start()
        }
    }

    private val logger = LoggerFactory.getLogger("Basalt")

    init {
        logger.info("Starting Basalt version ${Version.VERSION}, commit ${Version.COMMIT}")
    }

    override fun configurePlayerManager(state: NodeState, manager: AudioPlayerManager) {
        if (state.config().getBoolean("basalt.spotify.enabled", false)) {
            logger.info("Registering SpotifySourceManager source manager")
            manager.registerSourceManager(SpotifySourceManager(
                    Spotify(
                            state.config().get("basalt.spotify.clientID", ""),
                            state.config().get("basalt.spotify.clientSecret", "")
                    )
            ))
        }
    }
}
