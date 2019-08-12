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
