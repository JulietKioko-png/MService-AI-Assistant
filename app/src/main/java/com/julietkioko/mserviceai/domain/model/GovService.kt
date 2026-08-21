package com.julietkioko.mserviceai.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Service categories representing Kenyan government functional sectors.
 */
enum class ServiceCategory(
    val displayName: String,
    val icon: ImageVector
) {
    TAX_REVENUE("Tax & Revenue", Icons.Filled.AccountBalance),
    HEALTH_INSURANCE("Health Insurance", Icons.Filled.Favorite),
    ID_PASSPORT("ID & Passport", Icons.Filled.Badge),
    EDUCATION("Education", Icons.Filled.School),
    BUSINESS("Business", Icons.Filled.Business),
    LAND_HOUSING("Land & Housing", Icons.Filled.Apartment);

    companion object {
        fun fromNameOrNull(name: String?): ServiceCategory? {
            if (name.isNullOrBlank()) return null
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) ||
                it.displayName.equals(name, ignoreCase = true)
            }
        }
    }
}

/**
 * Domain model representing an official Kenyan public service.
 */
data class GovService(
    val id: String,
    val title: String,
    val category: ServiceCategory,
    val agency: String,
    val summary: String,
    val description: String,
    val requirements: List<String>,
    val steps: List<String>,
    val fees: String,
    val processingTime: String,
    val portalUrl: String,
    val isPopular: Boolean = false
)
