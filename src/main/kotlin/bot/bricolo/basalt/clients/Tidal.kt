package bot.bricolo.basalt.clients

import bot.bricolo.basalt.Basalt
import bot.bricolo.basalt.sources.entities.BasaltPlaylist
import bot.bricolo.basalt.sources.entities.BasaltTrack
import bot.bricolo.basalt.json
import okhttp3.Response
import org.json.JSONObject

class Tidal(private val countryCode: String) {

    // ----------
    // API Functions
    // ----------
    fun getTrack(trackId: String): BasaltTrack? {
        val response = Basalt.HTTP.get(buildUrl("https://api.tidal.com/v1/tracks/$trackId")).execute()
        val json = response?.json() ?: return null
        val artist = json.getJSONObject("artist").getString("name")
        val trackName = json.getString("title")
        return BasaltTrack(artist, trackName)
    }

    fun getTracksFromPlaylist(playlistId: String): TidalPlaylist? {
        val response = Basalt.HTTP.get(buildUrl("https://api.tidal.com/v1/playlists/$playlistId/tracks")).execute()
        return handleItems(response)
    }

    fun getTracksFromAlbum(albumId: String): TidalPlaylist? {
        val response = Basalt.HTTP.get(buildUrl("https://api.tidal.com/v1/albums/$albumId/tracks")).execute()
        return handleItems(response, true)
    }

    // ----------
    // Helpers
    // ----------
    private fun handleItems(response: Response?, album: Boolean = false): TidalPlaylist? {
        val json = response?.json() ?: return null
        val playlist = TidalPlaylist(if (album) "Deezer Album" else "Deezer Playlist")

        if (!json.has("items")) return null
        val tracks = json.getJSONArray("items")

        tracks.forEach {
            val t = it as JSONObject
            val artist = t.getJSONObject("artist").getString("name")
            val trackName = t.getString("title")
            playlist.addTrack(artist, trackName)
        }

        return playlist
    }

    private fun buildUrl(url: String) = "$url?countryCode=$countryCode&token=wdgaB1CilGA-S_s2"

    class TidalPlaylist(override val name: String) : BasaltPlaylist(name)
}