package com.julietkioko.mserviceai.domain.repository

import com.julietkioko.mserviceai.domain.model.GovService
import com.julietkioko.mserviceai.domain.model.ServiceCategory

/**
 * Contract for accessing and querying government service records.
 */
interface ServiceRepository {
    fun getAllServices(): List<GovService>
    fun getServicesByCategory(category: ServiceCategory): List<GovService>
    fun searchServices(query: String): List<GovService>
    fun getServiceById(id: String): GovService?
}
