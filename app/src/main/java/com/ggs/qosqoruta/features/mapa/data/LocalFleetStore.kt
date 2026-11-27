package com.ggs.qosqoruta.features.mapa.data

import com.ggs.qosqoruta.features.mapa.domain.models.BusInfo
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class LocalFleetStore(initialFavorites: Set<String>) {
    private val _buses = MutableStateFlow(createDemoFleet(initialFavorites))
    val buses: StateFlow<List<BusInfo>> = _buses.asStateFlow()

    fun updateFavorites(favoriteIds: Set<String>) {
        _buses.update { buses ->
            buses.map { bus -> bus.copy(isFavorite = bus.id in favoriteIds) }
        }
    }

    fun advanceSimulation() {
        val updatedAt = System.currentTimeMillis()
        _buses.update { buses ->
            buses.map { bus ->
                val config = rutaMapaConfig(bus.tag)
                val routeLength = routeLengthMeters(config.polyline).coerceAtLeast(1.0)
                val nextProgress = (bus.routeProgress + (METERS_PER_TICK / routeLength).toFloat())
                    .let { if (it >= 1f) 0.02f else it }
                val position = positionAt(config.polyline, nextProgress)
                val bearing = bearingAt(config.polyline, nextProgress)
                val stops = cuscoParaderosReales.filter {
                    it.snippet.contains(config.stopSnippet, ignoreCase = true)
                }
                val nextStopIndex = nextStopIndex(config.polyline, stops.size, nextProgress)
                val nextStop = stops.getOrNull(nextStopIndex)
                val distanceMeters = nextStop?.let { distanceMeters(position, it.location) } ?: 0.0
                val etaMinutes = ceil(distanceMeters / METERS_PER_MINUTE)
                    .toInt()
                    .coerceAtLeast(1)

                bus.copy(
                    latLng = position,
                    routeProgress = nextProgress,
                    bearingDegrees = bearing,
                    nextStopName = nextStop?.name ?: "Próximo paradero",
                    distance = formatDistance(distanceMeters),
                    etaMinutes = etaMinutes,
                    eta = "$etaMinutes min",
                    lastUpdatedAtMillis = updatedAt
                )
            }
        }
    }

    private fun createDemoFleet(favoriteIds: Set<String>): List<BusInfo> {
        val miradorFirst = createDemoUnit(
            base = availableBuses.first(),
            id = "B1",
            name = "Mirador · B1",
            routeCode = "RTI-07",
            progress = 0.28f,
            etaMinutes = 4,
            favoriteIds = favoriteIds
        )
        val miradorSecond = createDemoUnit(
            base = availableBuses.first(),
            id = "M-02",
            name = "Mirador · M-02",
            routeCode = "RTI-07",
            progress = 0.68f,
            etaMinutes = 12,
            favoriteIds = favoriteIds
        )

        val otherRouteBuses = availableBuses.drop(1).mapIndexed { index, bus ->
            createDemoUnit(
                base = bus,
                id = bus.id,
                name = bus.name,
                routeCode = bus.tag,
                progress = if (index == 0) 0.42f else 0.30f,
                etaMinutes = bus.eta.substringBefore(' ').toIntOrNull() ?: 6,
                favoriteIds = favoriteIds
            )
        }

        val leonesBase = availableBuses.first()
        val leonesFirst = createDemoUnit(
            base = leonesBase,
            id = "L-01",
            name = "Los Leones · L-01",
            routeCode = "LEONES",
            progress = 0.20f,
            etaMinutes = 5,
            favoriteIds = favoriteIds
        )
        val leonesSecond = createDemoUnit(
            base = leonesBase,
            id = "L-02",
            name = "Los Leones · L-02",
            routeCode = "LEONES",
            progress = 0.72f,
            etaMinutes = 13,
            favoriteIds = favoriteIds
        )

        return listOf(miradorFirst, miradorSecond) + otherRouteBuses + listOf(leonesFirst, leonesSecond)
    }

    private fun createDemoUnit(
        base: BusInfo,
        id: String,
        name: String,
        routeCode: String,
        progress: Float,
        etaMinutes: Int,
        favoriteIds: Set<String>
    ): BusInfo {
        val config = rutaMapaConfig(routeCode)
        val position = positionAt(config.polyline, progress)
        val stops = cuscoParaderosReales.filter {
            it.snippet.contains(config.stopSnippet, ignoreCase = true)
        }
        val stopIndex = nextStopIndex(config.polyline, stops.size, progress)

        return base.copy(
            id = id,
            name = name,
            tag = routeCode,
            route = config.displayName,
            eta = "$etaMinutes min",
            distance = "Calculando distancia",
            statusText = "SIMULACIÓN LOCAL • DEMO",
            iconColor = config.color,
            driverName = "Unidad demo",
            latLng = position,
            isFavorite = id in favoriteIds,
            etaMinutes = etaMinutes,
            nextStopName = stops.getOrNull(stopIndex)?.name ?: "Próximo paradero",
            bearingDegrees = bearingAt(config.polyline, progress),
            routeProgress = progress,
            lastUpdatedAtMillis = System.currentTimeMillis(),
            isDemo = true
        )
    }

    private fun formatDistance(distanceMeters: Double): String =
        if (distanceMeters >= 1_000.0) {
            String.format(Locale.getDefault(), "%.1f km", distanceMeters / 1_000.0)
        } else {
            "A ${distanceMeters.roundToInt()} m"
        }

    private companion object {
        const val METERS_PER_TICK = 5.0
        const val METERS_PER_MINUTE = 300.0

        fun positionAt(points: List<LatLng>, progress: Float): LatLng {
            if (points.size < 2) return points.firstOrNull() ?: LatLng(0.0, 0.0)
            val targetDistance = routeLengthMeters(points) * progress.coerceIn(0f, 0.999999f)
            var traversedDistance = 0.0
            for ((start, end) in points.zipWithNext()) {
                val segmentLength = distanceMeters(start, end)
                if (traversedDistance + segmentLength >= targetDistance) {
                    val fraction = ((targetDistance - traversedDistance) / segmentLength)
                        .coerceIn(0.0, 1.0)
                    return LatLng(
                        start.latitude + (end.latitude - start.latitude) * fraction,
                        start.longitude + (end.longitude - start.longitude) * fraction
                    )
                }
                traversedDistance += segmentLength
            }
            return points.last()
        }

        fun bearingAt(points: List<LatLng>, progress: Float): Float {
            if (points.size < 2) return 0f
            val targetDistance = routeLengthMeters(points) * progress.coerceIn(0f, 0.999999f)
            var traversedDistance = 0.0
            var start = points.first()
            var end = points[1]
            for ((segmentStart, segmentEnd) in points.zipWithNext()) {
                start = segmentStart
                end = segmentEnd
                val segmentLength = distanceMeters(start, end)
                if (traversedDistance + segmentLength >= targetDistance) break
                traversedDistance += segmentLength
            }
            val lat1 = Math.toRadians(start.latitude)
            val lat2 = Math.toRadians(end.latitude)
            val deltaLng = Math.toRadians(end.longitude - start.longitude)
            val y = sin(deltaLng) * cos(lat2)
            val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)
            return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
        }

        fun nextStopIndex(points: List<LatLng>, stopCount: Int, progress: Float): Int {
            val lastIndex = (stopCount - 1).coerceAtLeast(0)
            if (lastIndex == 0 || points.size < 2) return 0
            val routeLength = routeLengthMeters(points).coerceAtLeast(1.0)
            for (index in 1..lastIndex.coerceAtMost(points.lastIndex)) {
                val stopProgress = routeLengthMeters(points.take(index + 1)) / routeLength
                if (stopProgress > progress) return index
            }
            return lastIndex
        }

        fun routeLengthMeters(points: List<LatLng>): Double =
            points.zipWithNext().sumOf { (start, end) -> distanceMeters(start, end) }

        fun distanceMeters(start: LatLng, end: LatLng): Double {
            val earthRadiusMeters = 6_371_000.0
            val deltaLat = Math.toRadians(end.latitude - start.latitude)
            val deltaLng = Math.toRadians(end.longitude - start.longitude)
            val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(Math.toRadians(start.latitude)) *
                cos(Math.toRadians(end.latitude)) *
                sin(deltaLng / 2) * sin(deltaLng / 2)
            val boundedA = a.coerceIn(0.0, 1.0)
            return earthRadiusMeters * 2 * atan2(sqrt(boundedA), sqrt(1 - boundedA))
        }
    }
}
