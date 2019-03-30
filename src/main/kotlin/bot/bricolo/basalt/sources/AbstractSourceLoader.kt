package bot.bricolo.basalt.sources

import bot.bricolo.basalt.sources.entities.BasaltPlaylist
import bot.bricolo.basalt.sources.entities.BasaltTrack
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.*
import java.io.DataInput
import java.io.DataOutput

abstract class AbstractSourceLoader : AudioSourceManager {
    override fun isTrackEncodable(track: AudioTrack) = false
    override fun shutdown() {}

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {
        throw UnsupportedOperationException("Source manager may only be used to load items.")
    }

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack {
        throw UnsupportedOperationException("Source manager may only be used to load items.")
    }

    protected fun getYoutubeTrack(manager: DefaultAudioPlayerManager, track: BasaltTrack): AudioItem {
        val youtube = manager.source(YoutubeAudioSourceManager::class.java)
        val item = youtube.loadItem(manager, AudioReference("ytsearch:${track.title} ${track.artist}", null))

        return if (item is AudioPlaylist) item.tracks.first() else AudioReference.NO_TRACK
    }

    protected fun handlePlaylist(manager: DefaultAudioPlayerManager, playlist: BasaltPlaylist): AudioItem {
        if (playlist.tracks.size == 0) return AudioReference.NO_TRACK

        val tracks = mutableListOf<Pair<Int, AudioTrack>>()
        playlist.tracks
                .mapIndexed { i, t -> Pair(i, t) }
                .parallelStream()
                .map { Pair(it.first, getYoutubeTrack(manager, it.second)) }
                .forEach {
                    @Suppress("UNCHECKED_CAST")
                    if (it.second is AudioTrack) tracks.add(it as Pair<Int, AudioTrack>)
                }

        tracks.sortBy { it.first }
        return BasicAudioPlaylist(
                playlist.name,
                tracks.map { it.second },
                null,
                false
        )
    }
}
