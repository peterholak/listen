package net.holak.listen.tts

import android.content.Context
import android.os.ConditionVariable
import android.speech.tts.TextToSpeech
import android.util.Log

// TODO: cache voices, only refresh on request
class VoiceEnumerator(val context: Context) {

    lateinit var tts: TextToSpeech
    lateinit var engines: List<TextToSpeech.EngineInfo>
    val condition = ConditionVariable()
    val voicesByEngine = mutableMapOf<String, List<String>>()
    var ready = false

    private fun requestVoices(callback: (List<VoiceInfo>) -> Unit) {
        tts = TextToSpeech(context, {
            status ->
            engines = tts.engines
            storeAllVoices(callback)
        })
    }

    private fun storeAllVoices(callback: (List<VoiceInfo>) -> Unit) {
        var remainingEngines = engines.count()
        var i = 0
        for (engine in engines) {
            var engineTts: TextToSpeech? = null
            engineTts = TextToSpeech(context, {
                try {
                    voicesByEngine.put(engine.name, engineTts!!.voices.map { it.name })
                    i++
                }catch(e: NullPointerException) {
                    Log.e("voice", "NPE on voice " + i)
                }
                remainingEngines--;
                engineTts!!.shutdown()
                tts.shutdown()
                if (remainingEngines == 0) {
                    callback(voicesByEngine.flatMap { val key = it.key; it.value.map { VoiceInfo(key, it, "") } })
                }
            }, engine.name)
        }
    }

    fun queryVoices(callback: (List<VoiceInfo>) -> Unit) {
        requestVoices(callback)
    }


}