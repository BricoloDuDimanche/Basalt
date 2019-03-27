package bot.bricolo.basalt.sources

import bot.bricolo.basalt.Basalt
import bot.bricolo.basalt.clients.entities.BasaltPlaylist
import bot.bricolo.basalt.clients.entities.BasaltTrack
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.*
import java.io.DataInput
import java.io.DataOutput

abstract class AbstractSourceLoader : AudioSourceManager {
    override fun isTrackEncodable(track: AudioTrack?) = false
    override fun shutdown() {}

    override fun encodeTrack(track: AudioTrack?, output: DataOutput?) = throw UnsupportedOperationException("Source manager may only be used to load items.")
    override fun decodeTrack(trackInfo: AudioTrackInfo?, input: DataInput?) = throw UnsupportedOperationException("Source manager may only be used to load items.")

    protected fun getYoutubeTrack(manager: DefaultAudioPlayerManager, track: BasaltTrack): AudioItem {
        val youtube = manager.source(YoutubeAudioSourceManager::class.java)
        val result = Basalt.ddg.searchVideo("${track.title} ${track.artist}", youtube)
        return result ?: AudioReference.NO_TRACK
    }

    // @todo: Parallel!!!
    protected fun handlePlaylist(manager: DefaultAudioPlayerManager, playlist: BasaltPlaylist): AudioItem {
        if (playlist.tracks.size == 0) return AudioReference.NO_TRACK

        val tracks = mutableListOf<AudioTrack>()
        playlist.tracks.map { getYoutubeTrack(manager, it) }.forEach { if (it is AudioTrack) tracks.add(it) }
        return BasicAudioPlaylist(
                playlist.name,
                tracks,
                null,
                false
        )
    }
}
