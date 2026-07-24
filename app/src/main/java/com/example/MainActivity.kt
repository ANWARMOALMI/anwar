package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ProductWithStore
import com.example.ui.components.InteractiveMapDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.StoreOwnerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FindBeforeGoApp()
                }
            }
        }
    }
}

enum class NavigationTab {
    HOME,
    FAVORITES,
    STORE_OWNER,
    ADMIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindBeforeGoApp(
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var activeMapItem by remember { mutableStateOf<ProductWithStore?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val onlyAvailable by viewModel.onlyAvailable.collectAsStateWithLifecycle()
    val displayCurrency by viewModel.displayCurrency.collectAsStateWithLifecycle()
    val favoriteProducts by viewModel.favoriteProducts.collectAsStateWithLifecycle()
    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle()
    val allStores by viewModel.allStores.collectAsStateWithLifecycle()
    val importReports by viewModel.importReports.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            if (selectedProductId == null) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ابحث قبل أن تذهب",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    },
                    actions = {
                        // Role Selector Pills
                        Surface(
                            onClick = {
                                val nextRole = when (userRole) {
                                    "USER" -> "STORE_OWNER"
                                    "STORE_OWNER" -> "ADMIN"
                                    else -> "USER"
                                }
                                viewModel.setUserRole(nextRole)
                                if (nextRole == "STORE_OWNER") selectedTab = NavigationTab.STORE_OWNER
                                if (nextRole == "ADMIN") selectedTab = NavigationTab.ADMIN
                                if (nextRole == "USER") selectedTab = NavigationTab.HOME
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 12.dp).testTag("role_switcher_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val label = when (userRole) {
                                    "STORE_OWNER" -> "صاحب متجر 🏪"
                                    "ADMIN" -> "مدير النظام ⚙️"
                                    else -> "المستخدم 👤"
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (selectedProductId == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == NavigationTab.HOME,
                        onClick = { selectedTab = NavigationTab.HOME },
                        icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "البحث") },
                        label = { Text("البحث") },
                        modifier = Modifier.testTag("tab_home"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == NavigationTab.FAVORITES,
                        onClick = { selectedTab = NavigationTab.FAVORITES },
                        icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "المفضلة") },
                        label = { Text("المفضلة") },
                        modifier = Modifier.testTag("tab_favorites"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == NavigationTab.STORE_OWNER,
                        onClick = { selectedTab = NavigationTab.STORE_OWNER },
                        icon = { Icon(imageVector = Icons.Default.Store, contentDescription = "المتجر") },
                        label = { Text("المتجر") },
                        modifier = Modifier.testTag("tab_store"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == NavigationTab.ADMIN,
                        onClick = { selectedTab = NavigationTab.ADMIN },
                        icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "الإدارة") },
                        label = { Text("الإدارة") },
                        modifier = Modifier.testTag("tab_admin"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedProductId != null) {
                ProductDetailScreen(
                    productId = selectedProductId!!,
                    viewModel = viewModel,
                    onBackClick = { selectedProductId = null },
                    onOpenMap = { activeMapItem = it },
                    onProductClick = { selectedProductId = it }
                )
            } else {
                when (selectedTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            searchResults = searchResults,
                            categories = categories,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            selectedCity = selectedCity,
                            onlyAvailable = onlyAvailable,
                            displayCurrency = displayCurrency,
                            onProductClick = { selectedProductId = it },
                            onOpenMap = { activeMapItem = it }
                        )
                    }
                    NavigationTab.FAVORITES -> {
                        FavoritesScreen(
                            viewModel = viewModel,
                            favoriteProducts = favoriteProducts,
                            onProductClick = { selectedProductId = it },
                            onOpenMap = { activeMapItem = it }
                        )
                    }
                    NavigationTab.STORE_OWNER -> {
                        StoreOwnerScreen(
                            viewModel = viewModel,
                            allStores = allStores,
                            importReports = importReports
                        )
                    }
                    NavigationTab.ADMIN -> {
                        AdminScreen(
                            viewModel = viewModel,
                            exchangeRates = exchangeRates,
                            allStores = allStores
                        )
                    }
                }
            }

            // Interactive Map Dialog
            activeMapItem?.let { item ->
                InteractiveMapDialog(
                    item = item,
                    onDismiss = { activeMapItem = null }
                )
            }
        }
    }
}
