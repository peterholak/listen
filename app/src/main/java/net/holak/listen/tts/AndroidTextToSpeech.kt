package net.holak.listen.tts

import android.content.Context
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import org.jetbrains.anko.toast
import java.util.*

class AndroidTextToSpeech(
        val context: Context,
        val voice: VoiceInfo = DefaultVoice
) : TextToSpeech {

    lateinit var tts: android.speech.tts.TextToSpeech
    private val enumerator = VoiceEnumerator(context)
    private val queue = mutableListOf<String>()
    private var ready = false
    private val finishedCallbacks = mutableMapOf<String, (speech: TextToSpeech) -> Unit>()

    init {
        tts = android.speech.tts.TextToSpeech(context.applicationContext, {
            status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts.voice = tts.voices.find { it.name == voice.voiceName } ?: substituteVoice(tts, voice)
                tts.setSpeechRate(0.8f)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId != null) { finishedCallbacks[utteranceId]?.invoke(this@AndroidTextToSpeech) }
                    }

                    override fun onError(utteranceId: String?) {
                        context.toast("There was an error.")
                        say("There was an error.")
                        if (utteranceId != null) { finishedCallbacks[utteranceId]?.invoke(this@AndroidTextToSpeech) }
                    }

                    override fun onStart(utteranceId: String?) {

                    }
                })

                ready = true
                flushQueue()
            }
        }, voice.engineName) // TODO: substitute engine
    }

    override fun getMaxSpeechInputLength(): Int = android.speech.tts.TextToSpeech.getMaxSpeechInputLength()

    override fun enumerateVoices(callback: (List<VoiceInfo>) -> Unit) {
        enumerator.queryVoices(callback)
    }

    override fun say(thing: String, finishedCallback: ((speech: TextToSpeech) -> Unit)?) {
        val utteranceId = UUID.randomUUID().toString()
        if (finishedCallback != null) {
            finishedCallbacks[utteranceId] = finishedCallback
        }

        if (!ready) {
            queue.add(thing)
            return
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            val params = hashMapOf(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to utteranceId)
            tts.speak(thing, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params)
        }else {
            tts.speak(thing, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    override fun stop() {
        tts.stop()
    }

    override fun isSpeaking() = tts.isSpeaking

    override fun shutdown() {
        tts.shutdown()
    }

    /** Returns a fallback voice that will be used instead of the voice from the preset, which does not exist on the device. */
    private fun substituteVoice(tts: android.speech.tts.TextToSpeech, voice: VoiceInfo): Voice = tts.defaultVoice

    private fun flushQueue() {
        queue.forEach{ say(it) }
    }
}