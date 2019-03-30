package bot.bricolo.basalt.sources

/**
 * Original source from JukeBot (https://github.com/Devoxin/JukeBot), licenced under Apache-2.0
 */

import bot.bricolo.basalt.sources.entities.PornHubAudioTrack
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.FAULT
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager
import com.sedmelluq.discord.lavaplayer.track.*
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.utils.URIBuilder
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.net.URI
import java.util.regex.Pattern

class PornHubSourceManager : AbstractSourceLoader() {
    companion object {
        private val VIDEO_REGEX = Pattern.compile("^https?://(?:www|en|es|it|pt|pl|ru|jp|nl|de|cz|fr)\\.pornhub\\.com/view_video\\.php\\?viewkey=([a-zA-Z0-9]{9,15})\$")
        private val VIDEO_INFO_REGEX = Pattern.compile("var flashvars_\\d{7,9} = (\\{.+})")
        private const val VIDEO_SEARCH_PREFIX = "phsearch:"
    }

    val httpInterfaceManager: HttpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager()

    override fun getSourceName() = "pornhub"

    override fun loadItem(manager: DefaultAudioPlayerManager, reference: AudioReference): AudioItem? {
        println(reference.identifier)
        if (!VIDEO_REGEX.matcher(reference.identifier).matches() && !reference.identifier.startsWith(VIDEO_SEARCH_PREFIX))
            return null

        if (reference.identifier.startsWith(VIDEO_SEARCH_PREFIX)) {
            return searchForVideos(reference.identifier.substring(VIDEO_SEARCH_PREFIX.length).trim())
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
        return PornHubAudioTrack(trackInfo, this)
    }

    private fun loadItemOnce(reference: AudioReference): AudioItem {
        try {
            val info = getVideoInfo(reference.identifier) ?: return AudioReference.NO_TRACK

            if (info.getBoolean("video_unavailable"))
                return AudioReference.NO_TRACK

            val videoTitle = info.getString("video_title")
            val videoDuration = info.getInt("video_duration") * 1000 // PH returns seconds
            val videoUrl = info.getString("link_url")
            val matcher = VIDEO_REGEX.matcher(videoUrl)
            val videoId = if (matcher.matches()) matcher.group(1) else reference.identifier

            return buildTrackObject(videoUrl, videoId, videoTitle, "Unknown Uploader", false, videoDuration.toLong())
        } catch (e: Exception) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Loading information for a PornHub track failed.", FAULT, e)
        }
    }

    private fun searchForVideos(query: String): AudioItem {
        val uri = URIBuilder("https://www.pornhub.com/video/search").addParameter("search", query).build()

        makeHttpRequest(uri).use { res ->
            val statusCode: Int = res.statusLine.statusCode

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return AudioReference.NO_TRACK
                }
                throw IOException("Invalid status code for search response: $statusCode")
            }

            val document = Jsoup.parse(res.entity.content, Charsets.UTF_8.name(), "https://pornhub.com")
            val videos = document.select("#videoSearchResult li")

            if (videos.isEmpty())
                return AudioReference.NO_TRACK

            val tracks = mutableListOf<PornHubAudioTrack>()
            videos.forEach {
                val identifier = it.attr("_vkey")
                val title = it.selectFirst(".title").text()
                val url = it.selectFirst(".title a").absUrl("href")
                val duration = parseDuration(it.selectFirst("var.duration").text())

                tracks.add(buildTrackObject(url, identifier, title, "Unknown Uploader", false, duration))
            }

            return BasicAudioPlaylist("Search results for: $query", tracks as List<AudioTrack>?, null, true)
        }
    }

    @Throws(IOException::class)
    private fun getVideoInfo(videoURL: String): JSONObject? {
        makeHttpRequest(videoURL).use { res ->
            val statusCode = res.statusLine.statusCode

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null
                }
                throw IOException("Invalid status code for video page response: $statusCode")
            }

            val html = res.entity.content.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val match = VIDEO_INFO_REGEX.matcher(html)

            return if (match.find()) JSONObject(match.group(1)) else null
        }
    }

    private fun buildTrackObject(uri: String, identifier: String, title: String, uploader: String, isStream: Boolean, duration: Long): PornHubAudioTrack {
        return PornHubAudioTrack(AudioTrackInfo(title, uploader, duration, identifier, isStream, uri), this)
    }

    private fun parseDuration(duration: String): Long {
        val time = duration.split(":")
        val mins = time[0].toInt() * 60000
        val secs = time[1].toInt() * 1000

        return (mins + secs).toLong()
    }

    private fun makeHttpRequest(url: String): CloseableHttpResponse {
        return makeHttpRequest(HttpGet(url))
    }

    private fun makeHttpRequest(uri: URI): CloseableHttpResponse {
        return makeHttpRequest(HttpGet(uri))
    }

    private fun makeHttpRequest(request: HttpUriRequest): CloseableHttpResponse {
        return httpInterfaceManager.`interface`.use {
            it.execute(request)
        }

    }
}
