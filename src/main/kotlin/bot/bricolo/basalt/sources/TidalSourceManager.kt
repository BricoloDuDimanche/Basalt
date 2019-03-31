package bot.bricolo.basalt.sources

import bot.bricolo.basalt.Basalt
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import java.util.regex.Matcher
import java.util.regex.Pattern

class TidalSourceManager(private val maxTracks: Int) : AbstractSourceLoader() {
    @Suppress("PrivatePropertyName")
    private val PLAYLIST_PATTERN = Pattern.compile("^https?://(?:.*\\.)?tidal\\.com/browse/playlist/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}).*")

    @Suppress("PrivatePropertyName")
    private val ALBUM_PATTERN = Pattern.compile("^https?://(?:.*\\.)?tidal\\.com/browse/album/([0-9]+).*")

    @Suppress("PrivatePropertyName")
    private val TRACK_PATTERN = Pattern.compile("^https?://(?:.*\\.)?tidal\\.com/browse/track/([0-9]+).*")

    override fun getSourceName() = "tidal"

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
        val track = Basalt.tidal.getTrack(trackId) ?: return AudioReference.NO_TRACK

        return getYoutubeTrack(manager, track)
    }

    private fun loadPlaylist(manager: DefaultAudioPlayerManager, match: Matcher): AudioItem {
        match.matches()
        val listId: String = match.group(1)
        val playlist = Basalt.tidal.getTracksFromPlaylist(listId) ?: return AudioReference.NO_TRACK

        playlist.truncate(maxTracks)
        return handlePlaylist(manager, playlist)
    }

    private fun loadAlbum(manager: DefaultAudioPlayerManager, match: Matcher): AudioItem {
        match.matches()
        val albumId: String = match.group(1)
        val playlist = Basalt.tidal.getTracksFromAlbum(albumId) ?: return AudioReference.NO_TRACK

        playlist.truncate(maxTracks)
        return handlePlaylist(manager, playlist)
    }
}
