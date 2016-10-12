package net.holak.listen

import org.junit.runner.RunWith
import org.junit.Test
import android.support.test.runner.AndroidJUnit4
import net.holak.listen.config.Preset
import net.holak.listen.config.PresetStorage
import org.junit.Assert
import java.io.File

@RunWith(AndroidJUnit4::class)
class PresetStorageTest : TestWithContext() {

    @Test
    fun saveAndLoadPresets() {
        File(PresetStorage.presetFilePath(testContext)).delete()
        val presetStorage = PresetStorage(testContext)
        presetStorage.store(Preset("Hello", subreddits = listOf("AskReddit", "news")))
        presetStorage.savePresets()

        val secondStorage = PresetStorage(testContext)
        Assert.assertEquals(1, secondStorage.all().count())
        Assert.assertEquals("Hello", secondStorage.all().get(0).name)
        Assert.assertTrue(listOf("AskReddit", "news") == secondStorage.all().get(0).subreddits)
    }
}