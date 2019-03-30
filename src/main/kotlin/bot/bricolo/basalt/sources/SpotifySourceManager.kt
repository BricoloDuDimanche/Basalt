package bot.bricolo.basalt.sources

import bot.bricolo.basalt.Basalt
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import java.util.regex.Matcher
import java.util.regex.Pattern

class SpotifySourceManager(val maxTracks: Int) : AbstractSourceLoader() {
    @Suppress("PrivatePropertyName")
    private val PLAYLIST_PATTERN = Pattern.compile("^https?://.*\\.spotify\\.com/user/([a-zA-Z0-9_]+)/playlist/([a-zA-Z0-9]+).*")

    @Suppress("PrivatePropertyName")
    private val ALBUM_PATTERN = Pattern.compile("^https?://.*\\.spotify\\.com/album/([a-zA-Z0-9]+).*")

    @Suppress("PrivatePropertyName")
    private val TRACK_PATTERN = Pattern.compile("^https?://.*\\.spotify\\.com/track/([a-zA-Z0-9]+).*")

    override fun getSourceName() = "spotify"

    override fun loadItem(manager: DefaultAudioPlayerManager, reference: AudioReference): AudioItem? {
        return when {
            TRACK_PATTERN.toRegex().matches(reference.identifier) -> loadTrack(manager, TRACK_PATTERN.matcher(reference.identifier))
            PLAYLIST_PATTERN.toRegex().matches(reference.identifier) -> loadPlaylist(manager, PLAYLIST_PATTERN.matcher(reference.identifier))
            ALBUM_PATTERN.toRegex().matches(reference.identifier) -> loadAlbum(manager, ALBUM_PATTERN.matcher(reference.identifier))
            else -> null
        }
    }

    private fun loadTrack(manager: DefaultAudioPlayerManager, match: Matcher): AudioItem {
        match.matches()
        val trackId: String = match.group(1)
        val track = Basalt.spotify.getTrack(trackId) ?: return AudioReference.NO_TRACK

        return getYoutubeTrack(manager, track)
    }

    private fun loadPlaylist(manager: DefaultAudioPlayerManager, match: Matcher): AudioItem {
        match.matches()
        val userId: String = match.group(1)
        val listId: String = match.group(2)
        val playlist = Basalt.spotify.getTracksFromPlaylist(userId, listId) ?: return AudioReference.NO_TRACK

        playlist.truncate(maxTracks)
        return handlePlaylist(manager, playlist)
    }

    private fun loadAlbum(manager: DefaultAudioPlayerManager, match: Matcher): AudioItem {
        match.matches()
        val albumId: String = match.group(1)
        val playlist = Basalt.spotify.getTracksFromAlbum(albumId) ?: return AudioReference.NO_TRACK

        playlist.truncate(maxTracks)
        return handlePlaylist(manager, playlist)
    }
}
