package bot.bricolo.basalt.sources.entities

abstract class BasaltPlaylist(open val name: String) {
    val tracks: MutableList<BasaltTrack> = mutableListOf()

    fun addTrack(artist: String, title: String) {
        tracks.add(BasaltTrack(artist, title))
    }

    fun length(): Int {
        return tracks.size
    }
}
