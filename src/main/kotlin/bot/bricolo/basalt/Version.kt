package bot.bricolo.basalt

@Suppress("MemberVisibilityCanBePrivate")
object Version {
    const val VERSION_MAJOR = "@VERSION_MAJOR@"
    const val VERSION_MINOR = "@VERSION_MINOR@"
    const val VERSION_REVISION = "@VERSION_REVISION@"

    val VERSION: String
    const val COMMIT = "@COMMIT@"

    init {
        VERSION = if (VERSION_MAJOR.startsWith("@")) "indev" else "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_REVISION"
    }
}
