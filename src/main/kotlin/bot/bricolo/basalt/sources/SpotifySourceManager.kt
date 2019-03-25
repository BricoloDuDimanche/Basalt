package bot.bricolo.basalt.sources

import bot.bricolo.basalt.clients.Spotify
import bot.bricolo.basalt.clients.entities.BasaltPlaylist
import bot.bricolo.basalt.clients.entities.BasaltTrack
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import java.util.regex.Matcher
import java.util.regex.Pattern

class SpotifySourceManager(val API: Spotify) : AbstractSourceLoader() {
    @Suppress("PrivatePropertyName")
    private val PLAYLIST_PATTERN = Pattern.compile("^https?://.*\\.spotify\\.com/user/([a-zA-Z0-9_]+)/playlist/([a-zA-Z0-9]+).*")

    @Suppress("PrivatePropertyName")
    private val ALBUM_PATTERN = Pattern.compile("^https?://.*\\.spotify\\.com/album/([a-zA-Z0-9]+).*")

    @Suppress("PrivatePropertyName")
    private val TRACK_PATTERN = Pattern.compile("^https?://.*\\.spotify\\.com/track/([a-zA-Z0-9]+).*")

    override fun getSourceName() = "SpotifySourceManager"

    override fun loadItem(manager: DefaultAudioPlayerManager, reference: AudioReference): AudioItem? {
        return when {
            TRACK_PATTERN.toRegex().matches(reference.identifier) -> loadTrack(TRACK_PATTERN.matcher(reference.identifier))
            PLAYLIST_PATTERN.toRegex().matches(reference.identifier) -> loadPlaylist(PLAYLIST_PATTERN.matcher(reference.identifier))
            ALBUM_PATTERN.toRegex().matches(reference.identifier) -> loadAlbum(ALBUM_PATTERN.matcher(reference.identifier))
            else -> null
        }
    }

    private fun loadTrack(match: Matcher): AudioItem {
        match.matches()
        val trackId: String = match.group(1)
        val track = API.getTrackBlocking(trackId) ?: return AudioReference.NO_TRACK

        return getYoutubeTrack(track)
    }

    private fun loadPlaylist(match: Matcher): AudioItem {
        match.matches()
        val userId: String = match.group(1)
        val listId: String = match.group(2)
        val playlist = API.getTracksFromPlaylistBlocking(userId, listId) ?: return AudioReference.NO_TRACK

        return handlePlaylist(playlist)
    }

    private fun loadAlbum(match: Matcher): AudioItem {
        match.matches()
        val albumId: String = match.group(1)
        val playlist = API.getTracksFromAlbumBlocking(albumId) ?: return AudioReference.NO_TRACK

        return handlePlaylist(playlist)
    }
}
