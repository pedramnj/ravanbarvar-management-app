package com.ravanbarvar.patientmanager.util

/** Formats a Toman amount with thousands separators and Persian digits, e.g. "۵۰۰,۰۰۰ تومان". */
fun formatToman(amount: Long): String {
    val grouped = "%,d".format(amount)
    return "${PersianDigits.of(grouped)} تومان"
}
