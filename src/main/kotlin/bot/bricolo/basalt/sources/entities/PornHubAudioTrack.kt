package bot.bricolo.basalt.sources.entities

import bot.bricolo.basalt.Basalt
import bot.bricolo.basalt.sources.PornHubSourceManager
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor
import org.json.JSONObject
import java.net.URI
import java.util.regex.Pattern

class PornHubAudioTrack(trackInfo: AudioTrackInfo, private val sourceManager: PornHubSourceManager) : DelegatedAudioTrack(trackInfo) {

    override fun makeClone(): AudioTrack {
        return PornHubAudioTrack(trackInfo, sourceManager)
    }

    override fun getSourceManager(): AudioSourceManager {
        return sourceManager
    }

    @Throws(Exception::class)
    override fun process(localExecutor: LocalAudioTrackExecutor) {
        val playbackUrl = getPlaybackUrl() ?: throw Exception("no playback url found")
        sourceManager.httpInterfaceManager.`interface`.use {
            PersistentHttpStream(it, URI(playbackUrl), Long.MAX_VALUE).use { stream ->
                processDelegate(MpegAudioTrack(trackInfo, stream), localExecutor)
            }
        }
    }

    private fun getPlaybackUrl(): String? {
        val info = getPageConfig()
                ?: throw FriendlyException("This track is unplayable", FriendlyException.Severity.SUSPICIOUS, null)

        return (info.getJSONArray("mediaDefinitions").filter {
            if (it !is JSONObject) return@filter false
            return@filter it.getString("videoUrl") != ""
        }.first() as? JSONObject)?.getString("videoUrl")
    }

    private fun getPageConfig(): JSONObject? {
        val response = Basalt.HTTP.get(trackInfo.uri).block() ?: return null
        if (response.code() != 200) {
            return null
        }

        val html = response.body()?.string() ?: return null
        val match = VIDEO_INFO_REGEX.matcher(html)

        return if (match.find()) JSONObject(match.group(1)) else null
    }

    companion object {
        private val VIDEO_INFO_REGEX = Pattern.compile("var flashvars_\\d{7,9} = (\\{.+})")
    }
}
