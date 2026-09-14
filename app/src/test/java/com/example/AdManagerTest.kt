package com.example

import com.example.ads.AdManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdManagerTest {

    @Test
    fun adManager_configuredWithCorrectAdMobIds() {
        assertEquals("ca-app-pub-8212461864193378~9820987959", AdManager.ADMOB_APP_ID)
        assertEquals("ca-app-pub-8212461864193378/8050409587", AdManager.PROD_INTERSTITIAL_ID)
        assertEquals("ca-app-pub-8212461864193378/7680437595", AdManager.PROD_REWARDED_ID)

        assertTrue(AdManager.ADMOB_APP_ID.startsWith("ca-app-pub-"))
        assertTrue(AdManager.PROD_INTERSTITIAL_ID.startsWith("ca-app-pub-"))
        assertTrue(AdManager.PROD_REWARDED_ID.startsWith("ca-app-pub-"))
    }
}
