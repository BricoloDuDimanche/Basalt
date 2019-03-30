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
