package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val securityPreferences: SecurityPreferences
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun insertProduct(title: String, category: String, color: String): Long {
        val product = Product(
            title = title.trim(),
            category = category.trim(),
            color = color.trim()
        )
        return productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteById(id: Long) {
        productDao.deleteById(id)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    fun verifyAdminPassword(password: String): Boolean {
        return securityPreferences.verifyPassword(password)
    }

    fun changeAdminPassword(oldPass: String, newPass: String): Boolean {
        return securityPreferences.updatePassword(oldPass, newPass)
    }
}
