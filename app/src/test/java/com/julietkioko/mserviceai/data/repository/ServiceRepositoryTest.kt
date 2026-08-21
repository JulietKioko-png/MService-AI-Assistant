package com.julietkioko.mserviceai.data.repository

import com.julietkioko.mserviceai.domain.model.ServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ServiceRepositoryTest {

    private lateinit var repository: InMemoryServiceRepository

    @Before
    fun setUp() {
        repository = InMemoryServiceRepository()
    }

    // ── Retrieval Tests ─────────────────────────────────────────────────

    @Test
    fun `getAllServices returns all 10 catalog services`() {
        val all = repository.getAllServices()
        assertEquals(10, all.size)
    }

    @Test
    fun `getAllServices contains unique IDs`() {
        val all = repository.getAllServices()
        val uniqueIds = all.map { it.id }.toSet()
        assertEquals(all.size, uniqueIds.size)
    }

    // ── Category Filtering Tests ────────────────────────────────────────

    @Test
    fun `getServicesByCategory for TAX_REVENUE returns tax services`() {
        val taxServices = repository.getServicesByCategory(ServiceCategory.TAX_REVENUE)
        assertFalse(taxServices.isEmpty())
        assertTrue(taxServices.all { it.category == ServiceCategory.TAX_REVENUE })
        assertTrue(taxServices.any { it.id == "kra-pin-registration" })
        assertTrue(taxServices.any { it.id == "kra-itr-filing" })
    }

    @Test
    fun `getServicesByCategory for HEALTH_INSURANCE returns health services`() {
        val healthServices = repository.getServicesByCategory(ServiceCategory.HEALTH_INSURANCE)
        assertFalse(healthServices.isEmpty())
        assertTrue(healthServices.all { it.category == ServiceCategory.HEALTH_INSURANCE })
        assertTrue(healthServices.any { it.id == "sha-shif-registration" })
    }

    @Test
    fun `getServicesByCategory for ID_PASSPORT returns identity services`() {
        val idServices = repository.getServicesByCategory(ServiceCategory.ID_PASSPORT)
        assertFalse(idServices.isEmpty())
        assertTrue(idServices.all { it.category == ServiceCategory.ID_PASSPORT })
        assertTrue(idServices.any { it.id == "passport-application-renewal" })
        assertTrue(idServices.any { it.id == "national-id-card" })
    }

    @Test
    fun `getServicesByCategory for EDUCATION returns education services`() {
        val eduServices = repository.getServicesByCategory(ServiceCategory.EDUCATION)
        assertFalse(eduServices.isEmpty())
        assertTrue(eduServices.all { it.category == ServiceCategory.EDUCATION })
        assertTrue(eduServices.any { it.id == "helb-loan-application" })
    }

    // ── Search Tests ────────────────────────────────────────────────────

    @Test
    fun `searchServices matches service title`() {
        val results = repository.searchServices("Passport")
        assertFalse(results.isEmpty())
        assertTrue(results.any { it.title.contains("Passport", ignoreCase = true) })
    }

    @Test
    fun `searchServices matches agency name`() {
        val kraResults = repository.searchServices("Kenya Revenue Authority")
        assertFalse(kraResults.isEmpty())
        assertTrue(kraResults.all { it.agency.contains("Kenya Revenue Authority", ignoreCase = true) })
        assertTrue(kraResults.any { it.id == "kra-pin-registration" })

        val dciResults = repository.searchServices("DCI")
        assertFalse(dciResults.isEmpty())
        assertTrue(dciResults.any { it.id == "police-clearance-good-conduct" })
    }

    @Test
    fun `searchServices matches summary text`() {
        val results = repository.searchServices("universal healthcare")
        assertEquals(1, results.size)
        assertEquals("sha-shif-registration", results.first().id)
    }

    @Test
    fun `searchServices matches requirements`() {
        val results = repository.searchServices("KCSE Index")
        assertEquals(1, results.size)
        assertEquals("helb-loan-application", results.first().id)
    }

    @Test
    fun `searchServices is case-insensitive`() {
        val lower = repository.searchServices("kra pin")
        val upper = repository.searchServices("KRA PIN")
        val mixed = repository.searchServices("kRa PiN")

        assertEquals(lower.size, upper.size)
        assertEquals(lower.size, mixed.size)
        assertFalse(lower.isEmpty())
    }

    @Test
    fun `searchServices with no-match returns empty list`() {
        val results = repository.searchServices("NonExistentServiceXyz123")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `searchServices with empty string returns all services`() {
        val results = repository.searchServices("")
        assertEquals(10, results.size)
    }

    @Test
    fun `searchServices with whitespace-only returns all services`() {
        val results = repository.searchServices("   ")
        assertEquals(10, results.size)
    }

    // ── ID Lookup Tests ─────────────────────────────────────────────────

    @Test
    fun `getServiceById with valid ID returns matching service`() {
        val service = repository.getServiceById("kra-pin-registration")
        assertNotNull(service)
        assertEquals("KRA PIN Registration (Individual)", service?.title)
        assertEquals(ServiceCategory.TAX_REVENUE, service?.category)
    }

    @Test
    fun `getServiceById with invalid ID returns null`() {
        val service = repository.getServiceById("non-existent-id")
        assertNull(service)
    }
}
