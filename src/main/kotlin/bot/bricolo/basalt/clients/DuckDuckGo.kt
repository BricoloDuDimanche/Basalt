package bot.bricolo.basalt.clients

import bot.bricolo.basalt.createHeaders
import bot.bricolo.basalt.json
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.File
import java.util.regex.Pattern
import kotlin.math.log

class DuckDuckGo : AbstractClient() {
    @Suppress("PrivatePropertyName")
    private val DDG_VQD_REGEX = Pattern.compile(".*vqd='([^']+).*")

    fun searchVideo(query: String, manager: YoutubeAudioSourceManager): AudioTrack? {
        // STEP 1: Fetch DDG
        val response = callAPI("https://duckduckgo.com/?q=$query")?.body()?.string() ?: return null

        // STEP 2: Fetch DDG auth token
        val vqdMatcher = DDG_VQD_REGEX.matcher(response)
        if (!vqdMatcher.matches()) return null
        val vqd = vqdMatcher.group(1)

        // STEP 3: Download v.js
        val vJs = callAPI("https://duckduckgo.com/v.js?l=wt-wt&o=json&strict=1&q=$query&vqd=$vqd&f=,,,&p=1", hideUserAgent = true)?.body()?.string()
                ?: return null
        println(query)
        println(vJs)
        val searchData = JSONObject(vJs)

        // STEP 4: Decode shit
        val item = (searchData.getJSONArray("results").find { if (it is JSONObject) it.getString("provider") == "YouTube" else false }
                ?: return null) as? JSONObject ?: return null

        return toYouTubeAudioTrack(item.getString("id"), item.getString("title"), item.getString("uploader"), false, parseDuration(item.getString("duration")), manager)
    }

    private fun parseDuration(rawDuration: String): Long {
        return 0 // @todo
    }

    private fun toYouTubeAudioTrack(videoId: String, title: String, uploader: String, isStream: Boolean, duration: Long, manager: YoutubeAudioSourceManager): YoutubeAudioTrack {
        return manager.buildTrackObject(videoId, title, uploader, isStream, duration)
    }
}
