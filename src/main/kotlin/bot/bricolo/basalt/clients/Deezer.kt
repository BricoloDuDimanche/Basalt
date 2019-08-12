/*
 * Andesite plugin to provide extra features such as caching and sources like Spotify
 * Copyright (C) 2019 Bricolo du Dimanche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bot.bricolo.basalt.clients

import bot.bricolo.basalt.Basalt
import bot.bricolo.basalt.sources.entities.BasaltPlaylist
import bot.bricolo.basalt.sources.entities.BasaltTrack
import bot.bricolo.basalt.json
import okhttp3.Response
import org.json.JSONObject
import org.slf4j.LoggerFactory

class Deezer {
    // ----------
    // API Functions
    // ----------
    fun getTrack(trackId: String): BasaltTrack? {
        val response = Basalt.HTTP.get("https://api.deezer.com/track/$trackId").execute()
        val json = response?.json() ?: return null
        val artist = json.getJSONObject("artist").getString("name")
        val trackName = json.getString("title")
        return BasaltTrack(artist, trackName)
    }

    fun getTracksFromPlaylist(playlistId: String): DeezerPlaylist? {
        val response = Basalt.HTTP.get("https://api.deezer.com/playlist/$playlistId").execute()
        return handleItems(response)
    }

    fun getTracksFromAlbum(albumId: String): DeezerPlaylist? {
        val response = Basalt.HTTP.get("https://api.deezer.com/album/$albumId").execute()
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
