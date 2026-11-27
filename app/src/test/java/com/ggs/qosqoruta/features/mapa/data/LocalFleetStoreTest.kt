package com.ggs.qosqoruta.features.mapa.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFleetStoreTest {
    @Test
    fun createsIndependentDemoUnitsForMiradorAndLosLeones() {
        val store = LocalFleetStore(setOf("B1"))
        val initial = store.buses.value
        val mirador = initial.filter { it.tag == "RTI-07" }
        val leones = initial.filter { it.tag == "LEONES" }

        assertEquals(2, mirador.size)
        assertEquals(2, leones.size)
        assertEquals(initial.size, initial.map { it.id }.distinct().size)
        assertTrue(initial.all { it.isDemo && it.etaMinutes > 0 })
        assertTrue(initial.first { it.id == "B1" }.isFavorite)
        assertNotEquals(mirador[0].latLng, mirador[1].latLng)
    }

    @Test
    fun advancesEachUnitPositionAndEtaFromItsOwnRouteProgress() {
        val store = LocalFleetStore(emptySet())
        val initial = store.buses.value.associateBy { it.id }

        store.advanceSimulation()

        val updated = store.buses.value.associateBy { it.id }
        initial.forEach { (id, bus) ->
            val movedBus = updated.getValue(id)
            assertNotEquals(bus.latLng, movedBus.latLng)
            assertTrue(movedBus.etaMinutes > 0)
            assertTrue(movedBus.lastUpdatedAtMillis >= bus.lastUpdatedAtMillis)
        }
    }
}
