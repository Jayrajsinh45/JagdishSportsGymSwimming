package com.jagdishsports.gymswimming.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class MemberStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED
}

fun MemberEntity.endDate(): LocalDate = LocalDate.ofEpochDay(endDateEpochDay)

fun MemberEntity.startDate(): LocalDate = LocalDate.ofEpochDay(startDateEpochDay)

fun MemberEntity.status(
    today: LocalDate = LocalDate.now(),
    expiringSoonDays: Long = 5
): MemberStatus {
    val daysUntilExpiry = ChronoUnit.DAYS.between(today, endDate())
    return when {
        daysUntilExpiry < 0 -> MemberStatus.EXPIRED
        daysUntilExpiry <= expiringSoonDays -> MemberStatus.EXPIRING_SOON
        else -> MemberStatus.ACTIVE
    }
}

fun MemberEntity.isExpired(today: LocalDate = LocalDate.now()): Boolean {
    return endDate().isBefore(today)
}

fun MemberEntity.isActiveOrExpiresToday(today: LocalDate = LocalDate.now()): Boolean {
    return !endDate().isBefore(today)
}

fun MemberEntity.expiresWithin(days: Long, today: LocalDate = LocalDate.now()): Boolean {
    val daysUntilExpiry = ChronoUnit.DAYS.between(today, endDate())
    return daysUntilExpiry in 0..days
}
