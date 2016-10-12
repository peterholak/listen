package net.holak.listen

import android.support.test.InstrumentationRegistry
import android.test.RenamingDelegatingContext

open class TestWithContext {
    val testContext = RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
}
