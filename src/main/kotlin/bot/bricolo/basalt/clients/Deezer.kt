package bot.bricolo.basalt.clients

import bot.bricolo.basalt.Basalt
import bot.bricolo.basalt.sources.entities.BasaltPlaylist
import bot.bricolo.basalt.sources.entities.BasaltTrack
import bot.bricolo.basalt.json
import okhttp3.Response
import org.json.JSONObject
import org.slf4j.LoggerFactory

class Deezer {
    private val logger = LoggerFactory.getLogger("Basalt::DeezerClient")

    // ----------
    // API Functions
    // ----------
    fun getTrack(trackId: String): BasaltTrack? {
        val response = Basalt.HTTP.get("https://api.deezer.com/track/$trackId").block()
        val json = response?.json() ?: return null
        val artist = json.getJSONObject("artist").getString("name")
        val trackName = json.getString("title")
        return BasaltTrack(artist, trackName)
    }

    fun getTracksFromPlaylist(playlistId: String): DeezerPlaylist? {
        val response = Basalt.HTTP.get("https://api.deezer.com/playlist/$playlistId").block()
        return handleItems(response)
    }

    fun getTracksFromAlbum(albumId: String): DeezerPlaylist? {
        val response = Basalt.HTTP.get("https://api.deezer.com/album/$albumId").block()
        return handleItems(response, true)
    }

    // ----------
    // Helpers
    // ----------

    private fun handleItems(response: Response?, album: Boolean = false): DeezerPlaylist? {
        val json = response?.json() ?: return null
        val playlist = DeezerPlaylist(if (album) "Deezer Album" else "Deezer Playlist")

        if (!json.has("tracks")) return null
        val tracks = json.getJSONObject("tracks").getJSONArray("data")

        tracks.forEach {
            val t = it as JSONObject
            val artist = t.getJSONObject("artist").getString("name")
            val trackName = t.getString("title")
            playlist.addTrack(artist, trackName)
        }

        return playlist
    }

    class DeezerPlaylist(override val name: String) : BasaltPlaylist(name)
}
