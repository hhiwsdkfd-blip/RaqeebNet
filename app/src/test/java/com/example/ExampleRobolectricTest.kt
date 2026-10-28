package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.raqeeb.data.model.ConnectionType
import com.example.raqeeb.data.model.NetworkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext_matchesArabicAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("رقيب نت", appName)
    }

    @Test
    fun networkStatus_byteFormatting_isCorrect() {
        assertEquals("0 ميجابايت", NetworkStatus.formatBytes(0))
        assertEquals("500 كيلوبايت", NetworkStatus.formatBytes(500 * 1024))
        assertEquals("10.0 ميجابايت", NetworkStatus.formatBytes(10L * 1024 * 1024))
        assertEquals("2.50 جيجابايت", NetworkStatus.formatBytes((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun connectionType_enumTitles_areNotEmpty() {
        ConnectionType.values().forEach { type ->
            assertTrue(type.titleArabic.isNotEmpty())
            assertTrue(type.subtitleArabic.isNotEmpty())
        }
    }
}
