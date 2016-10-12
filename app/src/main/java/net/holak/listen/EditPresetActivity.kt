package net.holak.listen

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v7.widget.Toolbar.OnMenuItemClickListener
import android.view.*
import android.widget.*
import net.holak.listen.config.Preset
import net.holak.listen.config.PresetStorage
import net.holak.listen.tts.AndroidTextToSpeech
import net.holak.listen.tts.DefaultVoice
import net.holak.listen.tts.VoiceEnumerator
import net.holak.listen.tts.VoiceInfo
import java.util.*
import kotlin.reflect.KClass

val REQUEST_ADD_SUBREDDIT = 0

val REQUEST_ADD_PRESET = 0
val REQUEST_EDIT_PRESET = 1

val InitialPreset = "InitialPreset"

class EditPresetActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null

    val presetStorage by lazy{ PresetStorage(this) }

    /** Whether a new preset is being created, rather than an existing one being edited. */
    val creatingNew by lazy{ !intent.hasExtra(ExtraPresetId) }

    /** Id of the preset being edited, null if creating a new preset. */
    val presetId by lazy{ intent.getStringExtra(ExtraPresetId) }

    val existingPreset by lazy{ if (presetId != null) presetStorage.find(presetId!!) else null }

    val fragments = listOf(
            SourceFragment::class,
            VoiceFragment::class,
            SubredditsFragment::class
    )
    val fragmentInstances = mutableMapOf<KClass<out Fragment>, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_preset)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        toolbar.setOnMenuItemClickListener(OnMenuItemClickListener { item ->
            if (item.itemId != R.id.action_save) {
                return@OnMenuItemClickListener false
            }

            savePresetFromUi();

            true
        })

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager!!)
    }

    private fun handlePresetNotFound() {
        Toast.makeText(this, "No such preset", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** Saves the preset (new or existing) based on the UI state. */
    private fun savePresetFromUi() {
        val sourceFragment = fragmentInstance(SourceFragment::class)!!
        val presetName = sourceFragment.editPresetName.text.toString()
        val threadCount = sourceFragment.editThreadCount.text.toString().toInt()
        val commentCount = sourceFragment.editCommentCount.text.toString().toInt()

        // If the fragment hasn't even been loaded yet, just use existing values if editing an existing preset,
        // or a default values if creating a new preset.
        val subreddits = fragmentInstance(SubredditsFragment::class)?.subreddits ?: existingPreset?.subreddits ?: emptyList<String>()
        val voice = fragmentInstance(VoiceFragment::class)?.voice ?: existingPreset?.voice ?: DefaultVoice

        val preset = when(creatingNew) {
            true -> Preset(presetName, voice, subreddits, threadCount, commentCount)
            false -> Preset(presetId, presetName, voice, subreddits, threadCount, commentCount)
        }
        presetStorage.store(preset)
        presetStorage.savePresets()

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_edit_preset, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    abstract class EditFragmentPreset(val layoutId: Int) : Fragment() {
        protected var editedPreset: Preset? = null

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            var rootView = inflater!!.inflate(layoutId, container, false)
            editedPreset = arguments?.getSerializable(InitialPreset) as Preset?
            return rootView
        }
    }

    class SourceFragment() : EditFragmentPreset(R.layout.fragment_preset_source) {

        companion object { @JvmStatic val fragmentName = "Source" }
        lateinit var editPresetName: EditText
        lateinit var editThreadCount: EditText
        lateinit var editCommentCount: EditText

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val rootView = super.onCreateView(inflater, container, savedInstanceState)
            editPresetName = rootView.findViewById(R.id.editPresetName) as EditText
            editPresetName.setText(editedPreset?.name ?: "", TextView.BufferType.EDITABLE)

            editThreadCount = rootView.findViewById(R.id.editThreadCount) as EditText
            editThreadCount.setText(editedPreset?.threadsPerSubreddit?.toString() ?: "1", TextView.BufferType.EDITABLE)

            editCommentCount = rootView.findViewById(R.id.editCommentCount) as EditText
            editCommentCount.setText(editedPreset?.commentsPerThread?.toString() ?: "1", TextView.BufferType.EDITABLE)

            return rootView
        }

    }

    class SubredditsFragment : EditFragmentPreset(R.layout.fragment_preset_subreddits) {

        companion object { @JvmStatic val fragmentName = "Subreddits" }

        lateinit var rootView: View

        val subreddits = mutableListOf<String>()
        val subredditsAdapter by lazy{ ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, subreddits) }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val rootView = super.onCreateView(inflater, container, savedInstanceState)

            subreddits.clear()
            subreddits.addAll(0, editedPreset?.subreddits ?: emptyList())

            val addButton = rootView.findViewById(R.id.btnAddSubreddit) as Button
            addButton.setOnClickListener {
                startActivityForResult(Intent(activity, SubredditActivity::class.java), REQUEST_ADD_SUBREDDIT)
            }

            val listSubreddits = rootView.findViewById(R.id.listSubreddits) as ListView
            listSubreddits.adapter = subredditsAdapter
            listSubreddits.onItemLongClickListener = object: AdapterView.OnItemLongClickListener {
                override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
                    subreddits.removeAt(position)
                    (listSubreddits.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                    return true
                }
            }

            return rootView
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == REQUEST_ADD_SUBREDDIT && data != null) {
                val subreddit = data.getStringExtra(SubredditActivity.SubredditName)
                if (!subreddits.contains(subreddit)) {
                    subreddits.add(subreddit)
                    subredditsAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    class VoiceFragment : EditFragmentPreset(R.layout.fragment_preset_voice) {

        companion object { @JvmStatic val fragmentName = "Voice" }
        lateinit var voice: VoiceInfo
        val allVoices = mutableListOf<VoiceInfo>()

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val rootView = super.onCreateView(inflater, container, savedInstanceState)

            voice = editedPreset?.voice ?: DefaultVoice

            val genderSpinner = rootView.findViewById(R.id.spinnerGender) as Spinner
            genderSpinner.adapter = ArrayAdapter(
                    rootView.context,
                    android.R.layout.simple_spinner_dropdown_item,
                    allVoices
            )
            VoiceEnumerator(activity).queryVoices { voices ->
                allVoices.clear();
                allVoices.addAll(voices.filter { it.voiceName.contains("en", ignoreCase = true) });
                (genderSpinner.adapter as ArrayAdapter<VoiceInfo>).notifyDataSetChanged()
                val voicePosition = allVoices.indexOfFirst { it.equals(voice) }
                genderSpinner.setSelection(if (voicePosition != -1) voicePosition else 0)
            }

            genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    throw UnsupportedOperationException()
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    voice = allVoices[position]
                }
            }

            val testButton = rootView.findViewById(R.id.buttonTestVoice) as Button
            testButton.setOnClickListener {
                AndroidTextToSpeech(activity, voice).say(testSentences.get(Random().nextInt(testSentences.size)))
            }

            return rootView
        }

    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            // getItem is called to instantiate the fragment for the given page.
            val fragment = fragments.getOrNull(position)?.java?.newInstance()!!

            val bundleWithPreset = Bundle()
            bundleWithPreset.putSerializable(InitialPreset, existingPreset)
            fragment.arguments = bundleWithPreset

            return fragment
        }

        override fun getCount(): Int = fragments.count()

        override fun getPageTitle(position: Int): CharSequence? {
            // Return the value of static field `fragmentName`
            try {
                return fragments.getOrNull(position)?.java?.getMethod("getFragmentName")?.invoke(null) as? String ?: "<missing>"
            }catch(e: NoSuchMethodException) {
                return "<missing>"
            }
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
            val fragment = super.instantiateItem(container, position) as Fragment

            val fragmentClass = fragments.getOrNull(position) ?:
                    throw IndexOutOfBoundsException("Activity only has " + fragments.count() + " fragments, but fragment with position " + position + " requested.")
            fragmentInstances.put(fragmentClass, fragment)
            return fragment
        }
    }

    private fun <T: Fragment>fragmentInstance(c: KClass<T>): T? {
        return fragmentInstances.get(c) as T?;
    }
}

val testSentences = listOf(
        "Hello world yo"
)