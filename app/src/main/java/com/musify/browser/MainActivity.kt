package com.musify.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.musify.browser.downloads.LocalDownloadRepository
import com.musify.browser.downloads.MusifyDownloadManager
import com.musify.browser.permissions.LocalSitePermissionRepository
import com.musify.browser.storage.LocalAppPreferencesRepository
import com.musify.browser.storage.LocalBookmarkRepository
import com.musify.browser.storage.LocalHistoryRepository
import com.musify.browser.tabs.FileTabSessionStore
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.screens.BrowserShellScreen
import com.musify.browser.ui.screens.DataControlScreen
import com.musify.browser.ui.screens.DownloadsScreen
import com.musify.browser.ui.screens.EngineSpikeScreen
import com.musify.browser.filterlist.FilterEngine
import com.musify.browser.filterlist.FilterListRegistry
import com.musify.browser.filterlist.FilterListStorage
import com.musify.browser.filterlist.FilterListUpdater
import com.musify.browser.ui.screens.FilterListScreen
import com.musify.browser.ui.screens.HistoryBookmarksScreen
import com.musify.browser.ui.screens.LandingScreen
import com.musify.browser.ui.screens.OnboardingDialog
import com.musify.browser.ui.screens.ReleaseInfoScreen
import com.musify.browser.ui.screens.SitePermissionsScreen
import com.musify.browser.ui.screens.WelcomeMatrixScreen
import com.musify.browser.ui.theme.MusifyTheme
import java.io.File

enum class ScreenState {
    BROWSER,
    HISTORY_BOOKMARKS,
    DATA_CONTROL,
    FILTER_LISTS,
    DOWNLOADS,
    SITE_PERMISSIONS,
    RELEASE_INFO,
    LANDING,
    SPIKE,
    WELCOME_MATRIX
}

class MainActivity : ComponentActivity() {

    private var activeTabsViewModel: TabsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionFile = File(filesDir, "musify_tabs_session.json")
        val sessionStore = FileTabSessionStore(sessionFile)

        val historyFile = File(filesDir, "musify_history.json")
        val historyRepository = LocalHistoryRepository(historyFile)

        val bookmarksFile = File(filesDir, "musify_bookmarks.json")
        val bookmarkRepository = LocalBookmarkRepository(bookmarksFile)

        val shieldExceptionsFile = File(filesDir, "musify_shield_exceptions.txt")
        val shieldExceptionRepository = com.musify.browser.shield.LocalShieldExceptionRepository(shieldExceptionsFile)

        val filtersDir = File(filesDir, "filters")
        val filterStorage = FilterListStorage(filtersDir)
        val filterEngine = FilterEngine(filterStorage)
        val filterUpdater = FilterListUpdater(filterStorage)

        val downloadsFile = File(filesDir, "musify_downloads.json")
        val downloadRepository = LocalDownloadRepository(downloadsFile)
        val downloadManager = MusifyDownloadManager(this, downloadRepository, lifecycleScope)

        val sitePermissionsFile = File(filesDir, "musify_site_permissions.json")
        val sitePermissionRepository = LocalSitePermissionRepository(sitePermissionsFile)

        val preferencesFile = File(filesDir, "musify_preferences.json")
        val preferencesRepository = LocalAppPreferencesRepository(preferencesFile)

        setContent {
            MusifyTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.WELCOME_MATRIX) }
                var hasEnteredBrowser by remember { mutableStateOf(false) }
                var historyBookmarksTab by remember { mutableIntStateOf(0) }
                var showOnboarding by remember { mutableStateOf(!preferencesRepository.hasCompletedOnboarding()) }

                val tabsViewModel = remember {
                    TabsViewModel(
                        store = sessionStore,
                        historyRepository = historyRepository,
                        bookmarkRepository = bookmarkRepository,
                        shieldExceptionRepository = shieldExceptionRepository,
                        downloadManager = downloadManager,
                        sitePermissionRepository = sitePermissionRepository
                    )
                }
                activeTabsViewModel = tabsViewModel

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    FilterListRegistry.initializeBundledSeed(applicationContext, filterStorage, filterEngine)
                }

                androidx.compose.runtime.LaunchedEffect(intent) {
                    if (intent?.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank()) {
                        hasEnteredBrowser = true
                        currentScreen = ScreenState.BROWSER
                    }
                    handleExternalIntent(intent, tabsViewModel)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        ScreenState.BROWSER -> {
                            BrowserShellScreen(
                                viewModel = tabsViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onOpenBookmarks = {
                                    historyBookmarksTab = 0
                                    currentScreen = ScreenState.HISTORY_BOOKMARKS
                                },
                                onOpenHistory = {
                                    historyBookmarksTab = 1
                                    currentScreen = ScreenState.HISTORY_BOOKMARKS
                                },
                                onOpenDataControl = {
                                    currentScreen = ScreenState.DATA_CONTROL
                                },
                                onOpenFilterLists = {
                                    currentScreen = ScreenState.FILTER_LISTS
                                },
                                onOpenDownloads = {
                                    currentScreen = ScreenState.DOWNLOADS
                                },
                                onOpenSitePermissions = {
                                    currentScreen = ScreenState.SITE_PERMISSIONS
                                },
                                onOpenReleaseInfo = {
                                    currentScreen = ScreenState.RELEASE_INFO
                                },
                                onOpenSpikeDebug = { currentScreen = ScreenState.SPIKE },
                                onOpenRoadmap = { currentScreen = ScreenState.LANDING },
                                onOpenWelcome = { currentScreen = ScreenState.WELCOME_MATRIX }
                            )
                        }
                        ScreenState.HISTORY_BOOKMARKS -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            HistoryBookmarksScreen(
                                historyRepository = historyRepository,
                                bookmarkRepository = bookmarkRepository,
                                initialTab = historyBookmarksTab,
                                modifier = Modifier.padding(innerPadding),
                                onNavigateUrl = { targetUrl ->
                                    tabsViewModel.navigateActiveTab(targetUrl)
                                    currentScreen = ScreenState.BROWSER
                                },
                                onClose = { currentScreen = ScreenState.BROWSER }
                            )
                        }
                        ScreenState.DATA_CONTROL -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            DataControlScreen(
                                viewModel = tabsViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onClose = { currentScreen = ScreenState.BROWSER }
                            )
                        }
                        ScreenState.FILTER_LISTS -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            FilterListScreen(
                                filterEngine = filterEngine,
                                filterUpdater = filterUpdater,
                                modifier = Modifier.padding(innerPadding),
                                onClose = { currentScreen = ScreenState.BROWSER }
                            )
                        }
                        ScreenState.DOWNLOADS -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            DownloadsScreen(
                                downloadManager = downloadManager,
                                modifier = Modifier.padding(innerPadding),
                                onClose = { currentScreen = ScreenState.BROWSER }
                            )
                        }
                        ScreenState.SITE_PERMISSIONS -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            SitePermissionsScreen(
                                repository = sitePermissionRepository,
                                modifier = Modifier.padding(innerPadding),
                                onClose = { currentScreen = ScreenState.BROWSER }
                            )
                        }
                        ScreenState.RELEASE_INFO -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            ReleaseInfoScreen(
                                modifier = Modifier.padding(innerPadding),
                                onClose = { currentScreen = ScreenState.BROWSER }
                            )
                        }
                        ScreenState.LANDING -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            LandingScreen(
                                modifier = Modifier.padding(innerPadding),
                                onOpenBrowser = { currentScreen = ScreenState.BROWSER },
                                onLaunchSpike = { currentScreen = ScreenState.SPIKE }
                            )
                        }
                        ScreenState.SPIKE -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            EngineSpikeScreen(modifier = Modifier.padding(innerPadding))
                        }
                        ScreenState.WELCOME_MATRIX -> {
                            BackHandler(enabled = hasEnteredBrowser) {
                                currentScreen = ScreenState.BROWSER
                            }
                            WelcomeMatrixScreen(
                                onSkipMatrix = {
                                    hasEnteredBrowser = true
                                    currentScreen = ScreenState.BROWSER
                                }
                            )
                        }
                    }
                }

                if (showOnboarding) {
                    OnboardingDialog(
                        onDismiss = {
                            showOnboarding = false
                            preferencesRepository.setCompletedOnboarding(true)
                        },
                        onComplete = {
                            showOnboarding = false
                            preferencesRepository.setCompletedOnboarding(true)
                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activeTabsViewModel?.persistSession()
    }

    override fun onStop() {
        super.onStop()
        activeTabsViewModel?.persistSession()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        activeTabsViewModel?.let { handleExternalIntent(intent, it) }
    }

    private fun handleExternalIntent(intent: Intent?, viewModel: TabsViewModel) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val dataStr = intent.dataString
            if (!dataStr.isNullOrBlank()) {
                viewModel.createNewTab(url = dataStr, selectImmediately = true)
            }
        }
    }
}
