package net.holak.listen.config

import net.holak.listen.tts.DefaultVoice
import net.holak.listen.tts.VoiceInfo
import java.io.Serializable
import java.util.*

data class Preset(
        val id: String,
        val name: String,
        val voice: VoiceInfo = DefaultVoice,
        val subreddits: List<String> = emptyList(),
        val threadsPerSubreddit: Int = 3,
        val commentsPerThread: Int = 3
) : Serializable {

    constructor(
            name: String,
            voice: VoiceInfo = DefaultVoice,
            subreddits: List<String> = mutableListOf(),
            threadsPerSubreddit: Int = 3,
            commentsPerThread: Int = 3
    ) : this(UUID.randomUUID().toString(), name, voice, subreddits, threadsPerSubreddit, commentsPerThread)

}
