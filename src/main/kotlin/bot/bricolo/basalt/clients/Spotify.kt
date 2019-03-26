package bot.bricolo.basalt.clients

import bot.bricolo.basalt.Basalt
import bot.bricolo.basalt.clients.entities.BasaltPlaylist
import bot.bricolo.basalt.clients.entities.BasaltTrack
import bot.bricolo.basalt.createHeaders
import bot.bricolo.basalt.json
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Spotify(private val clientId: String, private val clientSecret: String) {
    private val logger = LoggerFactory.getLogger("Basalt:SpotifyClient")
    private var accessToken: String = ""
    private var enabled = false

    init {
        refreshAccessToken()
    }

    // ----------
    // API Functions
    // ----------
    fun getTrack(trackId: String, callback: (BasaltTrack?) -> Unit) {
        if (!enabled) return callback(null)
        Basalt.HTTP.get("https://api.spotify.com/v1/tracks/$trackId", createHeaders(Pair("Authorization", "Bearer $accessToken"))).queue {
            val json = it?.json() ?: return@queue callback(null)
            val artist = json.getJSONArray("artists").getJSONObject(0).getString("name")
            val trackName = json.getString("name")
            callback(BasaltTrack(artist, trackName))
        }
    }

    fun getTracksFromPlaylist(userId: String, playlistId: String, callback: (SpotifyPlaylist?) -> Unit) {
        if (!enabled) return callback(null)
        Basalt.HTTP.get("https://api.spotify.com/v1/users/$userId/playlists/$playlistId/tracks", createHeaders(Pair("Authorization", "Bearer $accessToken"))).queue {
            handleItems(it, callback)
        }
    }

    fun getTracksFromAlbum(albumId: String, callback: (SpotifyPlaylist?) -> Unit) {
        if (!enabled) return callback(null)
        Basalt.HTTP.get("https://api.spotify.com/v1/albums/$albumId/tracks", createHeaders(Pair("Authorization", "Bearer $accessToken"))).queue {
            handleItems(it, callback, true)
        }
    }

    // ----------
    // Blocking versions
    // ----------
    fun getTrackBlocking(trackId: String): BasaltTrack? {
        val promise = CompletableFuture<BasaltTrack?>()

        getTrack(trackId) {
            promise.complete(it)
        }

        return promise.get(10, TimeUnit.SECONDS)
    }

    fun getTracksFromPlaylistBlocking(userId: String, playlistId: String): SpotifyPlaylist? {
        val promise = CompletableFuture<SpotifyPlaylist?>()

        getTracksFromPlaylist(userId, playlistId) {
            promise.complete(it)
        }

        return promise.get(10, TimeUnit.SECONDS)
    }

    fun getTracksFromAlbumBlocking(albumId: String): SpotifyPlaylist? {
        val promise = CompletableFuture<SpotifyPlaylist?>()

        getTracksFromAlbum(albumId) {
            promise.complete(it)
        }

        return promise.get(10, TimeUnit.SECONDS)
    }

    // ----------
    // Helpers
    // ----------
    private fun refreshAccessToken() {
        val base64Auth = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        val body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "grant_type=client_credentials")

        Basalt.HTTP.post("https://accounts.spotify.com/api/token", body, createHeaders(Pair("Authorization", "Basic $base64Auth"))).queue {
            val json = it?.json()

            if (json == null) {
                logger.warn("Unable to refresh access token: Response body was null!", it?.code() ?: 0, it?.message())
                Basalt.setTimeout(this::refreshAccessToken, 1 * 60 * 1000)
                enabled = false
                return@queue
            }

            if (json.has("error") && json.getString("error").startsWith("invalid_")) {
                logger.error("Failed to refresh access token: ${json.getString("error")}")
                Basalt.setTimeout(this::refreshAccessToken, 1 * 60 * 1000)
                enabled = false
                return@queue
            }

            val refreshIn = json.getInt("expires_in")
            accessToken = json.getString("access_token")
            Basalt.setTimeout(this::refreshAccessToken, (refreshIn * 1000) - 10000)
            enabled = true

            logger.debug("Updated access token")
        }
    }

    private fun handleItems(response: Response?, callback: (SpotifyPlaylist?) -> Unit, album: Boolean = false) {
        val json = response?.json() ?: return callback(null)
        val playlist = SpotifyPlaylist(if (album) "Spotify Album" else "Spotify Playlist")

        if (!json.has("items")) return callback(null)
        val tracks = json.getJSONArray("items")

        tracks.forEach {
            val t = if (album) it as JSONObject else (it as JSONObject).getJSONObject("track")
            val artist = t.getJSONArray("artists").getJSONObject(0).getString("name")
            val trackName = t.getString("name")
            playlist.addTrack(artist, trackName)
        }

        callback(playlist)
    }

    class SpotifyPlaylist(override val name: String) : BasaltPlaylist(name)
}
