package com.example.producthunt.data

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Resolve o intervalo de "hoje" (00:00:00 até 23:59:59 no fuso do aparelho)
 * e converte para o formato ISO 8601 em UTC que a API do Product Hunt espera
 * nos argumentos postedAfter / postedBefore (ex: "2026-08-27T03:00:00Z").
 */
object DateUtils {

    fun todayRangeIso(zoneId: ZoneId = ZoneId.systemDefault()): Pair<String, String> {
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant()
        val startOfNextDay = today.plusDays(1).atStartOfDay(zoneId).toInstant()

        val formatter = DateTimeFormatter.ISO_INSTANT
        return formatter.format(startOfDay) to formatter.format(startOfNextDay)
    }
}
