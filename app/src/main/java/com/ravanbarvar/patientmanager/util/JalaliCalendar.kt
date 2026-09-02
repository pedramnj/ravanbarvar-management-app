package com.ravanbarvar.patientmanager.util

import java.time.LocalDate

/**
 * Gregorian <-> Jalali (Hijri-Shamsi / Persian solar) calendar conversion.
 *
 * Port of the well-established 33-year-break-cycle algorithm (jalaali-js /
 * Borkowski), validated against the `jdatetime` reference implementation
 * across 1930-2090 with zero mismatches before being ported here. All integer
 * division below truncates toward zero to match the original algorithm.
 */
object JalaliCalendar {

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /** Iran week order starting Saturday. */
    val weekdayNames = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    val weekdayShortNames = arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    private fun tdiv(a: Int, b: Int): Int = a / b
    private fun tmod(a: Int, b: Int): Int = a % b

    private class JalCalResult(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCalResult {
        val bl = BREAKS.size
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        require(jy >= jp && jy < BREAKS[bl - 1]) { "Invalid Jalali year $jy" }
        var jump = 0
        var jm: Int
        var i = 1
        while (i < bl) {
            jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ = leapJ + tdiv(jump, 33) * 8 + tdiv(tmod(jump, 33), 4)
            jp = jm
            i++
        }
        var n = jy - jp
        leapJ = leapJ + tdiv(n, 33) * 8 + tdiv(tmod(n, 33) + 3, 4)
        if (tmod(jump, 33) == 4 && jump - n == 4) {
            leapJ += 1
        }
        val leapG = tdiv(gy, 4) - tdiv((tdiv(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) {
            n = n - jump + tdiv(jump + 4, 33) * 33
        }
        var leap = tmod(tmod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return JalCalResult(leap, gy, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = tdiv((gy + tdiv(gm - 8, 6) + 100100) * 1461, 4) +
            tdiv(153 * tmod(gm + 9, 12) + 2, 5) +
            gd - 34840408
        d = d - tdiv(tdiv(gy + 100100 + tdiv(gm - 8, 6), 100) * 3, 4) + 752
        return d
    }

    private fun d2g(jdn: Int): IntArray {
        var j = 4 * jdn + 139361631
        j += tdiv(tdiv(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = tdiv(tmod(j, 1461), 4) * 5 + 308
        val gd = tdiv(tmod(i, 153), 5) + 1
        val gm = tmod(tdiv(i, 153), 12) + 1
        val gy = tdiv(j, 1461) - 100100 + tdiv(8 - gm, 6)
        return intArrayOf(gy, gm, gd)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - tdiv(jm, 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): IntArray {
        val gy = d2g(jdn)[0]
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                val jm = 1 + tdiv(k, 31)
                val jd = tmod(k, 31) + 1
                return intArrayOf(jy, jm, jd)
            } else {
                k -= 186
            }
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        val jm = 7 + tdiv(k, 30)
        val jd = tmod(k, 30) + 1
        return intArrayOf(jy, jm, jd)
    }

    fun isLeapJalaliYear(jy: Int): Boolean = jalCal(jy).leap == 0

    fun jalaliMonthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeapJalaliYear(jy)) 30 else 29
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray = d2j(g2d(gy, gm, gd))

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray = d2g(j2d(jy, jm, jd))
}

/** Immutable Jalali calendar date, interoperable with [LocalDate] via epoch day. */
data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {

    fun toEpochDay(): Long {
        val g = JalaliCalendar.jalaliToGregorian(year, month, day)
        return LocalDate.of(g[0], g[1], g[2]).toEpochDay()
    }

    fun toLocalDate(): LocalDate {
        val g = JalaliCalendar.jalaliToGregorian(year, month, day)
        return LocalDate.of(g[0], g[1], g[2])
    }

    fun plusDays(days: Long): JalaliDate = fromEpochDay(toEpochDay() + days)

    fun plusMonths(months: Int): JalaliDate {
        var y = year
        var m = month + months
        while (m > 12) { m -= 12; y += 1 }
        while (m < 1) { m += 12; y -= 1 }
        val d = day.coerceAtMost(JalaliCalendar.jalaliMonthLength(y, m))
        return JalaliDate(y, m, d)
    }

    fun withDay(newDay: Int): JalaliDate =
        JalaliDate(year, month, newDay.coerceIn(1, JalaliCalendar.jalaliMonthLength(year, month)))

    /** 0 = Saturday .. 6 = Friday (Iran week order). */
    fun dayOfWeekIndex(): Int {
        val iso = toLocalDate().dayOfWeek.value // MONDAY=1 .. SUNDAY=7
        return (iso + 1) % 7
    }

    fun monthName(): String = JalaliCalendar.monthNames[month - 1]

    fun monthLength(): Int = JalaliCalendar.jalaliMonthLength(year, month)

    fun formatted(): String =
        "${PersianDigits.of(day)} ${monthName()} ${PersianDigits.of(year)}"

    fun formattedShort(): String =
        "${PersianDigits.of(year)}/${PersianDigits.of(month, 2)}/${PersianDigits.of(day, 2)}"

    override fun compareTo(other: JalaliDate): Int = toEpochDay().compareTo(other.toEpochDay())

    companion object {
        fun today(): JalaliDate = fromLocalDate(LocalDate.now())

        fun fromLocalDate(date: LocalDate): JalaliDate {
            val j = JalaliCalendar.gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
            return JalaliDate(j[0], j[1], j[2])
        }

        fun fromEpochDay(epochDay: Long): JalaliDate = fromLocalDate(LocalDate.ofEpochDay(epochDay))
    }
}

/** Converts Western digits/numbers to Persian (Eastern Arabic-Indic) digits. */
object PersianDigits {
    private val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun of(text: String): String = buildString(text.length) {
        for (c in text) {
            append(if (c in '0'..'9') fa[c - '0'] else c)
        }
    }

    fun of(number: Int): String = of(number.toString())

    fun of(number: Int, minDigits: Int): String = of(number.toString().padStart(minDigits, '0'))

    fun toEnglishDigits(text: String): String = buildString(text.length) {
        for (c in text) {
            val idx = fa.indexOf(c)
            append(if (idx >= 0) ('0' + idx) else c)
        }
    }
}
