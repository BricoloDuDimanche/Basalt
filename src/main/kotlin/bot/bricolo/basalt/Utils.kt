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

package bot.bricolo.basalt


object Utils {
    // This function if from JukeBot by Devoxin, licensed under Apache License 2.0
    // https://github.com/Devoxin/JukeBot/blob/jda4/src/main/java/jukebot/audio/sourcemanagers/mixcloud/Utils.kt
    fun cycle(i: String): Sequence<Char> = sequence {
        var index = -1
        while (true) {
            ++index
            yield(i[index % i.length])
        }
    }

    // This function if from JukeBot by Devoxin, licensed under Apache License 2.0
    // https://github.com/Devoxin/JukeBot/blob/jda4/src/main/java/jukebot/audio/sourcemanagers/mixcloud/Utils.kt
    fun decryptXor(key: String, cipher: String): String {
        return cipher.asIterable()
                .zip(cycle(key).asIterable())
                .map { (ch, k) ->
                    (ch.toString().codePointAt(0) xor k.toString().codePointAt(0)).toChar()
                }
                .joinToString("")
    }
}
