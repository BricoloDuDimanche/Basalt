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

package bot.bricolo.basalt.sources.entities

abstract class BasaltPlaylist(open val name: String) {
    var tracks: MutableList<BasaltTrack> = mutableListOf()
        private set

    fun addTrack(artist: String, title: String) {
        tracks.add(BasaltTrack(artist, title))
    }

    fun truncate(size: Int) {
        tracks = tracks.subList(0, Math.min(size, tracks.size))
    }
}
