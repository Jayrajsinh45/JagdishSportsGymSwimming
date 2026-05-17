package com.jagdishsports.gymswimming

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jagdishsports.gymswimming.data.CameraPhotoTarget
import com.jagdishsports.gymswimming.data.MemberCategories
import com.jagdishsports.gymswimming.data.MemberEntity
import com.jagdishsports.gymswimming.data.MemberPhotoStorage
import com.jagdishsports.gymswimming.data.MemberStatus
import com.jagdishsports.gymswimming.data.endDate
import com.jagdishsports.gymswimming.data.expiresWithin
import com.jagdishsports.gymswimming.data.isActiveOrExpiresToday
import com.jagdishsports.gymswimming.data.isExpired
import com.jagdishsports.gymswimming.data.startDate
import com.jagdishsports.gymswimming.data.status
import com.jagdishsports.gymswimming.notifications.NotificationHelper
import com.jagdishsports.gymswimming.notifications.NotificationScheduler
import com.jagdishsports.gymswimming.reports.MonthlyReportPdf
import com.jagdishsports.gymswimming.reports.MonthlyReportRequest
import com.jagdishsports.gymswimming.ui.theme.DangerRed
import com.jagdishsports.gymswimming.ui.theme.JagdishSportsTheme
import com.jagdishsports.gymswimming.ui.theme.NavyBlue
import com.jagdishsports.gymswimming.ui.theme.SportGreen
import com.jagdishsports.gymswimming.ui.theme.WarningAmber
import com.jagdishsports.gymswimming.ui.viewmodel.GymViewModel
import com.jagdishsports.gymswimming.ui.viewmodel.MemberFormViewModel
import com.jagdishsports.gymswimming.ui.viewmodel.ReportViewModel
import com.jagdishsports.gymswimming.ui.viewmodel.SwimmingViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var pendingMonthlyReport: MonthlyReportRequest? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        NotificationScheduler.scheduleDailyExpiryCheck(this)
    }

    private val monthlyReportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val request = pendingMonthlyReport
        pendingMonthlyReport = null

        if (uri != null && request != null) {
            runCatching {
                MonthlyReportPdf.write(this, uri, request)
            }.onSuccess {
                Toast.makeText(this, "PDF report saved", Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                Toast.makeText(
                    this,
                    error.message ?: "Unable to save PDF report",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        NotificationScheduler.scheduleDailyExpiryCheck(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            JagdishSportsTheme {
                JagdishSportsApp(
                    onDownloadMonthlyPdf = ::downloadMonthlyPdf
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun downloadMonthlyPdf(category: String, month: YearMonth, members: List<MemberEntity>) {
        val request = MonthlyReportRequest(category = category, month = month, members = members)
        pendingMonthlyReport = request
        monthlyReportLauncher.launch(request.fileName)
    }
}

private object Routes {
    const val HOME = "home"
    const val REPORT = "report"
    const val MEMBERS = "members/{category}"
    const val MEMBER_FORM = "memberForm/{category}?memberId={memberId}"

    fun members(category: String) = "members/$category"

    fun addMember(category: String) = "memberForm/$category?memberId=-1"

    fun editMember(category: String, memberId: Long) = "memberForm/$category?memberId=$memberId"
}

private enum class HomeMemberFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    EXPIRED("Expired"),
    EXPIRING_SOON("Expiring Soon")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JagdishSportsApp(
    onDownloadMonthlyPdf: (String, YearMonth, List<MemberEntity>) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val category = backStackEntry?.arguments?.getString("category")
    val memberId = backStackEntry?.arguments?.getLong("memberId") ?: -1L
    val isHomeRoute = route == Routes.HOME || route?.startsWith("members") == true ||
        route?.startsWith("memberForm") == true

    val title = when {
        route == Routes.REPORT -> "Report"
        route?.startsWith("members") == true -> "${category.orEmpty()} Members"
        route?.startsWith("memberForm") == true && memberId > 0L -> "Edit ${category.orEmpty()} Member"
        route?.startsWith("memberForm") == true -> "Add ${category.orEmpty()} Member"
        else -> "Jagdish Sports Gym and Swimming"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (title == "Jagdish Sports Gym and Swimming") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Jagdish Sports",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Gym and Swimming",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    } else {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (route != Routes.HOME && route != Routes.REPORT) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = isHomeRoute,
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = route == Routes.REPORT,
                    onClick = {
                        navController.navigate(Routes.REPORT) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                    label = { Text("Report") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAddMember = { selectedCategory ->
                        navController.navigate(Routes.addMember(selectedCategory))
                    },
                    onEditMember = { member ->
                        navController.navigate(Routes.editMember(member.category, member.id))
                    }
                )
            }
            composable(Routes.REPORT) {
                ReportScreen(onDownloadMonthlyPdf = onDownloadMonthlyPdf)
            }
            composable(
                route = Routes.MEMBERS,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { entry ->
                val selectedCategory = entry.arguments?.getString("category") ?: MemberCategories.GYM
                MembersListScreen(
                    category = selectedCategory,
                    onAddMember = { navController.navigate(Routes.addMember(selectedCategory)) },
                    onEditMember = { member ->
                        navController.navigate(Routes.editMember(selectedCategory, member.id))
                    }
                )
            }
            composable(
                route = Routes.MEMBER_FORM,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("memberId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val selectedCategory = entry.arguments?.getString("category") ?: MemberCategories.GYM
                val selectedMemberId = entry.arguments?.getLong("memberId")?.takeIf { it > 0L }
                MemberFormScreen(
                    category = selectedCategory,
                    memberId = selectedMemberId,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onAddMember: (String) -> Unit,
    onEditMember: (MemberEntity) -> Unit
) {
    val gymViewModel: GymViewModel = viewModel()
    val swimmingViewModel: SwimmingViewModel = viewModel()
    val gymMembers by gymViewModel.members.collectAsStateWithLifecycle()
    val swimmingMembers by swimmingViewModel.members.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf(MemberCategories.GYM) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HomeMemberFilter.ALL) }
    val categoryMembers = if (selectedCategory == MemberCategories.SWIMMING) {
        swimmingMembers
    } else {
        gymMembers
    }.sortedForHome()
    val allMembers = (gymMembers + swimmingMembers).sortedForHome()
    val trimmedSearch = searchQuery.trim()
    val searchedMembers = if (trimmedSearch.isBlank()) {
        categoryMembers
    } else {
        allMembers.filter { it.matchesHomeSearch(trimmedSearch) }
    }
    val visibleMembers = searchedMembers.filter { it.matchesHomeFilter(selectedFilter) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Manage memberships",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Switch between Gym and Swimming from the top, then add or edit members directly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CategorySegmentedControl(
                        selectedCategory = selectedCategory,
                        onCategorySelected = {
                            selectedCategory = it
                            selectedFilter = HomeMemberFilter.ALL
                        }
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            selectedFilter = HomeMemberFilter.ALL
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search Gym and Swimming members") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                TextButton(onClick = { searchQuery = "" }) {
                                    Text("Clear")
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }
            item {
                HomeCategoryOverview(
                    category = selectedCategory,
                    members = categoryMembers,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        selectedFilter = if (selectedFilter == filter) {
                            HomeMemberFilter.ALL
                        } else {
                            filter
                        }
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trimmedSearch.isBlank()) {
                            "$selectedCategory Members"
                        } else {
                            "Search Results"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedFilter != HomeMemberFilter.ALL) {
                            TextButton(onClick = { selectedFilter = HomeMemberFilter.ALL }) {
                                Text("Show All")
                            }
                        }
                        Text(
                            text = "${visibleMembers.size} shown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (visibleMembers.isEmpty()) {
                item {
                    InlineEmptyState(
                        title = if (trimmedSearch.isBlank()) {
                            "No matching members"
                        } else {
                            "No search results"
                        },
                        message = if (trimmedSearch.isBlank()) {
                            "Tap the add button to create the first membership or clear the selected filter."
                        } else {
                            "Try another name, phone number, or category."
                        }
                    )
                }
            } else {
                items(visibleMembers, key = { it.id }) { member ->
                    MemberCard(
                        member = member,
                        onClick = { onEditMember(member) }
                    )
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            onClick = { onAddMember(selectedCategory) }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add member")
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }
        }
    }
}

@Composable
private fun CategorySegmentedControl(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CategorySegmentOption(
                modifier = Modifier.weight(1f),
                category = MemberCategories.GYM,
                icon = Icons.Filled.FitnessCenter,
                selected = selectedCategory == MemberCategories.GYM,
                activeColor = NavyBlue,
                onClick = { onCategorySelected(MemberCategories.GYM) }
            )
            CategorySegmentOption(
                modifier = Modifier.weight(1f),
                category = MemberCategories.SWIMMING,
                icon = Icons.Filled.Pool,
                selected = selectedCategory == MemberCategories.SWIMMING,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = { onCategorySelected(MemberCategories.SWIMMING) }
            )
        }
    }
}

@Composable
private fun CategorySegmentOption(
    category: String,
    icon: ImageVector,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (selected) activeColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeCategoryOverview(
    category: String,
    members: List<MemberEntity>,
    selectedFilter: HomeMemberFilter,
    onFilterSelected: (HomeMemberFilter) -> Unit
) {
    val accentColor = if (category == MemberCategories.GYM) {
        NavyBlue
    } else {
        MaterialTheme.colorScheme.primary
    }
    val icon = if (category == MemberCategories.GYM) {
        Icons.Filled.FitnessCenter
    } else {
        Icons.Filled.Pool
    }
    val activeCount = members.count { it.status(expiringSoonDays = 5) == MemberStatus.ACTIVE }
    val expiredCount = members.count { it.status(expiringSoonDays = 5) == MemberStatus.EXPIRED }
    val expiringSoonCount = members.count { it.status(expiringSoonDays = 5) == MemberStatus.EXPIRING_SOON }
    val totalFees = members.sumOf { it.feesPaid }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f),
                    contentColor = accentColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$category Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${members.size} members saved on this device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewMetric(
                    modifier = Modifier.weight(1f),
                    label = "Active",
                    value = activeCount.toString(),
                    color = SportGreen,
                    selected = selectedFilter == HomeMemberFilter.ACTIVE,
                    onClick = { onFilterSelected(HomeMemberFilter.ACTIVE) }
                )
                OverviewMetric(
                    modifier = Modifier.weight(1f),
                    label = "Expired",
                    value = expiredCount.toString(),
                    color = DangerRed,
                    selected = selectedFilter == HomeMemberFilter.EXPIRED,
                    onClick = { onFilterSelected(HomeMemberFilter.EXPIRED) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewMetric(
                    modifier = Modifier.weight(1f),
                    label = "Expiring",
                    value = expiringSoonCount.toString(),
                    color = WarningAmber,
                    selected = selectedFilter == HomeMemberFilter.EXPIRING_SOON,
                    onClick = { onFilterSelected(HomeMemberFilter.EXPIRING_SOON) }
                )
                OverviewMetric(
                    modifier = Modifier.weight(1f),
                    label = "Fees",
                    value = formatRupees(totalFees),
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun OverviewMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val metricModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Surface(
        modifier = metricModifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color else color.copy(alpha = 0.10f),
        contentColor = if (selected) Color.White else color
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InlineEmptyState(title: String, message: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MembersListScreen(
    category: String,
    onAddMember: () -> Unit,
    onEditMember: (MemberEntity) -> Unit
) {
    val gymViewModel: GymViewModel = viewModel()
    val swimmingViewModel: SwimmingViewModel = viewModel()
    val selectedMembers = if (category == MemberCategories.SWIMMING) {
        swimmingViewModel.members
    } else {
        gymViewModel.members
    }
    val members by selectedMembers.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        if (members.isEmpty()) {
            EmptyState(
                title = "No $category members yet",
                message = "Tap the add button to create the first membership."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(members, key = { it.id }) { member ->
                    MemberCard(
                        member = member,
                        onClick = { onEditMember(member) }
                    )
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            onClick = onAddMember
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add member")
        }
    }
}

@Composable
private fun MemberCard(
    member: MemberEntity,
    modifier: Modifier = Modifier,
    statusWindowDays: Long = 5,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    ElevatedCard(modifier = cardModifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                MemberPhotoAvatar(
                    photoPath = member.photoPath,
                    name = member.fullName,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = member.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(member.status(expiringSoonDays = statusWindowDays))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateText(label = "Start", date = member.startDate())
                DateText(label = "End", date = member.endDate())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(member.category) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (member.category == MemberCategories.GYM) {
                                Icons.Filled.FitnessCenter
                            } else {
                                Icons.Filled.Pool
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Text(
                    text = formatRupees(member.feesPaid),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MemberPhotoAvatar(
    photoPath: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(photoPath) {
        photoPath?.let { path ->
            BitmapFactory.decodeFile(path)?.asImageBitmap()
        }
    }
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Member photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateText(label: String, date: LocalDate) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusBadge(status: MemberStatus) {
    val (label, color, icon) = when (status) {
        MemberStatus.ACTIVE -> Triple("Active", SportGreen, Icons.Filled.CheckCircle)
        MemberStatus.EXPIRING_SOON -> Triple("Expiring Soon", WarningAmber, Icons.Filled.Warning)
        MemberStatus.EXPIRED -> Triple("Expired", DangerRed, Icons.Filled.Cancel)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.13f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberFormScreen(
    category: String,
    memberId: Long?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MemberFormViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val memberFlow = remember(memberId) {
        memberId?.let { viewModel.observeMember(it) } ?: flowOf(null)
    }
    val existingMember by memberFlow.collectAsStateWithLifecycle(initialValue = null)

    var formLoaded by remember(memberId) { mutableStateOf(memberId == null) }
    var fullName by remember(memberId) { mutableStateOf("") }
    var phoneNumber by remember(memberId) { mutableStateOf("") }
    var startDate by remember(memberId) { mutableStateOf(LocalDate.now()) }
    var endDate by remember(memberId) { mutableStateOf(LocalDate.now().plusMonths(1)) }
    var feesPaid by remember(memberId) { mutableStateOf("") }
    var photoPath by remember(memberId) { mutableStateOf<String?>(null) }
    var formError by remember(memberId) { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingCameraTarget by remember { mutableStateOf<CameraPhotoTarget?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val target = pendingCameraTarget
        if (saved && target != null) {
            photoPath = target.path
            formError = null
        } else {
            MemberPhotoStorage.deletePhoto(target?.path)
        }
        pendingCameraTarget = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            runCatching {
                MemberPhotoStorage.copyGalleryPhoto(context, it)
            }.onSuccess { copiedPath ->
                photoPath = copiedPath
                formError = null
            }.onFailure {
                formError = "Unable to add selected photo."
            }
        }
    }

    LaunchedEffect(existingMember?.id) {
        val member = existingMember
        if (!formLoaded && member != null) {
            fullName = member.fullName
            phoneNumber = member.phoneNumber
            startDate = member.startDate()
            endDate = member.endDate()
            feesPaid = member.feesPaid.toString()
            photoPath = member.photoPath
            formLoaded = true
        }
    }

    if (!formLoaded) {
        EmptyState(
            title = "Loading member",
            message = "Please wait while the saved record is opened."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AssistChip(
                onClick = {},
                label = { Text("$category category") },
                leadingIcon = {
                    Icon(
                        imageVector = if (category == MemberCategories.GYM) {
                            Icons.Filled.FitnessCenter
                        } else {
                            Icons.Filled.Pool
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
        item {
            MemberPhotoSection(
                photoPath = photoPath,
                memberName = fullName.ifBlank { "Member" },
                onTakePhoto = {
                    runCatching {
                        MemberPhotoStorage.createCameraPhotoTarget(context)
                    }.onSuccess { target ->
                        pendingCameraTarget = target
                        cameraLauncher.launch(target.uri)
                    }.onFailure {
                        pendingCameraTarget = null
                        formError = "Unable to open camera."
                    }
                },
                onChooseFromGallery = {
                    galleryLauncher.launch("image/*")
                },
                onRemovePhoto = {
                    photoPath = null
                }
            )
        }
        item {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
        }
        item {
            DatePickerField(
                label = "Start Date",
                date = startDate,
                onDateChange = { startDate = it }
            )
        }
        item {
            DatePickerField(
                label = "End Date",
                date = endDate,
                onDateChange = { endDate = it }
            )
        }
        item {
            OutlinedTextField(
                value = feesPaid,
                onValueChange = { value ->
                    feesPaid = value.filter { it.isDigit() }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Fees Paid (\u20B9)") },
                leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
        formError?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val fees = feesPaid.toLongOrNull()
                    formError = when {
                        fullName.isBlank() -> "Full name is required."
                        phoneNumber.isBlank() -> "Phone number is required."
                        endDate.isBefore(startDate) -> "End date cannot be before start date."
                        fees == null -> "Fees paid must be a number."
                        else -> null
                    }

                    if (formError == null && fees != null) {
                        coroutineScope.launch {
                            viewModel.save(
                                MemberEntity(
                                    id = memberId ?: 0L,
                                    fullName = fullName.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    startDateEpochDay = startDate.toEpochDay(),
                                    endDateEpochDay = endDate.toEpochDay(),
                                    feesPaid = fees,
                                    category = category,
                                    photoPath = photoPath,
                                    createdAtEpochMillis = existingMember?.createdAtEpochMillis
                                        ?: System.currentTimeMillis()
                                )
                            )
                            if (existingMember?.photoPath != photoPath) {
                                MemberPhotoStorage.deletePhoto(existingMember?.photoPath)
                            }
                            onDone()
                        }
                    }
                }
            ) {
                Text(if (memberId == null) "Add Member" else "Save Changes")
            }
        }
        if (memberId != null) {
            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Member")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete member?") },
            text = { Text("This removes the member from local storage on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val member = existingMember
                        showDeleteDialog = false
                        if (member != null) {
                            coroutineScope.launch {
                                viewModel.delete(member)
                                MemberPhotoStorage.deletePhoto(member.photoPath)
                                onDone()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MemberPhotoSection(
    photoPath: String?,
    memberName: String,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberPhotoAvatar(
                    photoPath = photoPath,
                    name = memberName,
                    modifier = Modifier.size(82.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Member Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Take a live photo or choose one from gallery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onTakePhoto
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onChooseFromGallery
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
            if (photoPath != null) {
                TextButton(onClick = onRemovePhoto) {
                    Text("Remove Photo")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = formatDate(date),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true },
        label = { Text(label) },
        readOnly = true,
        leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Choose $label")
            }
        }
    )

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateChange(it.toLocalDateFromPicker())
                        }
                        showPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private enum class ReportFilter(val label: String) {
    ALL("All"),
    MONTH("Month"),
    EXPIRED("Expired")
}

@Composable
private fun ReportScreen(
    onDownloadMonthlyPdf: (String, YearMonth, List<MemberEntity>) -> Unit
) {
    val viewModel: ReportViewModel = viewModel()
    val members by viewModel.members.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf(MemberCategories.GYM) }
    var selectedFilter by remember { mutableStateOf(ReportFilter.ALL) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val categoryMembers = members.filter { it.category == selectedCategory }
    val monthlyMembers = membersForMonth(categoryMembers, selectedMonth)
    val filteredMembers = when (selectedFilter) {
        ReportFilter.ALL -> categoryMembers
        ReportFilter.MONTH -> monthlyMembers
        ReportFilter.EXPIRED -> categoryMembers.filter { it.isExpired(today) }
    }
    val categoryColor = if (selectedCategory == MemberCategories.GYM) {
        NavyBlue
    } else {
        MaterialTheme.colorScheme.primary
    }
    val categoryIcon = if (selectedCategory == MemberCategories.GYM) {
        Icons.Filled.FitnessCenter
    } else {
        Icons.Filled.Pool
    }
    val activeCount = filteredMembers.count { it.isActiveOrExpiresToday(today) }
    val expiredCount = filteredMembers.count { it.isExpired(today) }
    val expiringSoonCount = filteredMembers.count { it.expiresWithin(7, today) }
    val totalFees = filteredMembers.sumOf { it.feesPaid }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Report Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                CategorySegmentedControl(
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        selectedCategory = it
                        selectedFilter = ReportFilter.ALL
                    }
                )
            }
        }
        item {
            SummarySection(
                totalMembersText = "$selectedCategory = ${filteredMembers.size}",
                activeCount = activeCount,
                expiredCount = expiredCount,
                expiringSoonCount = expiringSoonCount,
                totalFees = totalFees
            )
        }
        item {
            CategoryDataCard(
                title = "$selectedCategory Data",
                count = filteredMembers.size,
                fees = totalFees,
                color = categoryColor,
                icon = categoryIcon
            )
        }
        item {
            MonthReportControls(
                category = selectedCategory,
                selectedMonth = selectedMonth,
                monthlyMemberCount = monthlyMembers.size,
                monthlyFees = monthlyMembers.sumOf { it.feesPaid },
                onMonthChange = { month ->
                    selectedMonth = month
                    selectedFilter = ReportFilter.MONTH
                },
                onShowMonth = { selectedFilter = ReportFilter.MONTH },
                onDownloadPdf = {
                    onDownloadMonthlyPdf(selectedCategory, selectedMonth, monthlyMembers)
                }
            )
        }
        item {
            FilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }
        if (filteredMembers.isEmpty()) {
            item {
                EmptyState(
                    title = "No matching members",
                    message = "Try another filter, choose another month, or add members from Home."
                )
            }
        } else {
            items(filteredMembers, key = { it.id }) { member ->
                MemberCard(
                    member = member,
                    statusWindowDays = 7
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    totalMembersText: String,
    activeCount: Int,
    expiredCount: Int,
    expiringSoonCount: Int,
    totalFees: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            label = "Total Members",
            value = totalMembersText,
            color = MaterialTheme.colorScheme.secondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Active",
                value = activeCount.toString(),
                color = SportGreen
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Expired",
                value = expiredCount.toString(),
                color = DangerRed
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Expiring Soon",
                value = expiringSoonCount.toString(),
                color = WarningAmber
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Fees Collected",
                value = formatRupees(totalFees),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryBreakdownSection(
    gymCount: Int,
    gymFees: Long,
    swimmingCount: Int,
    swimmingFees: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Category Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryDataCard(
                modifier = Modifier.weight(1f),
                title = "Gym",
                count = gymCount,
                fees = gymFees,
                color = NavyBlue,
                icon = Icons.Filled.FitnessCenter
            )
            CategoryDataCard(
                modifier = Modifier.weight(1f),
                title = "Swimming",
                count = swimmingCount,
                fees = swimmingFees,
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Filled.Pool
            )
        }
    }
}

@Composable
private fun CategoryDataCard(
    title: String,
    count: Int,
    fees: Long,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = color.copy(alpha = 0.14f),
                    contentColor = color
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$count members",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatRupees(fees),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MemberSplitChart(gymCount: Int, swimmingCount: Int) {
    val total = gymCount + swimmingCount
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Gym vs Swimming Split",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (total == 0) {
                Text(
                    text = "No member data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val gymRatio = gymCount.toFloat() / total.toFloat()
                val swimmingColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val gymWidth = size.width * gymRatio
                    drawRoundRect(
                        color = NavyBlue,
                        topLeft = Offset.Zero,
                        size = Size(gymWidth, size.height),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                    drawRoundRect(
                        color = swimmingColor,
                        topLeft = Offset(gymWidth, 0f),
                        size = Size(size.width - gymWidth, size.height),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChartLegendItem(color = NavyBlue, label = "Gym", value = gymCount)
                    ChartLegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Swimming",
                        value = swimmingCount
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthReportControls(
    category: String,
    selectedMonth: YearMonth,
    monthlyMemberCount: Int,
    monthlyFees: Long,
    onMonthChange: (YearMonth) -> Unit,
    onShowMonth: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$category Month Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$monthlyMemberCount records | ${formatRupees(monthlyFees)} fees",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(0.8f),
                    onClick = { onMonthChange(selectedMonth.minusMonths(1)) }
                ) {
                    Text("<")
                }
                OutlinedButton(
                    modifier = Modifier.weight(2.2f),
                    onClick = { showMonthPicker = true }
                ) {
                    Text(
                        text = formatMonth(selectedMonth),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    modifier = Modifier.weight(0.8f),
                    onClick = { onMonthChange(selectedMonth.plusMonths(1)) }
                ) {
                    Text(">")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onShowMonth
                ) {
                    Text("Show Month")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDownloadPdf
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Download PDF")
                }
            }
        }
    }

    if (showMonthPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedMonth.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showMonthPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onMonthChange(it.toYearMonthFromPicker())
                        }
                        showMonthPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selectedFilter: ReportFilter,
    onFilterSelected: (ReportFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

private fun formatDate(date: LocalDate): String = date.format(dateFormatter)

private fun formatMonth(month: YearMonth): String = month.format(monthFormatter)

private fun formatRupees(amount: Long): String = "\u20B9$amount"

private fun List<MemberEntity>.sortedForHome(): List<MemberEntity> {
    return sortedWith(
        compareBy<MemberEntity> { it.endDateEpochDay }
            .thenBy { it.fullName.lowercase() }
    )
}

private fun MemberEntity.matchesHomeSearch(query: String): Boolean {
    val normalized = query.lowercase()
    return fullName.lowercase().contains(normalized) ||
        phoneNumber.contains(query) ||
        category.lowercase().contains(normalized)
}

private fun MemberEntity.matchesHomeFilter(filter: HomeMemberFilter): Boolean {
    return when (filter) {
        HomeMemberFilter.ALL -> true
        HomeMemberFilter.ACTIVE -> status(expiringSoonDays = 5) == MemberStatus.ACTIVE
        HomeMemberFilter.EXPIRED -> status(expiringSoonDays = 5) == MemberStatus.EXPIRED
        HomeMemberFilter.EXPIRING_SOON -> status(expiringSoonDays = 5) == MemberStatus.EXPIRING_SOON
    }
}

private fun membersForMonth(members: List<MemberEntity>, month: YearMonth): List<MemberEntity> {
    return members.filter { YearMonth.from(it.startDate()) == month }
}

private fun LocalDate.toDatePickerMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun YearMonth.toDatePickerMillis(): Long {
    return atDay(1).toDatePickerMillis()
}

private fun Long.toLocalDateFromPicker(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun Long.toYearMonthFromPicker(): YearMonth {
    return YearMonth.from(toLocalDateFromPicker())
}
