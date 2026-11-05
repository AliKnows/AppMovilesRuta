package com.ggs.qosqoruta.features.mapa.data

import androidx.compose.ui.graphics.Color
import com.ggs.qosqoruta.features.mapa.domain.models.BusInfo
import com.ggs.qosqoruta.features.mapa.domain.models.Paradero
import com.ggs.qosqoruta.features.mapa.domain.models.RutaDetalle
import com.google.android.gms.maps.model.LatLng

// ---------------------------------------------------------
// PARADEROS REALES DE CUSCO
// ---------------------------------------------------------
val cuscoParaderosReales = listOf(
    // 1. Empresa Nuevo Mirador S.A. (RTI-07)
    Paradero("P01", "Final Ccachona", "RTI-07 - Nuevo Mirador", LatLng(-13.541250, -71.985530)),
    Paradero("P02", "Octavo Paradero (Cachona)", "RTI-07 - Nuevo Mirador", LatLng(-13.539015, -71.983010)),
    Paradero("P03", "Mercado de Huancaro", "RTI-07 - Nuevo Mirador", LatLng(-13.532588, -71.979720)),
    Paradero("P04", "Belempampa", "RTI-07 - Nuevo Mirador", LatLng(-13.528012, -71.981045)),
    Paradero("P05", "Colegio Rosario", "RTI-07 - Nuevo Mirador", LatLng(-13.526540, -71.978010)),
    Paradero("P06", "Óvalo Pachacútec", "RTI-07 - Nuevo Mirador", LatLng(-13.524580, -71.973050)),
    Paradero("P07", "Terminal Terrestre", "RTI-07 - Nuevo Mirador", LatLng(-13.527815, -71.969520)),
    Paradero("P08", "Coliseo Uriel García", "RTI-07 - Nuevo Mirador", LatLng(-13.526033, -71.958010)),
    Paradero("P09", "Ingreso Vía Expresa", "RTI-07 - Nuevo Mirador", LatLng(-13.528540, -71.953020)),
    Paradero("P10", "República de Brasil", "RTI-07 - Nuevo Mirador", LatLng(-13.529010, -71.948030)),
    Paradero("P11", "Primer Paradero San Sebastián", "RTI-07 - Nuevo Mirador", LatLng(-13.530015, -71.938010)),
    Paradero("P12", "Plaza de San Sebastián", "RTI-07 - Nuevo Mirador", LatLng(-13.530345, -71.933315)),
    Paradero("P13", "Séptimo Paradero San Sebastián", "RTI-07 - Nuevo Mirador", LatLng(-13.531520, -71.930050)),
    Paradero("P14", "Mercadillo San Sebastián", "RTI-07 - Nuevo Mirador", LatLng(-13.532050, -71.928040)),
    Paradero("P15", "Universidad Andina (UAC - Larapa)", "RTI-07 - Nuevo Mirador", LatLng(-13.537500, -71.903880)),
    Paradero("P16", "Penal (San Jerónimo)", "RTI-07 - Nuevo Mirador", LatLng(-13.536540, -71.898015)),
    Paradero("P17", "Mercado Vinocanchón", "RTI-07 - Nuevo Mirador", LatLng(-13.538055, -71.890015)),
    Paradero("P18", "La Encantada / Angostura", "RTI-07 - Nuevo Mirador", LatLng(-13.545040, -71.870020)),
    Paradero("P19", "Estadio de Saylla", "RTI-07 - Nuevo Mirador", LatLng(-13.555010, -71.850050)),

    // 2. Empresa Patrón de San Jerónimo S.A. (RTU-01)
    Paradero("P20", "Mirador Domingo Yupanqui", "RTU-01 - Patrón San Jerónimo", LatLng(-13.518020, -71.990030)),
    Paradero("P21", "Mercado de Huancaro", "RTU-01 - Patrón San Jerónimo", LatLng(-13.532588, -71.979720)),
    Paradero("P22", "Plaza Santiago", "RTU-01 - Patrón San Jerónimo", LatLng(-13.521540, -71.982050)),
    Paradero("P23", "Puente Grau", "RTU-01 - Patrón San Jerónimo", LatLng(-13.520015, -71.978020)),
    Paradero("P24", "Calle San Miguel", "RTU-01 - Patrón San Jerónimo", LatLng(-13.519020, -71.975030)),
    Paradero("P25", "Paradero Garcilaso", "RTU-01 - Patrón San Jerónimo", LatLng(-13.521055, -71.970040)),
    Paradero("P26", "UNSAAC (Puerta Principal)", "RTU-01 - Patrón San Jerónimo", LatLng(-13.522660, -71.958610)),
    Paradero("P27", "Hospital Regional", "RTU-01 - Patrón San Jerónimo", LatLng(-13.523330, -71.955000)),
    Paradero("P28", "Manuel Prado", "RTU-01 - Patrón San Jerónimo", LatLng(-13.524510, -71.951520)),
    Paradero("P29", "Magisterio", "RTU-01 - Patrón San Jerónimo", LatLng(-13.526020, -71.948030)),
    Paradero("P30", "Marcavalle", "RTU-01 - Patrón San Jerónimo", LatLng(-13.527015, -71.944040)),
    Paradero("P31", "Primer Paradero San Sebastián", "RTU-01 - Patrón San Jerónimo", LatLng(-13.530015, -71.938010)),
    Paradero("P32", "Séptimo Paradero San Sebastián", "RTU-01 - Patrón San Jerónimo", LatLng(-13.531520, -71.930050)),
    Paradero("P33", "Paradero Penal", "RTU-01 - Patrón San Jerónimo", LatLng(-13.536540, -71.898015)),
    Paradero("P34", "Mercado Vinocanchón", "RTU-01 - Patrón San Jerónimo", LatLng(-13.538055, -71.890015)),
    Paradero("P35", "Ciencias de la Salud UAC (Kayra)", "RTU-01 - Patrón San Jerónimo", LatLng(-13.537020, -71.885030)),
    Paradero("P36", "Universidad Continental", "RTU-01 - Patrón San Jerónimo", LatLng(-13.535900, -71.895500)),

    // 3. Empresa Saylla Huasao (RTI-01)
    Paradero("P37", "Luis Vallejo Santoni (Zarzuela)", "RTI-01 - Saylla Huasao", LatLng(-13.530015, -71.975020)),
    Paradero("P38", "Mercado Wanchaq", "RTI-01 - Saylla Huasao", LatLng(-13.525030, -71.965040)),
    Paradero("P39", "Mariscal Gamarra", "RTI-01 - Saylla Huasao", LatLng(-13.524045, -71.960010)),
    Paradero("P40", "UNSAAC", "RTI-01 - Saylla Huasao", LatLng(-13.521660, -71.958610)),
    Paradero("P41", "Hospital Regional", "RTI-01 - Saylla Huasao", LatLng(-13.523330, -71.955000)),
    Paradero("P42", "Primer Paradero San Sebastián", "RTI-01 - Saylla Huasao", LatLng(-13.530015, -71.938010)),
    Paradero("P43", "Séptimo Paradero San Sebastián", "RTI-01 - Saylla Huasao", LatLng(-13.531520, -71.930050)),
    Paradero("P44", "Universidad Andina (UAC - Larapa)", "RTI-01 - Saylla Huasao", LatLng(-13.537500, -71.903880)),
    Paradero("P45", "Paradero Penal", "RTI-01 - Saylla Huasao", LatLng(-13.536540, -71.898015)),
    Paradero("P46", "Mercado Vinocanchón", "RTI-01 - Saylla Huasao", LatLng(-13.538055, -71.890015)),
    Paradero("P47", "Ciencias de la Salud UAC (Kayra)", "RTI-01 - Saylla Huasao", LatLng(-13.537020, -71.885030)),
    Paradero("P48", "La Encantada / Angostura", "RTI-01 - Saylla Huasao", LatLng(-13.545040, -71.870020)),
    Paradero("P49", "Estadio de Saylla", "RTI-01 - Saylla Huasao", LatLng(-13.555010, -71.850050)),
    Paradero("P50", "Huasao (Terminal Valle Sur)", "RTI-01 - Saylla Huasao", LatLng(-13.560040, -71.820050)),

    // 4. Empresa Los Leones
    Paradero("P51", "Plaza de Tipón / Choquepata", "Los Leones - Centro Histórico", LatLng(-13.570020, -71.780015)),
    Paradero("P52", "Huasao", "Los Leones - Centro Histórico", LatLng(-13.560040, -71.820050)),
    Paradero("P53", "Estadio de Saylla", "Los Leones - Centro Histórico", LatLng(-13.555010, -71.850050)),
    Paradero("P54", "Ciencias de la Salud UAC (Kayra)", "Los Leones - Centro Histórico", LatLng(-13.537020, -71.885030)),
    Paradero("P55", "Paradero Penal", "Los Leones - Centro Histórico", LatLng(-13.536540, -71.898015)),
    Paradero("P56", "Universidad Andina (UAC - Larapa)", "Los Leones - Centro Histórico", LatLng(-13.537500, -71.903880)),
    Paradero("P57", "Séptimo Paradero San Sebastián", "Los Leones - Centro Histórico", LatLng(-13.531520, -71.930050)),
    Paradero("P58", "UNSAAC", "Los Leones - Centro Histórico", LatLng(-13.521660, -71.958610)),
    Paradero("P59", "Limacpampa", "Los Leones - Centro Histórico", LatLng(-13.521045, -71.972030)),
    Paradero("P60", "Avenida El Sol (Maruri)", "Los Leones - Centro Histórico", LatLng(-13.519520, -71.975540)),
    Paradero("P61", "Plaza San Francisco", "Los Leones - Centro Histórico", LatLng(-13.517770, -71.978330)),
    Paradero("P62", "Santa Teresa", "Los Leones - Centro Histórico", LatLng(-13.515030, -71.980040)),
    Paradero("P63", "Don Bosco / Saphi (Final)", "Los Leones - Centro Histórico", LatLng(-13.513010, -71.982020))
)

// ---------------------------------------------------------
// COORDENADAS REALES DE LAS RUTAS DE CUSCO
// ---------------------------------------------------------
val rti07NuevoMiradorPolyline = listOf(
    LatLng(-13.541250, -71.985530),
    LatLng(-13.539015, -71.983010),
    LatLng(-13.532588, -71.979720),
    LatLng(-13.528012, -71.981045),
    LatLng(-13.526540, -71.978010),
    LatLng(-13.524580, -71.973050),
    LatLng(-13.527815, -71.969520),
    LatLng(-13.526033, -71.958010),
    LatLng(-13.528540, -71.953020),
    LatLng(-13.529010, -71.948030),
    LatLng(-13.530015, -71.938010),
    LatLng(-13.530345, -71.933315),
    LatLng(-13.531520, -71.930050),
    LatLng(-13.532050, -71.928040),
    LatLng(-13.537500, -71.903880),
    LatLng(-13.536540, -71.898015),
    LatLng(-13.538055, -71.890015),
    LatLng(-13.545040, -71.870020),
    LatLng(-13.555010, -71.850050)
)

val rtu01PatronPolyline = listOf(
    LatLng(-13.518020, -71.990030),
    LatLng(-13.532588, -71.979720),
    LatLng(-13.521540, -71.982050),
    LatLng(-13.520015, -71.978020),
    LatLng(-13.519020, -71.975030),
    LatLng(-13.521055, -71.970040),
    LatLng(-13.522660, -71.958610),
    LatLng(-13.523330, -71.955000),
    LatLng(-13.524510, -71.951520),
    LatLng(-13.526020, -71.948030),
    LatLng(-13.527015, -71.944040),
    LatLng(-13.530015, -71.938010),
    LatLng(-13.531520, -71.930050),
    LatLng(-13.536540, -71.898015),
    LatLng(-13.538055, -71.890015),
    LatLng(-13.537020, -71.885030),
    LatLng(-13.535900, -71.895500)
)

val rti01SayllaHuasaoPolyline = listOf(
    LatLng(-13.530015, -71.975020),
    LatLng(-13.525030, -71.965040),
    LatLng(-13.524045, -71.960010),
    LatLng(-13.521660, -71.958610),
    LatLng(-13.523330, -71.955000),
    LatLng(-13.530015, -71.938010),
    LatLng(-13.531520, -71.930050),
    LatLng(-13.537500, -71.903880),
    LatLng(-13.536540, -71.898015),
    LatLng(-13.538055, -71.890015),
    LatLng(-13.537020, -71.885030),
    LatLng(-13.545040, -71.870020),
    LatLng(-13.555010, -71.850050),
    LatLng(-13.560040, -71.820050)
)

val losLeonesPolyline = listOf(
    LatLng(-13.570020, -71.780015),
    LatLng(-13.560040, -71.820050),
    LatLng(-13.555010, -71.850050),
    LatLng(-13.537020, -71.885030),
    LatLng(-13.536540, -71.898015),
    LatLng(-13.537500, -71.903880),
    LatLng(-13.531520, -71.930050),
    LatLng(-13.521660, -71.958610),
    LatLng(-13.521045, -71.972030),
    LatLng(-13.519520, -71.975540),
    LatLng(-13.517770, -71.978330),
    LatLng(-13.515030, -71.980040),
    LatLng(-13.513010, -71.982020)
)

// ---------------------------------------------------------
// RUTAS Y BUSES DETALLADOS EN CUSCO
// ---------------------------------------------------------
val cuscoRutasList = listOf(
    RutaDetalle(
        code = "RTI-07",
        name = "Expreso Batman / Nuevo Mirador",
        company = "Empresa Nuevo Mirador S.A.",
        itinerary = "Ccachona ➔ Huancaro ➔ Óvalo Pachacuteq ➔ Saylla",
        description = "Atraviesa el Eje Diagonal desde Ccachona, Huancaro, Belempampa, Óvalo Pachacuteq, Terminal Terrestre, San Sebastián, UAC hasta Saylla.",
        price = "S/. 1.00",
        frequency = "Cada 4 min",
        activeBusesCount = 2
    ),
    RutaDetalle(
        code = "RTU-01",
        name = "Patrón de San Jerónimo",
        company = "Empresa Patrón de San Jerónimo S.A.",
        itinerary = "Santiago ➔ Puente Grau ➔ UNSAAC ➔ Univ. Continental",
        description = "Conecta el Clúster Académico desde Mirador Domingo Yupanqui, Plaza Santiago, Garcilaso, UNSAAC, Magisterio hasta Univ. Continental.",
        price = "S/. 1.50",
        frequency = "Cada 6 min",
        activeBusesCount = 2
    ),
    RutaDetalle(
        code = "RTI-01",
        name = "Conector Valle Sur",
        company = "Empresa Saylla Huasao",
        itinerary = "Vallejo Santoni ➔ Wanchaq ➔ UAC ➔ Huasao",
        description = "Conector del Valle Sur pasando por Mercado Wanchaq, Mariscal Gamarra, Ciencias de la Salud UAC hasta el Terminal Huasao.",
        price = "S/. 1.00",
        frequency = "Cada 8 min",
        activeBusesCount = 1
    ),
    RutaDetalle(
        code = "LEONES",
        name = "Los Leones",
        company = "Empresa Los Leones",
        itinerary = "Saphi / Don Bosco ➔ Limacpampa ➔ Plaza Tipón",
        description = "Penetración Centro Histórico desde Colegio Don Bosco (Saphi), Plaza San Francisco, Limacpampa hasta la Plaza de Tipón.",
        price = "S/. 1.50",
        frequency = "Cada 10 min",
        activeBusesCount = 1
    )
)

val availableBuses = listOf(
    BusInfo("B1", "Nuevo Mirador (U-12)", "RTI-07", "Ccachona ➔ Huancaro ➔ Saylla", "3 min", "A 350 metros", "Media (6 libres)", Color(0xFFD84315), "S/. 1.00", "Cada 4 min", "Asientos limitados", Color(0xFF1565C0), "Juan Yasser", LatLng(-13.5280, -71.9600)),
    BusInfo("B2", "Patrón San Jerónimo", "RTU-01", "Santiago ➔ UNSAAC ➔ Continental", "7 min", "A 1.2 km", "Alta (2 libres)", Color.Red, "S/. 1.50", "Cada 6 min", "Asientos libres disponibles", Color.Gray, "Carlos Condori", LatLng(-13.5238, -71.9540)),
    BusInfo("B3", "Saylla Huasao", "RTI-01", "Wanchaq ➔ UAC ➔ Huasao", "12 min", "A 2.5 km", "Baja (15 libres)", Color(0xFF388E3C), "S/. 1.00", "Cada 8 min", "Ruta despejada", Color.Gray, "Marcos Quispe", LatLng(-13.5250, -71.9650))
)
