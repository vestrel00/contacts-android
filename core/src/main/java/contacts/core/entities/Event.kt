package contacts.core.entities

import android.content.res.Resources
import android.os.Build
import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.Event.Type
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A data kind representing an event.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * Local RawContacts (those that are not associated with an Account) **should not** have any entries
 * of this data kind.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class Event internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of event.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The event date as the user entered it.
     *
     * ## Query matching
     *
     * To use event dates in queries' WHERE clauses, the [Date] or [EventDate] must be converted to
     * a string  using [Date.toWhereString] or [EventDate.toWhereString].
     *
     * ## Dev notes
     *
     * This date is stored in the database in one of the following formats;
     *
     * - a full date; "yyyy-MM-dd" (e.g. 2019-08-21)
     * - a date with no year; "--MM-dd" (e.g. --08-21)
     *
     * Also note that the month "MM" is 1-based. The first month of the year, January, is 1.
     */
    val date: EventDate?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Event

    // type and label are excluded from this check as they are useless information by themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(date)

    fun toMutableEvent() = MutableEvent(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        date = date
    )

    enum class Type(override val value: Int) : CommonDataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        BIRTHDAY(CommonDataKinds.Event.TYPE_BIRTHDAY), // Default
        ANNIVERSARY(CommonDataKinds.Event.TYPE_ANNIVERSARY),
        OTHER(CommonDataKinds.Event.TYPE_OTHER),
        CUSTOM(CommonDataKinds.Event.TYPE_CUSTOM);

        override fun labelStr(resources: Resources, label: String?): String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CommonDataKinds.Event.getTypeLabel(resources, value, label).toString()
            } else {
                // Copy pasted from CommonDataKinds.Event.getTypeLabel function body.
                if (this == CUSTOM && label?.isNotEmpty() == true) {
                    label
                } else {
                    val labelRes = CommonDataKinds.Event.getTypeResource(value)
                    resources.getText(labelRes).toString()
                }
            }

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/**
 * A mutable [Event].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class MutableEvent internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Event.type].
     */
    override var type: Type?,

    /**
     * See [Event.label].
     */
    override var label: String?,

    /**
     * See [Event.date].
     */
    var date: EventDate?

) : MutableCommonDataEntityWithType<Type> {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Event

    // type and label are excluded from this check as they are useless information by themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(date)

    /**
     * The [date] as a string.
     *
     * Note that the setter does nothing. Use the [date] directly to set it.
     */
    override var primaryValue: String?
        get() = date?.toDisplayString()
        set(_) {
            // The primary value is EventDate, which has an internal 1-based and consumer-facing
            // 0-based representation of the month. There is ambiguity here. We can document that
            // this setter should only be used for the consumer-facing 0-based representation but
            // the ambiguity may still cause bugs. We could implement this if the community really
            // wants to but for now... leaving it blank!
        }
}

/**
 * An Event date that may or may not have a year.
 */
@Parcelize
data class EventDate internal constructor(

    /**
     * The [Calendar.YEAR]. This is null for events with no year.
     */
    val year: Int?,

    /**
     * The [Calendar.MONTH].
     *
     * Note that this is 0-based to support [Calendar] operations. The first month of the year,
     * January, is 0.
     *
     * The month displayed in UIs and typical normal life is 1-based where the first month of the
     * year, January, is 1.
     *
     * ## Dev notes
     *
     * The month stored in the database is 1-based, NOT 0-based. The first month of the year,
     * January, is 1. We should use SimpleDateFormat to parse the month stored in the database
     * using the "MM" pattern, which is also 1-based.
     *
     * As far as consumers are concerned, the month is 0-based. For us internally, when interacting
     * with the database in queries or other operations, we must be 1-based.
     *
     * The API-facing side of this should be compatible with [Calendar.MONTH], which is 0-based.
     */
    val month: Int,

    /**
     * The [Calendar.DAY_OF_MONTH].
     */
    val dayOfMonth: Int

) : Parcelable {

    /**
     * The 0-based [month] as 1-based for interfacing with the database.
     */
    internal val monthInDb: Int
        get() = month + 1

    companion object {

        /**
         * Returns an [EventDate] from the given [dateStrFromDb], which is either;
         *
         * - a full date; "yyyy-MM-dd" (e.g. 2019-08-21)
         * - a date with no year; "--MM-dd" (e.g. --08-21)
         *
         * If the given [dateStrFromDb] is not a valid date or does not follow one of the above date
         * formats, then null will be returned.
         *
         * ## Dev notes
         *
         * This function should only be directly used **internally** to convert database event date
         * values to [EventDate]. Therefore, it should be internal. The reason is that this is
         * reading a 1-based month in the database but outputs 0-based [EventDate.month].
         */
        internal fun fromDateStrFromDb(dateStrFromDb: String?): EventDate? = dateStrFromDb?.let {
            // We could parse the string ourselves and easily get the year, month, and day.
            // However, validating whether the date is actually valid or not is another story.
            // We won't write code that's already been written. We'll use SimpleDateFormat =)
            val noYear = it.startsWith("-")
            val date = try {
                if (noYear) {
                    DATE_FORMAT_NO_YEAR.parse(it)
                } else {
                    DATE_FORMAT.parse(it)
                }
            } catch (pe: ParseException) {
                null
            }

            // I know we can do date?.let. I like this better in this case because this is already
            // inside a let... Don't like it? SUE ME!
            return if (date != null) from(date, noYear) else null
        }

        /**
         * Returns an [EventDate] from the given [year], [month], and [dayOfMonth].
         *
         * Note that the given [month] should be zero-based. The first month of the year, January,
         * is 0.
         */
        fun from(year: Int?, month: Int, dayOfMonth: Int): EventDate {
            // Note that we should not just construct an instance of EventDate directly from the
            // given values. EventDate values should only be created from valid dates to ensure
            // integrity/validity of dates stored in the database.
            val calendar = Calendar.getInstance()
            if (year != null) {
                calendar.set(Calendar.YEAR, year)
            }
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            return from(calendar.time, year == null)
        }

        /**
         * Returns an [EventDate] from the given [date].
         */
        fun from(date: Date, noYear: Boolean = false): EventDate {
            val calendar = Calendar.getInstance()
            calendar.time = date

            return EventDate(
                if (noYear) null else calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

/**
 * Returns the [Date] represented by this [EventDate]. Note that if [EventDate.year] is null, then
 * the year will be the calendar's default year, which is typically the current year.
 */
fun EventDate.toDate(): Date {
    val calendar = Calendar.getInstance()

    if (year != null) {
        calendar.set(Calendar.YEAR, year)
    }
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

    return calendar.time
}

/**
 * Returns the string representation of this [EventDate] for use in database queries' WHERE clauses.
 *
 * The month used here is the [EventDate.monthInDb], which is 1-based.
 *
 * ## Inequality comparison
 *
 * Comparing dates with a year to dates without a year will always result in dates without
 * a year being less than dates with a year. E.G. `"--11-11" < "2020-10-10"` is true.
 */
internal fun EventDate.toDbString(): String = "${year ?: "-"}-$monthInDb-$dayOfMonth"

/**
 * Returns the string representation of this [EventDate].
 *
 * The month used here is the [EventDate.monthInDb], which is 1-based.
 */
fun EventDate.toDisplayString(): String = toDbString()

/**
 * Returns the string representation of this [EventDate].
 *
 * The month used here is the [EventDate.monthInDb], which is 1-based.
 *
 * ## Inequality comparison
 *
 * Comparing dates with a year to dates without a year will always result in dates without
 * a year being less than dates with a year. E.G. `"--11-11" < "2020-10-10"` is true.
 */
fun EventDate.toWhereString(): String = toDbString()

/**
 * Returns the string representation of this [Date] for use in database queries' WHERE clauses.
 *
 * The month used here is the [EventDate.monthInDb], which is 1-based.
 *
 * ## Inequality comparison
 *
 * Comparing dates with a year to dates without a year will always result in dates without
 * a year being less than dates with a year. E.G. `"--11-11" < "2020-10-10"` is true.
 */
@JvmOverloads
fun Date.toWhereString(ignoreYear: Boolean = false): String =
    EventDate.from(this, ignoreYear).toWhereString()

// FIXME Are these all of the possible date formats? Or are we missing something?
// Was this coincidence or did the Android team choose these format on purpose? Are these
// formats influenced by device local and/or OEM modifications? Only time will tell. The
// community should file bugs if they see any issues. Though, I should probably check this
// again when we get closer to v1.0.0.
private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val DATE_FORMAT_NO_YEAR = SimpleDateFormat("--MM-dd", Locale.US)