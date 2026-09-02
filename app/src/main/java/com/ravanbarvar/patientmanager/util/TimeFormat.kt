package com.ravanbarvar.patientmanager.util

/** Formats minutes-since-midnight as a Persian-digit "HH:mm" string. */
fun minutesToPersianClock(totalMinutes: Int): String {
    val h = (totalMinutes / 60).coerceIn(0, 23)
    val m = totalMinutes % 60
    return PersianDigits.of("%02d:%02d".format(h, m))
}

fun minutesToDurationLabel(minutes: Int): String {
    if (minutes < 60) return "${PersianDigits.of(minutes)} دقیقه"
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${PersianDigits.of(h)} ساعت" else "${PersianDigits.of(h)} ساعت و ${PersianDigits.of(m)} دقیقه"
}
