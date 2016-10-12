package net.holak.listen.tts

import java.io.Serializable

data class VoiceInfo(
        val engineName: String,
        val voiceName: String,
        val language: String
) : Serializable {
    override fun toString(): String = engineName + ": " + voiceName + " (" + language + ")"
}

val DefaultVoice = VoiceInfo("", "", "")
