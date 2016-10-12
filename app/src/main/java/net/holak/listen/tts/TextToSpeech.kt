package net.holak.listen.tts

interface TextToSpeech {
    fun getMaxSpeechInputLength(): Int
    fun enumerateVoices(callback: (List<VoiceInfo>) -> Unit)
    fun say(thing: String, finishedCallback: ((speech: TextToSpeech) -> Unit)? = null)
    fun stop()
    fun isSpeaking(): Boolean
    fun shutdown()
}
