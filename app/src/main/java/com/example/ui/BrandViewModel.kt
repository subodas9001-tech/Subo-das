package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiAlert(
    val title: String,
    val message: String,
    val isSuccess: Boolean = false
)

class BrandViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    // Admin authentication state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Form inputs matching the React Native code
    private val _passwordInput = MutableStateFlow("")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _productTitle = MutableStateFlow("")
    val productTitle: StateFlow<String> = _productTitle.asStateFlow()

    private val _productCategory = MutableStateFlow("")
    val productCategory: StateFlow<String> = _productCategory.asStateFlow()

    private val _productColor = MutableStateFlow("")
    val productColor: StateFlow<String> = _productColor.asStateFlow()

    // Search and category filter for catalog
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Dialog & Alert states matching Alert.alert(...)
    private val _uiAlert = MutableStateFlow<UiAlert?>(null)
    val uiAlert: StateFlow<UiAlert?> = _uiAlert.asStateFlow()

    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _productToDelete = MutableStateFlow<Product?>(null)
    val productToDelete: StateFlow<Product?> = _productToDelete.asStateFlow()

    // Raw products from Room
    private val allProductsFlow = repository.allProducts

    // Filtered products list for reactive display
    val products: StateFlow<List<Product>> = combine(
        allProductsFlow,
        _searchQuery,
        _selectedCategory
    ) { rawProducts, query, category ->
        rawProducts.filter { product ->
            val matchesQuery = query.isBlank() ||
                product.title.contains(query, ignoreCase = true) ||
                product.category.contains(query, ignoreCase = true) ||
                product.color.contains(query, ignoreCase = true)

            val matchesCategory = category == null ||
                product.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onPasswordInputChange(value: String) {
        _passwordInput.value = value
    }

    fun onProductTitleChange(value: String) {
        _productTitle.value = value
    }

    fun onProductCategoryChange(value: String) {
        _productCategory.value = value
    }

    fun onProductColorChange(value: String) {
        _productColor.value = value
    }

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun openLoginDialog() {
        _passwordInput.value = ""
        _showLoginDialog.value = true
    }

    fun closeLoginDialog() {
        _showLoginDialog.value = false
        _passwordInput.value = ""
    }

    fun handleLogin() {
        val entered = _passwordInput.value
        if (repository.verifyAdminPassword(entered)) {
            _isLoggedIn.value = true
            _showLoginDialog.value = false
            _passwordInput.value = ""
            _uiAlert.value = UiAlert(
                title = "সফল!",
                message = "আপনি সফলভাবে অ্যাডমিন হিসেবে লগইন করেছেন।",
                isSuccess = true
            )
        } else {
            _uiAlert.value = UiAlert(
                title = "ভুল পাসওয়ার্ড",
                message = "সঠিক পাসওয়ার্ড দিন।",
                isSuccess = false
            )
        }
    }

    fun handleLogout() {
        _isLoggedIn.value = false
        _uiAlert.value = UiAlert(
            title = "লগআউট",
            message = "অ্যাডমিন সেশন সফলভাবে সমাপ্ত হয়েছে।",
            isSuccess = true
        )
    }

    fun handlePostProduct() {
        val title = _productTitle.value.trim()
        val category = _productCategory.value.trim()
        val color = _productColor.value.trim()

        if (title.isEmpty() || category.isEmpty() || color.isEmpty()) {
            _uiAlert.value = UiAlert(
                title = "ভুল",
                message = "দয়া করে সব ঘর পূরণ করুন।",
                isSuccess = false
            )
            return
        }

        viewModelScope.launch {
            repository.insertProduct(title, category, color)
            _productTitle.value = ""
            _productCategory.value = ""
            _productColor.value = ""
            _uiAlert.value = UiAlert(
                title = "সফল!",
                message = "নতুন স্টাইল সফলভাবে পোস্ট করা হয়েছে।",
                isSuccess = true
            )
        }
    }

    fun requestDeleteProduct(product: Product) {
        _productToDelete.value = product
    }

    fun dismissDeleteProduct() {
        _productToDelete.value = null
    }

    fun confirmDeleteProduct() {
        val product = _productToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteProduct(product)
            _productToDelete.value = null
            _uiAlert.value = UiAlert(
                title = "মুছে ফেলা হয়েছে",
                message = "${product.title} সফলভাবে তালিকা থেকে মুছে ফেলা হয়েছে।",
                isSuccess = true
            )
        }
    }

    fun openChangePasswordDialog() {
        _showChangePasswordDialog.value = true
    }

    fun closeChangePasswordDialog() {
        _showChangePasswordDialog.value = false
    }

    fun handleChangePassword(currentPass: String, newPass: String): Boolean {
        val success = repository.changeAdminPassword(currentPass, newPass)
        if (success) {
            _showChangePasswordDialog.value = false
            _uiAlert.value = UiAlert(
                title = "পাসওয়ার্ড আপডেট",
                message = "অ্যাডমিন পাসওয়ার্ড সফলভাবে পরিবর্তিত হয়েছে। নতুন পাসওয়ার্ড মনে রাখুন।",
                isSuccess = true
            )
        } else {
            _uiAlert.value = UiAlert(
                title = "ব্যর্থ হয়েছে",
                message = "বর্তমান পাসওয়ার্ডটি সঠিক নয় অথবা নতুন পাসওয়ার্ডটি ফাঁকা।",
                isSuccess = false
            )
        }
        return success
    }

    fun dismissAlert() {
        _uiAlert.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BrandViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val securityPrefs = SecurityPreferences(application)
                val repository = ProductRepository(db.productDao(), securityPrefs)
                return BrandViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
