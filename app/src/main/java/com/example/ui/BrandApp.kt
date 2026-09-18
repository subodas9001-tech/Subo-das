package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AdminLoginDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.example.ui.components.AlertDialogBox
import com.example.ui.components.ChangePasswordDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.screens.AdminDashboardView
import com.example.ui.screens.PublicCatalogView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandApp(
    viewModel: BrandViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val passwordInput by viewModel.passwordInput.collectAsStateWithLifecycle()
    val productTitle by viewModel.productTitle.collectAsStateWithLifecycle()
    val productCategory by viewModel.productCategory.collectAsStateWithLifecycle()
    val productColor by viewModel.productColor.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val uiAlert by viewModel.uiAlert.collectAsStateWithLifecycle()
    val showLoginDialog by viewModel.showLoginDialog.collectAsStateWithLifecycle()
    val showChangePasswordDialog by viewModel.showChangePasswordDialog.collectAsStateWithLifecycle()
    val productToDelete by viewModel.productToDelete.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) } // 0 = Public Catalog, 1 = Admin Panel

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_brand_logo),
                            contentDescription = "Brand Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Brand Style",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ব্র্যান্ড ফ্যাশন হাব",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "অ্যাডমিন সক্রিয়",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "ক্যাটালগ"
                        )
                    },
                    label = { Text("ক্যাটালগ") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_public_catalog")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = {
                        if (isLoggedIn) {
                            currentTab = 1
                        } else {
                            viewModel.openLoginDialog()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isLoggedIn) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                            contentDescription = "অ্যাডমিন প্যানেল"
                        )
                    },
                    label = {
                        Text(if (isLoggedIn) "অ্যাডমিন" else "অ্যাডমিন লগইন")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_admin_panel")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (isLoggedIn) {
                            currentTab = 1
                        } else {
                            viewModel.openLoginDialog()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "স্টাইল পোস্ট করুন"
                        )
                    },
                    text = {
                        Text(text = if (isLoggedIn) "নতুন পোস্ট" else "অ্যাডমিন পোস্ট")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_style")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tabIndex ->
                if (tabIndex == 0) {
                    PublicCatalogView(
                        products = products,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchChange = viewModel::onSearchQueryChange,
                        onCategorySelect = viewModel::onCategorySelected,
                        onOpenAdminLogin = {
                            if (isLoggedIn) {
                                currentTab = 1
                            } else {
                                viewModel.openLoginDialog()
                            }
                        }
                    )
                } else {
                    AdminDashboardView(
                        productTitle = productTitle,
                        productCategory = productCategory,
                        productColor = productColor,
                        products = products,
                        onTitleChange = viewModel::onProductTitleChange,
                        onCategoryChange = viewModel::onProductCategoryChange,
                        onColorChange = viewModel::onProductColorChange,
                        onPostProduct = viewModel::handlePostProduct,
                        onDeleteProduct = viewModel::requestDeleteProduct,
                        onChangePasswordClick = viewModel::openChangePasswordDialog,
                        onLogoutClick = {
                            viewModel.handleLogout()
                            currentTab = 0
                        }
                    )
                }
            }
        }
    }

    // Admin Login Dialog
    if (showLoginDialog) {
        AdminLoginDialog(
            passwordInput = passwordInput,
            onPasswordChange = viewModel::onPasswordInputChange,
            onLogin = {
                viewModel.handleLogin()
                if (viewModel.isLoggedIn.value) {
                    currentTab = 1
                }
            },
            onDismiss = viewModel::closeLoginDialog
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onChangePassword = { oldPass, newPass ->
                viewModel.handleChangePassword(oldPass, newPass)
            },
            onDismiss = viewModel::closeChangePasswordDialog
        )
    }

    // Delete Product Confirmation Dialog
    productToDelete?.let { product ->
        DeleteConfirmDialog(
            product = product,
            onConfirm = viewModel::confirmDeleteProduct,
            onDismiss = viewModel::dismissDeleteProduct
        )
    }

    // Alert Dialog (matching React Native Alert.alert)
    uiAlert?.let { alert ->
        AlertDialogBox(
            alert = alert,
            onDismiss = viewModel::dismissAlert
        )
    }
}
