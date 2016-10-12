package net.holak.listen.config

import android.content.Context
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import net.holak.listen.config.Preset
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class PresetStorage(context: Context) {

    private val context = context.applicationContext;

    private val filePath = this.context.filesDir.absolutePath + "/" + fileName

    companion object {
        private val fileName = "presets.json"
        fun presetFilePath(context: Context): String {
            return context.filesDir.absolutePath + "/" + fileName
        }
    }

    private var presets: MutableList<Preset>

    init {
        presets = reload()
    }

    fun reload(): MutableList<Preset> {
        if (!File(filePath).exists()) {
            presets = mutableListOf()
        }else {
            val reader = FileReader(filePath)
            // TODO: enforce schema
            presets = Gson().fromJson(reader)
        }
        return presets
    }

    fun savePresets() {
        val writer = FileWriter(filePath)
        writer.write(Gson().toJson(presets))
        writer.close()
    }

    fun all(): List<Preset> = presets
    fun find(id: String): Preset? = presets.find { it.id == id }

    fun store(p: Preset) {
        val existing = presets.indexOfFirst { it.id == p.id }
        if (existing != -1) {
            presets[existing] = p
        }else{
            presets.add(p)
        }
    }
}