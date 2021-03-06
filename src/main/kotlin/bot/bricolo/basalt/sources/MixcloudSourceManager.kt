/*
   Copyright 2019 Devoxin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package bot.bricolo.basalt.sources

import bot.bricolo.basalt.Utils
import bot.bricolo.basalt.sources.entities.MixcloudAudioTrack
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager
import com.sedmelluq.discord.lavaplayer.track.*
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

class MixcloudSourceManager : AbstractSourceLoader() {
    companion object {
        private val URL_REGEX = Pattern.compile("https?://(?:(?:www|beta|m)\\.)?mixcloud\\.com/([^/]+)/(?!stream|uploads|favorites|listens|playlists)([^/]+)/?")
        private val JSON_REGEX = Pattern.compile("<script id=\"relay-data\" type=\"text/x-mixcloud\">([^<]+)</script>")
        private val JS_REGEX = Pattern.compile("<script[^>]+src=\"(https://(?:www\\.)?mixcloud\\.com/media/(?:js2/www_js_4|js/www)\\.[^>]+\\.js)")
        private val KEY_REGEX = Pattern.compile("\\{return *?[\"']([^\"']+)[\"']\\.concat\\([\"']([^\"']+)[\"']\\)}")
    }

    val httpInterfaceManager: HttpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager()

    override fun getSourceName() = "mixcloud"

    override fun loadItem(manager: DefaultAudioPlayerManager, reference: AudioReference): AudioItem? {
        val matcher = URL_REGEX.matcher(reference.identifier)
        if (!matcher.matches()) {
            return null
        }

        return try {
            loadItemOnce(reference)
        } catch (exception: FriendlyException) {
            // In case of a connection reset exception, try once more.
            if (HttpClientTools.isRetriableNetworkException(exception.cause)) {
                loadItemOnce(reference)
            } else {
                throw exception
            }
        }
    }

    override fun isTrackEncodable(track: AudioTrack) = true

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {}

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack {
        return MixcloudAudioTrack(trackInfo, this)
    }

    private fun loadItemOnce(reference: AudioReference): AudioItem {
        try {
            val trackInfo = getTrackInfo(reference.identifier) ?: return AudioReference.NO_TRACK

            if (!trackInfo.get("isPlayable").`as`(Boolean::class.java)) {
                throw FriendlyException(trackInfo.get("restrictedReason").text(), FriendlyException.Severity.COMMON, null)
            }

            val url = reference.identifier
            val id = trackInfo.get("slug").text()
            val title = trackInfo.get("name").text()
            val duration = trackInfo.get("audioLength").`as`(Long::class.java) * 1000
            val uploader = trackInfo.get("owner").get("displayName").text()

            return buildTrackObject(url, id, title, uploader, false, duration)
        } catch (e: Exception) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Loading information for a MixCloud track failed.", FriendlyException.Severity.FAULT, e)
        }
    }

    fun getTrackInfo(uri: String): JsonBrowser? {
        makeHttpRequest(HttpGet(uri)).use {
            val statusCode = it.statusLine.statusCode
            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null
                }
                throw IOException("Invalid status code for Mixcloud track page response: $statusCode")
            }

            val content = IOUtils.toString(it.entity.content, StandardCharsets.UTF_8)
            if (content.contains("m-play-info")) { // legacy
                // TO_DO
            }

            val matcher = JSON_REGEX.matcher(content)

            if (matcher.find()) {
                val json = JsonBrowser.parse(matcher.group(1).replace("&quot;", "\""))
                for (node in json.values()) {
                    val info = node.safeGet("cloudcast").safeGet("data").safeGet("cloudcastLookup")
                    if (!info.isNull && !info.get("streamInfo").isNull) {
                        val jsMatcher = JS_REGEX.matcher(content)
                        if (jsMatcher.find()) {
                            info.put("jsUrl", jsMatcher.group(1))
                        }
                        return info
                    }
                }
                throw IllegalStateException("Missing cloudcast.data.cloudcastLookup node")
            }
            throw IllegalStateException("Missing Mixcloud track JSON")
        }
    }

    fun getStreamKey(json: JsonBrowser): String {
        if (json.get("jsUrl").isNull) {
            throw IllegalStateException("jsUrl is missing from json")
        }

        makeHttpRequest(HttpGet(json.get("jsUrl").text())).use {
            val statusCode = it.statusLine.statusCode

            if (statusCode != 200) {
                throw IllegalStateException("Invalid status code while fetching JS")
            }

            val content = IOUtils.toString(it.entity.content, StandardCharsets.UTF_8)
            val keyMatcher = KEY_REGEX.matcher(content)

            if (keyMatcher.find()) {
                return keyMatcher.group(1) + keyMatcher.group(2)
            }

            throw IllegalStateException("Missing key in JS")
        }
    }

    fun decodeUrl(key: String, url: String): String {
        val xorUrl = String(Base64.getDecoder().decode(url))
        return Utils.decryptXor(key, xorUrl)
    }

    @Suppress("SameParameterValue")
    private fun buildTrackObject(uri: String, identifier: String, title: String, uploader: String, isStream: Boolean, duration: Long): MixcloudAudioTrack {
        return MixcloudAudioTrack(AudioTrackInfo(title, uploader, duration, identifier, isStream, uri), this)
    }

    private fun makeHttpRequest(request: HttpUriRequest): CloseableHttpResponse {
        return httpInterfaceManager.`interface`.use {
            it.execute(request)
        }
    }
}
