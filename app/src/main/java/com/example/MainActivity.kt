package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.DhakaDateUtils
import com.example.data.model.FinTransaction
import com.example.data.model.MoneyUtils
import com.example.data.model.NavSection
import com.example.data.repository.LocalPersonalBmStore
import com.example.data.repository.PersonalBmRepository
import com.example.ui.components.AddEditTransactionModal
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.UserAvatarBadge
import com.example.ui.screens.AccountsAndWalletsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.CategoryTypedModuleScreen
import com.example.ui.screens.FinancialCalendarScreen
import com.example.ui.screens.LifeEventsScreen
import com.example.ui.screens.LoansAndDebtsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.RecurringTransactionsScreen
import com.example.ui.screens.ReportsAndExportsScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.onGoogleSignInClicked
import com.example.ui.screens.signOut
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PersonalBMTheme
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueSoft
import com.example.ui.theme.isEffectiveDarkTheme
import com.example.ui.viewmodel.PersonalBmUiState
import com.example.ui.viewmodel.PersonalBmViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    var currentUser by remember {
        mutableStateOf(
            try {
                Firebase.auth.currentUser
            } catch (_: Exception) {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        try {
            Firebase.auth.addAuthStateListener(listener)
        } catch (_: Exception) {
            // Ignore if Firebase auth is offline
        }
        onDispose {
            try {
                Firebase.auth.removeAuthStateListener(listener)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    val effectiveUid = currentUser?.uid ?: LocalPersonalBmStore.DEFAULT_LOCAL_USER_ID
    val effectiveName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Guest"
    val effectiveEmail = currentUser?.email?.takeIf { it.isNotBlank() } ?: "guest@personalbm.app"
    val effectivePhotoUrl = currentUser?.photoUrl?.toString().orEmpty()

    key(effectiveUid) {
        val vm: PersonalBmViewModel = viewModel(
            key = effectiveUid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) as Application
                    PersonalBmViewModel(
                        repository = PersonalBmRepository(app),
                        currentUserId = effectiveUid,
                        initialDisplayName = effectiveName,
                        initialEmail = effectiveEmail,
                        initialPhotoUrl = effectivePhotoUrl
                    )
                }
            }
        )
        PersonalBmAppShell(
            viewModel = vm,
            isCloudSignedIn = currentUser != null,
            onAuthStateUpdated = {
                currentUser = try {
                    Firebase.auth.currentUser
                } catch (_: Exception) {
                    null
                }
            }
        )
    }
}

fun iconForNavSection(section: NavSection): ImageVector = when (section) {
    NavSection.OVERVIEW -> Icons.Default.Dashboard
    NavSection.EVENTS -> Icons.Default.Celebration
    NavSection.TRANSACTIONS -> Icons.AutoMirrored.Filled.ListAlt
    NavSection.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
    NavSection.EXPENSES -> Icons.AutoMirrored.Filled.TrendingDown
    NavSection.ACCOUNTS -> Icons.Default.AccountBalanceWallet
    NavSection.ANALYTICS -> Icons.Default.Analytics
    NavSection.BUDGETS -> Icons.Default.PieChart
    NavSection.SAVINGS_GOALS -> Icons.Default.Savings
    NavSection.RECURRING -> Icons.Default.Repeat
    NavSection.LOANS -> Icons.Default.AccountBalance
    NavSection.CALENDAR -> Icons.Default.CalendarMonth
    NavSection.REPORTS -> Icons.Default.Description
    NavSection.SETTINGS -> Icons.Default.Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalBmAppShell(
    viewModel: PersonalBmViewModel,
    isCloudSignedIn: Boolean = false,
    onAuthStateUpdated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showTransactionModal by remember { mutableStateOf(false) }
    var modalDefaultType by remember { mutableStateOf("EXPENSE") }
    var modalDefaultDateEpochDay by remember { mutableStateOf<Long?>(null) }
    var editingTransaction by remember { mutableStateOf<FinTransaction?>(null) }
    var deletingTransaction by remember { mutableStateOf<FinTransaction?>(null) }
    var showMoreDrawerSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showProfileAccountSheet by remember { mutableStateOf(false) }
    var showGoogleSignInSheet by remember { mutableStateOf(false) }
    var sidebarCollapsed by remember { mutableStateOf(false) }

    // BackHandler on secondary sections returns to Overview
    if (uiState.currentSection != NavSection.OVERVIEW) {
        BackHandler {
            viewModel.navigateTo(NavSection.OVERVIEW)
        }
    }

    LaunchedEffect(uiState.snackbarMessage, uiState.errorMessage) {
        val msg = uiState.errorMessage ?: uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Calculate active notifications (budget alerts >= 75% + active upcoming bills)
    val currentMonthKey = uiState.selectedMonthKey.ifBlank { DhakaDateUtils.currentMonthKey() }
    val activeAlertsCount = remember(uiState.budgets, uiState.transactions, uiState.recurringTransactions, currentMonthKey) {
        val monthTxs = uiState.transactions.filter {
            it.type == "EXPENSE" && DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey
        }
        val totalExp = monthTxs.sumOf { it.amountPaisa }
        val budgetWarnings = uiState.budgets.count { b ->
            b.monthKey == currentMonthKey && b.limitPaisa > 0L && run {
                val spent = if (b.categoryId == "ALL") totalExp else monthTxs.filter { it.categoryId == b.categoryId }.sumOf { it.amountPaisa }
                (spent * 100.0 / b.limitPaisa) >= 75.0
            }
        }
        val dueRecurring = uiState.recurringTransactions.count { it.isActive }
        budgetWarnings + dueRecurring
    }

    val isDarkEffective = isEffectiveDarkTheme(uiState.settings.themeMode)

    AppTheme(themeMode = uiState.settings.themeMode, darkTheme = isDarkEffective) {
        if (showTransactionModal) {
            AddEditTransactionModal(
                initialTransaction = editingTransaction,
                defaultType = modalDefaultType,
                defaultDateEpochDay = modalDefaultDateEpochDay,
                accounts = uiState.accounts,
                categories = uiState.categories,
                defaultAccountId = uiState.settings.defaultAccountId,
                currencySymbol = uiState.settings.currencySymbol.ifBlank { "৳" },
                onDismiss = {
                    showTransactionModal = false
                    editingTransaction = null
                    modalDefaultDateEpochDay = null
                },
                onSave = { tx, isEdit ->
                    viewModel.saveTransaction(tx, isEdit)
                }
            )
        }

        if (showGoogleSignInSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGoogleSignInSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    AuthScreen(
                        onAuthSuccess = {
                            showGoogleSignInSheet = false
                            showProfileAccountSheet = false
                            onAuthStateUpdated()
                        }
                    )
                }
            }
        }

        if (showProfileAccountSheet) {
            ProfileAndCloudAccountSheet(
                uiState = uiState,
                isCloudSignedIn = isCloudSignedIn,
                onDismiss = { showProfileAccountSheet = false },
                onGoogleLoginClick = {
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            showProfileAccountSheet = false
                            onAuthStateUpdated()
                        },
                        onAuthError = {
                            showProfileAccountSheet = false
                            showGoogleSignInSheet = true
                        },
                        scope = scope,
                        onAuthCancelled = {}
                    )
                },
                onSyncToCloud = {
                    viewModel.syncWithFirebaseCloud()
                },
                onSignOutToGuest = {
                    signOut(
                        credentialManager = credentialManager,
                        scope = scope,
                        onSignOutComplete = {
                            showProfileAccountSheet = false
                            onAuthStateUpdated()
                        }
                    )
                },
                onUpdateUserProfile = { updatedSettings, newPhotoUrl, isCustomPhoto ->
                    viewModel.updateUserProfile(updatedSettings, newPhotoUrl, isCustomPhoto)
                },
                onOpenFullSettings = {
                    showProfileAccountSheet = false
                    viewModel.navigateTo(NavSection.SETTINGS)
                }
            )
        }

        if (deletingTransaction != null) {
            ConfirmDeleteDialog(
                title = "Delete Transaction?",
                message = "Deleting \"${deletingTransaction!!.title}\" will automatically reverse its balance impact on ${deletingTransaction!!.accountName}.",
                onConfirm = {
                    viewModel.deleteTransaction(deletingTransaction!!.id)
                    deletingTransaction = null
                },
                onDismiss = { deletingTransaction = null }
            )
        }

        if (showNotificationsSheet) {
            NotificationsAndAlertsSheet(
                uiState = uiState,
                onDismiss = { showNotificationsSheet = false },
                onNavigate = { sec ->
                    viewModel.navigateTo(sec)
                    showNotificationsSheet = false
                }
            )
        }

        if (showMoreDrawerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMoreDrawerSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Personal BM — All Modules",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    NavSection.entries.forEach { section ->
                        val active = uiState.currentSection == section
                        Surface(
                            color = if (active) RoyalBlue.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.navigateTo(section)
                                    showMoreDrawerSheet = false
                                }
                                .testTag("nav_drawer_${section.route}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = iconForNavSection(section),
                                    contentDescription = section.labelEn,
                                    tint = if (active) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = section.label(uiState.settings.language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (active) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletOrDesktop = maxWidth >= 640.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    // Prominent Floating + Button for adding transactions directly
                    FloatingActionButton(
                        onClick = {
                            editingTransaction = null
                            modalDefaultType = "EXPENSE"
                            modalDefaultDateEpochDay = null
                            showTransactionModal = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = RoyalBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_transaction")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                },
                bottomBar = {
                    if (!isTabletOrDesktop) {
                        // Mobile Bottom Navigation: Home | Transactions | Events | Analytics | More
                        val mobileTabs = listOf(
                            NavSection.OVERVIEW to (if (uiState.settings.language == "bn") "হোম" else "Home"),
                            NavSection.TRANSACTIONS to (if (uiState.settings.language == "bn") "লেনদেন" else "Ledger"),
                            NavSection.EVENTS to (if (uiState.settings.language == "bn") "ইভেন্ট" else "Events"),
                            NavSection.ANALYTICS to (if (uiState.settings.language == "bn") "চার্ট" else "Charts")
                        )
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                mobileTabs.forEach { (section, tabLabel) ->
                                    val selected = uiState.currentSection == section
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.navigateTo(section) },
                                        icon = {
                                            Icon(
                                                imageVector = iconForNavSection(section),
                                                contentDescription = tabLabel
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tabLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RoyalBlue,
                                            selectedTextColor = RoyalBlue,
                                            indicatorColor = RoyalBlue.copy(alpha = 0.14f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("bottom_nav_${section.route}")
                                    )
                                }
                                val isSecondaryActive = mobileTabs.none { it.first == uiState.currentSection }
                                NavigationBarItem(
                                    selected = isSecondaryActive,
                                    onClick = { showMoreDrawerSheet = true },
                                    icon = { Icon(Icons.Default.Menu, contentDescription = "More Modules") },
                                    label = {
                                        Text(
                                            text = if (uiState.settings.language == "bn") "আরও" else "More",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = RoyalBlue,
                                        selectedTextColor = RoyalBlue,
                                        indicatorColor = RoyalBlue.copy(alpha = 0.14f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("bottom_nav_more")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Minimal Light Collapsible / Fixed Left Sidebar on Tablet & Desktop
                    if (isTabletOrDesktop) {
                        MinimalLightSidebar(
                            currentSection = uiState.currentSection,
                            language = uiState.settings.language,
                            collapsed = sidebarCollapsed,
                            onToggleCollapse = { sidebarCollapsed = !sidebarCollapsed },
                            onSelect = { viewModel.navigateTo(it) }
                        )
                        VerticalDivider(color = MaterialTheme.colorScheme.outline)
                    }

                    // Main Content Column with Adaptive Top Bar / Mobile Greeting Header
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (isTabletOrDesktop) {
                            DesktopTopNavBar(
                                uiState = uiState,
                                isDarkTheme = isDarkEffective,
                                activeAlertsCount = activeAlertsCount,
                                onSearchQueryChange = { q ->
                                    viewModel.updateTransactionFilter { it.copy(searchQuery = q, currentPage = 1) }
                                    if (q.isNotBlank() && uiState.currentSection != NavSection.TRANSACTIONS) {
                                        viewModel.navigateTo(NavSection.TRANSACTIONS)
                                    }
                                },
                                onMonthChange = { viewModel.setSelectedMonthKey(it) },
                                onNotificationsClick = { showNotificationsSheet = true },
                                onToggleTheme = {
                                    val nextTheme = if (isDarkEffective) "light" else "dark"
                                    viewModel.updateSettings(uiState.settings.copy(themeMode = nextTheme))
                                },
                                onProfileClick = { showProfileAccountSheet = true }
                            )
                        } else {
                            MobileGreetingHeader(
                                displayName = uiState.settings.displayName.ifBlank { "Guest" },
                                photoUrl = uiState.photoUrl,
                                isCloudSignedIn = isCloudSignedIn,
                                isDarkTheme = isDarkEffective,
                                activeAlertsCount = activeAlertsCount,
                                onNotificationsClick = { showNotificationsSheet = true },
                                onToggleTheme = {
                                    val nextTheme = if (isDarkEffective) "light" else "dark"
                                    viewModel.updateSettings(uiState.settings.copy(themeMode = nextTheme))
                                },
                                onProfileClick = { showProfileAccountSheet = true }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = RoyalBlue)
                                }
                            } else {
                                AnimatedContent(
                                    targetState = uiState.currentSection,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(240)) +
                                            slideInVertically(animationSpec = tween(260)) { it / 22 })
                                            .togetherWith(fadeOut(animationSpec = tween(150)))
                                    },
                                    label = "nav_section_animated_content"
                                ) { section ->
                                    when (section) {
                                        NavSection.OVERVIEW -> OverviewScreen(
                                            uiState = uiState,
                                            onAddTransactionClick = {
                                                editingTransaction = null
                                                modalDefaultType = "EXPENSE"
                                                modalDefaultDateEpochDay = null
                                                showTransactionModal = true
                                            },
                                            onAddExpenseClick = {
                                                editingTransaction = null
                                                modalDefaultType = "EXPENSE"
                                                modalDefaultDateEpochDay = null
                                                showTransactionModal = true
                                            },
                                            onAddIncomeClick = {
                                                editingTransaction = null
                                                modalDefaultType = "INCOME"
                                                modalDefaultDateEpochDay = null
                                                showTransactionModal = true
                                            },
                                            onTransferMoneyClick = {
                                                editingTransaction = null
                                                modalDefaultType = "TRANSFER"
                                                modalDefaultDateEpochDay = null
                                                showTransactionModal = true
                                            },
                                            onQuickAllowance = { amt, title, accId, accName, setBudget, dateEpochDay, timeFormatted ->
                                                viewModel.addQuickAllowanceOrIncome(amt, title, accId, accName, setBudget, dateEpochDay, timeFormatted)
                                            },
                                            onQuickExpense = { whereTitle, amt, catId, catName, accId, accName, notes, dateEpochDay, timeFormatted ->
                                                viewModel.addQuickExpense(whereTitle, amt, catId, catName, accId, accName, notes, dateEpochDay, timeFormatted)
                                            },
                                            onQuickSavings = { goalName, amt, targetAmt, accId, accName, notes ->
                                                viewModel.addQuickSavings(goalName, amt, targetAmt, accId, accName, notes)
                                            },
                                            onQuickEventExpense = { eventType, eventName, items, accId, accName, extraNotes, dateEpochDay, timeFormatted ->
                                                viewModel.addEventExpenseWithBreakdown(
                                                    eventType = eventType,
                                                    eventName = eventName,
                                                    items = items,
                                                    accountId = accId,
                                                    accountName = accName,
                                                    extraNotes = extraNotes,
                                                    dateEpochDay = dateEpochDay,
                                                    timeFormatted = timeFormatted
                                                )
                                            },
                                            onEditTransaction = { tx ->
                                                editingTransaction = tx
                                                modalDefaultDateEpochDay = null
                                                showTransactionModal = true
                                            },
                                            onDuplicateTransaction = { tx -> viewModel.duplicateTransaction(tx) },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx },
                                            onNavigate = { viewModel.navigateTo(it) }
                                        )
                                        NavSection.EVENTS -> LifeEventsScreen(
                                            uiState = uiState,
                                            onRecordEventExpense = { eventType, eventName, items, accId, accName, extraNotes, dateEpochDay, timeFormatted ->
                                                viewModel.addEventExpenseWithBreakdown(
                                                    eventType = eventType,
                                                    eventName = eventName,
                                                    items = items,
                                                    accountId = accId,
                                                    accountName = accName,
                                                    extraNotes = extraNotes,
                                                    dateEpochDay = dateEpochDay,
                                                    timeFormatted = timeFormatted
                                                )
                                            },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx }
                                        )
                                        NavSection.TRANSACTIONS -> TransactionsScreen(
                                            uiState = uiState,
                                            onFilterChange = { viewModel.updateTransactionFilter(it) },
                                            onAddTransactionClick = {
                                                editingTransaction = null
                                                modalDefaultType = "EXPENSE"
                                                showTransactionModal = true
                                            },
                                            onEditTransaction = { tx ->
                                                editingTransaction = tx
                                                showTransactionModal = true
                                            },
                                            onDuplicateTransaction = { tx -> viewModel.duplicateTransaction(tx) },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx }
                                        )
                                        NavSection.INCOME -> CategoryTypedModuleScreen(
                                            moduleType = "INCOME",
                                            uiState = uiState,
                                            onAddTransactionWithType = {
                                                editingTransaction = null
                                                modalDefaultType = "INCOME"
                                                showTransactionModal = true
                                            },
                                            onEditTransaction = { tx ->
                                                editingTransaction = tx
                                                showTransactionModal = true
                                            },
                                            onDuplicateTransaction = { tx -> viewModel.duplicateTransaction(tx) },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx },
                                            onSaveCategory = { viewModel.saveCategory(it) },
                                            onDeleteCategory = { viewModel.deleteCategory(it) }
                                        )
                                        NavSection.EXPENSES -> CategoryTypedModuleScreen(
                                            moduleType = "EXPENSE",
                                            uiState = uiState,
                                            onAddTransactionWithType = {
                                                editingTransaction = null
                                                modalDefaultType = "EXPENSE"
                                                showTransactionModal = true
                                            },
                                            onEditTransaction = { tx ->
                                                editingTransaction = tx
                                                showTransactionModal = true
                                            },
                                            onDuplicateTransaction = { tx -> viewModel.duplicateTransaction(tx) },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx },
                                            onSaveCategory = { viewModel.saveCategory(it) },
                                            onDeleteCategory = { viewModel.deleteCategory(it) }
                                        )
                                        NavSection.ACCOUNTS -> AccountsAndWalletsScreen(
                                            uiState = uiState,
                                            onSaveAccount = { viewModel.saveAccount(it) },
                                            onDeleteAccount = { viewModel.deleteAccount(it) },
                                            onTransferClick = {
                                                editingTransaction = null
                                                modalDefaultType = "TRANSFER"
                                                showTransactionModal = true
                                            },
                                            onEditTransaction = { tx ->
                                                editingTransaction = tx
                                                showTransactionModal = true
                                            },
                                            onDuplicateTransaction = { tx -> viewModel.duplicateTransaction(tx) },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx }
                                        )
                                        NavSection.ANALYTICS -> AnalyticsScreen(
                                            uiState = uiState,
                                            onPeriodChange = { viewModel.setAnalyticsPeriod(it) },
                                            onChartTypeChange = { viewModel.setAnalyticsChartType(it) }
                                        )
                                        NavSection.BUDGETS -> BudgetsScreen(
                                            uiState = uiState,
                                            onMonthChange = { viewModel.setSelectedMonthKey(it) },
                                            onSaveBudget = { viewModel.saveBudget(it) },
                                            onDeleteBudget = { viewModel.deleteBudget(it) }
                                        )
                                        NavSection.SAVINGS_GOALS -> SavingsGoalsScreen(
                                            uiState = uiState,
                                            onSaveGoal = { viewModel.saveSavingsGoal(it) },
                                            onContribute = { goalId, amt, accId, accName, notes ->
                                                viewModel.contributeToSavingsGoal(goalId, amt, accId, accName, notes)
                                            },
                                            onDeleteGoal = { viewModel.deleteSavingsGoal(it) }
                                        )
                                        NavSection.RECURRING -> RecurringTransactionsScreen(
                                            uiState = uiState,
                                            onSaveRule = { viewModel.saveRecurringRule(it) },
                                            onToggleRule = { viewModel.toggleRecurringRule(it) },
                                            onDeleteRule = { viewModel.deleteRecurringRule(it) },
                                            onRunCheckNow = { viewModel.runRecurringCheckNow() }
                                        )
                                        NavSection.LOANS -> LoansAndDebtsScreen(
                                            uiState = uiState,
                                            onSaveLoan = { viewModel.saveLoan(it) },
                                            onRecordPayment = { loan, amt, accId, accName, notes ->
                                                viewModel.recordLoanPayment(loan, amt, accId, accName, notes)
                                            },
                                            onDeleteLoan = { viewModel.deleteLoan(it) }
                                        )
                                        NavSection.CALENDAR -> FinancialCalendarScreen(
                                            uiState = uiState,
                                            onMonthChange = { viewModel.setSelectedMonthKey(it) },
                                            onSelectDate = { viewModel.setSelectedCalendarDay(it) },
                                            onAddTransactionOnDate = { dateEpochDay ->
                                                editingTransaction = null
                                                modalDefaultType = "EXPENSE"
                                                modalDefaultDateEpochDay = dateEpochDay
                                                showTransactionModal = true
                                            },
                                            onEditTransaction = { tx ->
                                                editingTransaction = tx
                                                modalDefaultDateEpochDay = null
                                                showTransactionModal = true
                                            },
                                            onDuplicateTransaction = { tx -> viewModel.duplicateTransaction(tx) },
                                            onDeleteTransaction = { tx -> deletingTransaction = tx }
                                        )
                                        NavSection.REPORTS -> ReportsAndExportsScreen(uiState = uiState)
                                        NavSection.SETTINGS -> SettingsScreen(
                                            uiState = uiState,
                                            isCloudSignedIn = isCloudSignedIn,
                                            onUpdateSettings = { viewModel.updateSettings(it) },
                                            onUpdateUserProfile = { updatedSettings, newPhotoUrl, isCustomPhoto ->
                                                viewModel.updateUserProfile(updatedSettings, newPhotoUrl, isCustomPhoto)
                                            },
                                            onSyncToCloud = {
                                                viewModel.syncWithFirebaseCloud()
                                            },
                                            onSignOutClick = {
                                                if (isCloudSignedIn) {
                                                    signOut(
                                                        credentialManager = credentialManager,
                                                        scope = scope,
                                                        onSignOutComplete = { onAuthStateUpdated() }
                                                    )
                                                } else {
                                                    onGoogleSignInClicked(
                                                        context = context,
                                                        credentialManager = credentialManager,
                                                        onAuthSuccess = {
                                                            onAuthStateUpdated()
                                                        },
                                                        onAuthError = {
                                                            showGoogleSignInSheet = true
                                                        },
                                                        scope = scope,
                                                        onAuthCancelled = {}
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Confirmed Mobile Greeting Header:
 * Personal BM logo | Greeting | Notification icon | Theme toggle | Profile avatar
 */
@Composable
private fun MobileGreetingHeader(
    displayName: String,
    photoUrl: String,
    isCloudSignedIn: Boolean = false,
    isDarkTheme: Boolean,
    activeAlertsCount: Int,
    onNotificationsClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onProfileClick: () -> Unit
) {
    val firstName = displayName.trim().split(" ").firstOrNull()?.ifBlank { "Guest" } ?: "Guest"

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = RoyalBlue,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "BM",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCloudSignedIn) "Cloud Synced" else "Guest Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCloudSignedIn) EmeraldSuccess else RoyalBlue,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "Hi, $firstName",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("mobile_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("mobile_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (activeAlertsCount > 0) {
                                Badge(containerColor = CrimsonDanger, contentColor = Color.White) {
                                    Text("$activeAlertsCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("mobile_profile_avatar")
                ) {
                    UserAvatarBadge(
                        displayName = displayName,
                        photoUrl = photoUrl,
                        size = 34.dp
                    )
                }
            }
        }
    }
}

/**
 * Confirmed Tablet / Desktop Top Navigation Bar:
 * Page title | Date range selector | Search | Notifications | Theme toggle | User profile
 */
@Composable
private fun DesktopTopNavBar(
    uiState: PersonalBmUiState,
    isDarkTheme: Boolean,
    activeAlertsCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onProfileClick: () -> Unit
) {
    val monthKey = uiState.selectedMonthKey.ifBlank { DhakaDateUtils.currentMonthKey() }
    val displayName = uiState.settings.displayName.ifBlank { "Personal BM User" }
    val initials = displayName.trim().take(2).uppercase().ifBlank { "BM" }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page Title
            Text(
                text = uiState.currentSection.label(uiState.settings.language),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date / Month Range Selector
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onMonthChange(DhakaDateUtils.previousMonthKey(monthKey)) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DhakaDateUtils.formatMonthKeyDisplay(monthKey),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onMonthChange(DhakaDateUtils.nextMonthKey(monthKey)) }
                        )
                    }
                }

                // Compact Search Input
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.width(210.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.txFilter.searchQuery.isEmpty()) {
                                Text(
                                    text = "Search ledger...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = uiState.txFilter.searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(RoyalBlue),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Theme Toggle
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Notifications
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (activeAlertsCount > 0) {
                                Badge(containerColor = CrimsonDanger, contentColor = Color.White) {
                                    Text("$activeAlertsCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // User Profile Pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onProfileClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatarBadge(
                            displayName = displayName,
                            photoUrl = uiState.photoUrl,
                            size = 26.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayName.split(" ").firstOrNull() ?: "Profile",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimal Light Collapsible / Fixed Left Sidebar for Tablet & Desktop screens.
 */
@Composable
private fun MinimalLightSidebar(
    currentSection: NavSection,
    language: String,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onSelect: (NavSection) -> Unit
) {
    val sidebarWidth by animateDpAsState(
        targetValue = if (collapsed) 74.dp else 236.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sidebar_width_anim"
    )

    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp, top = 4.dp),
            horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = RoyalBlue,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("BM", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                if (!collapsed) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Personal BM",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Every Taka. Total Control.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (!collapsed) {
                IconButton(
                    onClick = onToggleCollapse,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Collapse sidebar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (collapsed) {
            IconButton(
                onClick = onToggleCollapse,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Expand sidebar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        NavSection.entries.forEach { section ->
            val selected = currentSection == section
            Surface(
                color = if (selected) RoyalBlue else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(section) }
                    .testTag("sidebar_${section.route}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.Start
                ) {
                    Icon(
                        imageVector = iconForNavSection(section),
                        contentDescription = section.labelEn,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    if (!collapsed) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = section.label(language),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsAndAlertsSheet(
    uiState: PersonalBmUiState,
    onDismiss: () -> Unit,
    onNavigate: (NavSection) -> Unit
) {
    val symbol = uiState.settings.currencySymbol.ifBlank { "৳" }
    val currentMonthKey = uiState.selectedMonthKey.ifBlank { DhakaDateUtils.currentMonthKey() }
    val monthTxs = uiState.transactions.filter {
        it.type == "EXPENSE" && DhakaDateUtils.epochDayToMonthKey(it.dateEpochDay) == currentMonthKey
    }
    val totalExp = monthTxs.sumOf { it.amountPaisa }

    val warningBudgets = uiState.budgets.filter { it.monthKey == currentMonthKey && it.limitPaisa > 0L }.mapNotNull { b ->
        val spent = if (b.categoryId == "ALL") totalExp else monthTxs.filter { it.categoryId == b.categoryId }.sumOf { it.amountPaisa }
        val pct = ((spent * 100.0) / b.limitPaisa).toInt()
        if (pct >= 75) Triple(b, spent, pct) else null
    }
    val activeBills = uiState.recurringTransactions.filter { it.isActive }.sortedBy { it.nextDueDateEpochDay }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Notifications & Financial Alerts",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (warningBudgets.isEmpty() && activeBills.isEmpty()) {
                Text(
                    text = "All clear! Your budgets are well within limits and no recurring bills require attention.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (warningBudgets.isNotEmpty()) {
                Text(
                    text = "Budget Threshold Warnings",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                warningBudgets.forEach { (bgt, spent, pct) ->
                    val alertColor = if (pct >= 100) CrimsonDanger else AmberWarning
                    Surface(
                        color = alertColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, alertColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(NavSection.BUDGETS) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = alertColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${bgt.categoryName} ($pct% used)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${MoneyUtils.formatPaisa(spent, symbol)} of ${MoneyUtils.formatPaisa(bgt.limitPaisa, symbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (activeBills.isNotEmpty()) {
                Text(
                    text = "Scheduled Recurring Items",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                activeBills.take(5).forEach { bill ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(NavSection.RECURRING) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bill.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Next due: ${DhakaDateUtils.formatEpochDay(bill.nextDueDateEpochDay)} (${bill.frequency})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = MoneyUtils.formatPaisa(bill.amountPaisa, symbol),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (bill.type == "INCOME") EmeraldSuccess else CrimsonDanger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Account & Profile Modal Sheet opened directly from the Profile Icon in the top bar.
 * Lets a Guest user log into their Personal Google Account & save their data to Firebase Cloud,
 * or lets a signed-in user update their Google Profile Photo / Display Name and trigger Cloud Sync.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAndCloudAccountSheet(
    uiState: PersonalBmUiState,
    isCloudSignedIn: Boolean,
    onDismiss: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onSyncToCloud: () -> Unit,
    onSignOutToGuest: () -> Unit,
    onUpdateUserProfile: (com.example.data.model.UserSettings, String, Boolean) -> Unit,
    onOpenFullSettings: () -> Unit
) {
    val current = uiState.settings
    val googleUser = remember(isCloudSignedIn) {
        try { Firebase.auth.currentUser } catch (_: Exception) { null }
    }
    val googlePhotoUrl = remember(googleUser) {
        googleUser?.photoUrl?.toString() ?: ""
    }

    var displayName by remember(current.displayName) { mutableStateOf(current.displayName.ifBlank { "Guest" }) }
    var photoUrl by remember(uiState.photoUrl, googlePhotoUrl) {
        mutableStateOf(uiState.photoUrl.ifBlank { googlePhotoUrl })
    }
    var isCustomPhoto by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUrl = uri.toString()
            isCustomPhoto = true
            onUpdateUserProfile(
                current.copy(displayName = displayName.trim().ifBlank { "Guest" }.take(120)),
                photoUrl,
                true
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                UserAvatarBadge(
                    displayName = displayName.ifBlank { "Guest" },
                    photoUrl = photoUrl,
                    size = 64.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName.ifBlank { "Guest" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isCloudSignedIn) {
                            current.email.ifBlank { googleUser?.email ?: "Personal Google Account" }
                        } else {
                            "Guest Account • Local Device Storage"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (isCloudSignedIn) EmeraldSuccess.copy(alpha = 0.14f) else RoyalBlue.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCloudSignedIn) Icons.Default.CloudDone else Icons.Default.Backup,
                                contentDescription = null,
                                tint = if (isCloudSignedIn) EmeraldSuccess else RoyalBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCloudSignedIn) "Cloud Sync Active (Firebase)" else "Logged in as Guest",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCloudSignedIn) EmeraldSuccess else RoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Primary Action Card: Login with Google & Save to Cloud OR Sync / Sign Out
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isCloudSignedIn) {
                        Text(
                            text = "Cloud Backup",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Sign in with Google to sync your profile & records to Firebase Cloud.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onGoogleLoginClick,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_sheet_google_login_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Google", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text(
                            text = "Google Cloud Connected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(
                            onClick = onSyncToCloud,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_sheet_sync_cloud_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync to Cloud Now")
                        }
                        OutlinedButton(
                            onClick = onSignOutToGuest,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_sheet_sign_out_guest_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = CrimsonDanger,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Switch to Guest Mode", color = CrimsonDanger)
                        }
                    }
                }
            }

            // Quick Profile Editor (Update Name & Avatar Photo)
            Text(
                text = "Customize Profile Name & Photo",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change Photo")
                }
                if (googlePhotoUrl.isNotBlank() && photoUrl != googlePhotoUrl) {
                    TextButton(
                        onClick = {
                            photoUrl = googlePhotoUrl
                            isCustomPhoto = false
                            onUpdateUserProfile(
                                current.copy(displayName = displayName.trim().ifBlank { googleUser?.displayName ?: "User" }),
                                googlePhotoUrl,
                                false
                            )
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use Google Photo")
                    }
                }
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_sheet_name_input")
            )

            Button(
                onClick = {
                    onUpdateUserProfile(
                        current.copy(displayName = displayName.trim().ifBlank { "Guest" }.take(120)),
                        photoUrl.trim(),
                        isCustomPhoto
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile Name & Photo")
            }

            // Quick Theme Switcher (White Default vs Dark & Orange)
            Text(
                text = "Theme Appearance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "light" to "Light (White)",
                    "dark" to "Dark & Orange",
                    "system" to "System Auto"
                ).forEach { (modeKey, modeLabel) ->
                    FilterChip(
                        selected = current.themeMode == modeKey,
                        onClick = {
                            onUpdateUserProfile(
                                current.copy(themeMode = modeKey),
                                photoUrl.trim(),
                                isCustomPhoto
                            )
                        },
                        label = { Text(modeLabel) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            OutlinedButton(
                onClick = onOpenFullSettings,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Full Settings & Preferences")
            }
        }
    }
}

