package net.holak.listen

import android.content.Intent
import android.database.DataSetObserver
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import net.holak.listen.config.Preset
import net.holak.listen.config.PresetStorage

class PresetListActivity : AppCompatActivity() {

    val fab by lazy{ findViewById(R.id.fab) as FloatingActionButton }
    val listPresets by lazy { findViewById(R.id.listPresets) as ListView }
    lateinit var tts: TextToSpeech
    var redditString = "Not ready yet."
    val presetAdapter by lazy{ PresetAdapter(PresetStorage(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preset_list)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        listPresets.adapter = presetAdapter
        listPresets.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val preset = parent.adapter.getItem(position) as Preset
            startActivity<PlayingActivity>(ExtraPresetId to preset.id)
        }

        fab.setOnClickListener {
            startActivityForResult<EditPresetActivity>(REQUEST_ADD_PRESET)
        }
    }

    override fun onResume() {
        super.onResume()
        presetAdapter.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_preset_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    class PresetAdapter(private val storage: PresetStorage) : ListAdapter {

        val observers = mutableListOf<DataSetObserver>()

        fun refresh() {
            storage.reload()
            observers.forEach { it.onChanged() }
        }

        override fun isEmpty(): Boolean = storage.all().isEmpty()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view = TextView(parent.context)
            view.setTextSize(24.0f)
            val pad = 20
            view.setPadding(pad, pad, pad, pad)
            view.text = storage.all().get(position).name
            return view
        }

        override fun registerDataSetObserver(observer: DataSetObserver) {
            observers.add(observer)
        }
        override fun getItemViewType(position: Int): Int = 0
        override fun getItem(position: Int): Any? = storage.all()[position]
        override fun getViewTypeCount(): Int = 1
        override fun getItemId(position: Int): Long = position.toLong()
        override fun hasStableIds(): Boolean = false
        override fun unregisterDataSetObserver(observer: DataSetObserver) {
            observers.remove(observer)
        }
        override fun getCount(): Int = storage.all().count()
        override fun isEnabled(position: Int): Boolean = true
        override fun areAllItemsEnabled(): Boolean = true

    }
}
