package ru.gohasoft.wanderingtable.feature.main.mapper

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.feature.main.R

/**
 * Date rendering for the main feature. Everything the server sends is a UTC [Instant]; the screens
 * show club-local wall time, so every formatter here is bound to the device zone.
 *
 * Patterns stay plain strings — they are already locale-formatted by [DateTimeFormatter] — while
 * anything with words around a number goes through [TextResource] so it can be translated.
 */

private const val DATE_TIME_PATTERN = "EEE, MMM d · h:mm a"
private const val SHORT_DATE_PATTERN = "MMM d"
private const val LONG_DATE_PATTERN = "MMMM d, yyyy"
private const val TIME_PATTERN = "h:mm a"

/**
 * Formatters are per-locale rather than per-process: the device language can change while the app
 * is alive, and a formatter captured at class-load would keep rendering the old one. Cached
 * because building one is not free and these run once per list row.
 */
private val formatters = ConcurrentHashMap<Pair<String, Locale>, DateTimeFormatter>()

private fun formatter(pattern: String): DateTimeFormatter {
    val locale = Locale.getDefault()
    return formatters.getOrPut(pattern to locale) { DateTimeFormatter.ofPattern(pattern, locale) }
}

/** "Sat, Jul 12 · 7:00 PM" — the meta line of a game card. */
internal fun Instant.toDateTimeLabel(zone: ZoneId = ZoneId.systemDefault()): String =
    formatter(DATE_TIME_PATTERN).format(atZone(zone))

/** "Jul 14" — the corner of a news card. */
internal fun Instant.toShortDateLabel(zone: ZoneId = ZoneId.systemDefault()): String =
    formatter(SHORT_DATE_PATTERN).format(atZone(zone))

/** "July 14, 2026" — the byline of a news post. */
internal fun Instant.toLongDateLabel(zone: ZoneId = ZoneId.systemDefault()): String =
    formatter(LONG_DATE_PATTERN).format(atZone(zone))

/** The value shown in the Create Request date field. */
internal fun Instant.toDateFieldLabel(zone: ZoneId = ZoneId.systemDefault()): String =
    formatter(SHORT_DATE_PATTERN).format(atZone(zone))

/** The value shown in the Create Request time field. */
internal fun Instant.toTimeFieldLabel(zone: ZoneId = ZoneId.systemDefault()): String =
    formatter(TIME_PATTERN).format(atZone(zone))

/**
 * "2h ago" / "Yesterday" / "3 days ago", as the notification feed labels its rows. Anything older
 * than a week falls back to a date, which reads better than "37 days ago".
 */
internal fun Instant.toRelativeLabel(
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): TextResource {
    val elapsed = Duration.between(this, now)
    // Calendar days apart, not elapsed hours: 11pm yesterday to 1am today is "Yesterday", not "2h".
    val days = ChronoUnit.DAYS.between(toLocalDate(zone), now.toLocalDate(zone))
    return when {
        elapsed.toMinutes() < 1 -> StringResource(R.string.relative_just_now)
        elapsed.toHours() < 1 -> StringResource(
            R.string.relative_minutes_ago,
            listOf(elapsed.toMinutes().toString()),
        )
        days == 0L -> StringResource(R.string.relative_hours_ago, listOf(elapsed.toHours().toString()))
        days == 1L -> StringResource(R.string.relative_yesterday)
        days < 7L -> StringResource(R.string.relative_days_ago, listOf(days.toString()))
        else -> TextResource.DynamicString(toShortDateLabel(zone))
    }
}

/** Feed rows are split into Today and Earlier; this is the test that decides which. */
internal fun Instant.isToday(
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): Boolean = toLocalDate(zone) == now.toLocalDate(zone)

/** `LocalDate.ofInstant` is API 34; going through the zoned date-time works from API 26. */
private fun Instant.toLocalDate(zone: ZoneId): LocalDate = atZone(zone).toLocalDate()
