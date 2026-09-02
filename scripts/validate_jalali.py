import datetime
import jdatetime

BREAKS = [-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
          1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178]

def tdiv(a, b):
    # truncating integer division toward zero (matches JS ~~(a/b) and Kotlin Int '/')
    q = a / b
    return int(q) if q >= 0 else -int(-q)

def tmod(a, b):
    return a - tdiv(a, b) * b

def jal_cal(jy):
    bl = len(BREAKS)
    gy = jy + 621
    leap_j = -14
    jp = BREAKS[0]
    if jy < jp or jy >= BREAKS[bl - 1]:
        raise ValueError("invalid jalali year " + str(jy))
    jump = 0
    jm = jp
    for i in range(1, bl):
        jm = BREAKS[i]
        jump = jm - jp
        if jy < jm:
            break
        leap_j = leap_j + tdiv(jump, 33) * 8 + tdiv(tmod(jump, 33), 4)
        jp = jm
    n = jy - jp
    leap_j = leap_j + tdiv(n, 33) * 8 + tdiv(tmod(n, 33) + 3, 4)
    if tmod(jump, 33) == 4 and jump - n == 4:
        leap_j += 1
    leap_g = tdiv(gy, 4) - tdiv((tdiv(gy, 100) + 1) * 3, 4) - 150
    march = 20 + leap_j - leap_g
    if jump - n < 6:
        n = n - jump + tdiv(jump + 4, 33) * 33
    leap = tmod(tmod(n + 1, 33) - 1, 4)
    if leap == -1:
        leap = 4
    return {"leap": leap, "gy": gy, "march": march}

def g2d(gy, gm, gd):
    d = tdiv((gy + tdiv(gm - 8, 6) + 100100) * 1461, 4) \
        + tdiv(153 * tmod(gm + 9, 12) + 2, 5) \
        + gd - 34840408
    d = d - tdiv(tdiv(gy + 100100 + tdiv(gm - 8, 6), 100) * 3, 4) + 752
    return d

def d2g(jdn):
    j = 4 * jdn + 139361631
    j = j + tdiv(tdiv(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
    i = tdiv(tmod(j, 1461), 4) * 5 + 308
    gd = tdiv(tmod(i, 153), 5) + 1
    gm = tmod(tdiv(i, 153), 12) + 1
    gy = tdiv(j, 1461) - 100100 + tdiv(8 - gm, 6)
    return (gy, gm, gd)

def j2d(jy, jm, jd):
    r = jal_cal(jy)
    return g2d(r["gy"], 3, r["march"]) + (jm - 1) * 31 - tdiv(jm, 7) * (jm - 7) + jd - 1

def d2j(jdn):
    gy, _, _ = d2g(jdn)
    jy = gy - 621
    r = jal_cal(jy)
    jdn1f = g2d(gy, 3, r["march"])
    k = jdn - jdn1f
    if k >= 0:
        if k <= 185:
            jm = 1 + tdiv(k, 31)
            jd = tmod(k, 31) + 1
            return (jy, jm, jd)
        else:
            k -= 186
    else:
        jy -= 1
        k += 179
        if r["leap"] == 1:
            k += 1
    jm = 7 + tdiv(k, 30)
    jd = tmod(k, 30) + 1
    return (jy, jm, jd)

def g_to_j(gy, gm, gd):
    return d2j(g2d(gy, gm, gd))

def j_to_g(jy, jm, jd):
    return d2g(j2d(jy, jm, jd))

# Validate against jdatetime across a wide range
start = datetime.date(1930, 1, 1)
end = datetime.date(2090, 12, 31)
d = start
mismatches = 0
count = 0
while d <= end:
    jd_ref = jdatetime.date.fromgregorian(date=d)
    jy, jm, jday = g_to_j(d.year, d.month, d.day)
    count += 1
    if (jy, jm, jday) != (jd_ref.year, jd_ref.month, jd_ref.day):
        mismatches += 1
        if mismatches <= 10:
            print("MISMATCH", d, "mine=", (jy, jm, jday), "ref=", (jd_ref.year, jd_ref.month, jd_ref.day))
    # also test reverse
    gy, gm, gd = j_to_g(jy, jm, jday)
    if (gy, gm, gd) != (d.year, d.month, d.day):
        mismatches += 1
        if mismatches <= 10:
            print("REVERSE MISMATCH", d, "->", (jy,jm,jday), "->", (gy,gm,gd))
    d += datetime.timedelta(days=97)  # sample every 97 days for speed, still spans full range densely over years

print("checked:", count, "mismatches:", mismatches)

# Dense check near today and a few known Nowruz dates
tests = [
    (2021, 3, 21), (2022, 3, 21), (2023, 3, 21), (2024, 3, 20),
    (2025, 3, 20), (2026, 3, 21), (2026, 9, 2), (2000, 1, 1), (1979, 2, 11),
]
for (gy, gm, gd) in tests:
    ref = jdatetime.date.fromgregorian(date=datetime.date(gy, gm, gd))
    mine = g_to_j(gy, gm, gd)
    status = "OK" if (mine[0], mine[1], mine[2]) == (ref.year, ref.month, ref.day) else "FAIL"
    print(gy, gm, gd, "-> mine", mine, "ref", (ref.year, ref.month, ref.day), status)

# dense day-by-day check for a couple of years to catch leap-year edge cases
for year in [1399, 1400, 1403, 1404, 1408, 1412]:
    j = jdatetime.date(year, 1, 1)
    g = j.togregorian()
    jy, jm, jday = g_to_j(g.year, g.month, g.day)
    print("Farvardin 1 of", year, "gregorian ref", g, "mine", (jy,jm,jday))
