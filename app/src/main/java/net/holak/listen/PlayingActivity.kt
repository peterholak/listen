package net.holak.listen

import android.content.Intent
import android.media.SoundPool
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.TextView
import net.dean.jraw.models.meta.Model
import net.holak.listen.config.Preset
import net.holak.listen.config.PresetStorage
import net.holak.listen.history.SqliteHistory
import net.holak.listen.preprocessing.*
import net.holak.listen.reddit.Reddit
import net.holak.listen.script.DefaultScript
import net.holak.listen.tts.AndroidTextToSpeech
import net.holak.listen.tts.TextToSpeech
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

val ExtraPresetId = "PresetId"

class PlayingActivity : AppCompatActivity() {

    val bigText by lazy{ findViewById(R.id.textPresetName) as TextView }
    val playFab by lazy{ findViewById(R.id.playButton) as FloatingActionButton }
    val nextFab by lazy{ findViewById(R.id.skipButton) as FloatingActionButton }
    val deepFab by lazy{ findViewById(R.id.intoButton) as FloatingActionButton }
    val presetStorage by lazy{ PresetStorage(this) }
    val presetId by lazy{ intent.getStringExtra(ExtraPresetId)!! }
    val script by lazy{ DefaultScript(presetStorage.find(presetId)!!, reddit, history, android.speech.tts.TextToSpeech.getMaxSpeechInputLength() ) }
    val reddit by lazy{ Reddit() }
    val history by lazy{ SqliteHistory(this) }

    var ThreadEarcon = 0
    var CommentEarcon = 0
    val sounds: SoundPool by lazy{
        val sounds = SoundPool.Builder().build()
        ThreadEarcon = sounds.load(assets.openFd("earcons/threads.wav"), 1)
        CommentEarcon = sounds.load(assets.openFd("earcons/comments.wav"), 1)
        return@lazy sounds
    }

    val transformers = listOf(
            RedditFormatting(),
            GenderAgeBrackets(),
            RemoveExtraSlashForSubreddits(),
            LinksSummary(),
            CommonAbbreviations()
    )

    // Cannot simply be lazy, because we don't want to needlessly initialize it in onDestroy
    var player: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playing)

        bigText.text = preset().name

        playFab.setOnClickListener {
            if (player().isSpeaking()) {
                playFab.setImageDrawable(resources.getDrawable(android.R.drawable.ic_media_play, theme))
                return@setOnClickListener player().stop()
            }
            sayNext()
        }

        nextFab.setOnClickListener {
            if (!player().isSpeaking())
                return@setOnClickListener

            player().stop()
            sayNext()
        }

        deepFab.setOnClickListener {
            val listened = history.listListened()
            toast(listened.joinToString(", "))
        }
    }

    private fun sayNext() {
        if (script.hasNext()) {
            playFab.setImageDrawable(resources.getDrawable(android.R.drawable.ic_menu_upload, theme))
            doAsync {
                val text = transformers.fold(script.next(), { str, transformer -> transformer.transform(str) })
                uiThread {
                    playFab.setImageDrawable(resources.getDrawable(android.R.drawable.ic_media_pause, theme))
                    if (script.currentType == Model.Kind.LINK) {
                        sounds.play(ThreadEarcon, 1.0f, 1.0f, 1, 0, 1.0f)
                    }else if (script.currentType == Model.Kind.COMMENT){
                        sounds.play(CommentEarcon, 1.0f, 1.0f, 1, 0, 1.0f)
                    }
                    player().say(text, { runOnUiThread { sayNext() } })
                }
            }
        }else{
            playFab.setImageDrawable(resources.getDrawable(android.R.drawable.ic_media_play, theme))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_playing, menu)
        menu.findItem(R.id.action_edit).setOnMenuItemClickListener {
            startActivityForResult<EditPresetActivity>(REQUEST_EDIT_PRESET, ExtraPresetId to presetId)
            true
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_PRESET) {
            // TODO: reinitialize tts if voice has changed
            presetStorage.reload()
            bigText.text = preset().name
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.shutdown()
    }

    // This is rather wasteful
    private fun preset(): Preset = presetStorage.find(presetId)!!

    private fun player(): TextToSpeech {
        if (player == null) {
            player = AndroidTextToSpeech(this, preset().voice)
        }
        return player!!
    }
}
