package contacts.core.entities

import android.content.res.Resources
import android.os.Build
import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.Redactable
import contacts.core.entities.EventEntity.Type
import kotlinx.parcelize.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A data kind representing an event.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface EventEntity : DataEntityWithTypeAndLabel<Type> {

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

    /**
     * The [date] as a string.
     *
     * Note that the setter does nothing. Use the [date] directly to set it.
     */
    override val primaryValue: String?
        get() = date?.toDisplayString()

    override val mimeType: MimeType
        get() = MimeType.Event

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(date)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): EventEntity

    enum class Type(override val value: Int) : DataEntity.Type {

        // Order of declaration is the same as seen in the AOSP contacts app
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

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from EventEntity, there is only one interface that extends it; MutableEventEntity.
 *
 * The MutableEventEntity interface is used for library constructs that require an EventEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableEvent and NewEvent. With this, we can create constructs that can
 * keep a reference to MutableEvent(s) or NewEvent(s) through the MutableEventEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewEventEntity, ExistingEventEntity, and
 * ImmutableEventEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [EventEntity]. `
 */
sealed interface MutableEventEntity : EventEntity, MutableDataEntityWithTypeAndLabel<Type> {

    override var date: EventDate?

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

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableEventEntity
}

/**
 * An existing immutable [EventEntity].
 */
@Parcelize
data class Event internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val date: EventDate?,

    override val isRedacted: Boolean

) : EventEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableEvent> {

    override fun mutableCopy() = MutableEvent(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        date = date,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        date = date?.redactedCopy()
    )
}

/**
 * An existing mutable [EventEntity].
 */
@Parcelize
data class MutableEvent internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override var date: EventDate?,

    override val isRedacted: Boolean

) : EventEntity, ExistingDataEntity, MutableEventEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        date = date?.redactedCopy()
    )
}

/**
 * A new mutable [EventEntity].
 */
@Parcelize
data class NewEvent @JvmOverloads constructor(

    override var type: Type? = null,
    override var label: String? = null,

    override var date: EventDate? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : EventEntity, NewDataEntity, MutableEventEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        date = date?.redactedCopy()
    )
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
    val dayOfMonth: Int,

    override val isRedacted: Boolean

) : Redactable, Parcelable /* This is not a database Entity */ {

    /**
     * The 0-based [month] as 1-based for interfacing with the database.
     */
    internal val monthInDb: Int
        get() = month + 1

    override fun redactedCopy() = copy(
        isRedacted = true,

        year = null,
        month = 0,
        dayOfMonth = 1
    )

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
            } catch (e: IndexOutOfBoundsException) {
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
        @JvmStatic
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
        @JvmStatic
        fun from(date: Date, noYear: Boolean = false): EventDate {
            val calendar = Calendar.getInstance()
            calendar.time = date

            return EventDate(
                if (noYear) null else calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                false
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
 * If the [EventDate.monthInDb] or [EventDate.dayOfMonth] is a single digit, it is prefixed with 0
 * in order to adhere to the Contacts Provider date string format. Otherwise, a duplicate event will
 * be created with the correct date string format.
 *
 * ## Inequality comparison
 *
 * Comparing dates with a year to dates without a year will always result in dates without
 * a year being less than dates with a year. E.G. `"--11-11" < "2020-10-10"` is true.
 */
internal fun EventDate.toDbString(): String =
    "${year ?: "-"}-${monthInDb.toDoubleDigitStr()}-${dayOfMonth.toDoubleDigitStr()}"

/**
 * Returns this int as a string prefixed by 0 if it is a single digit.
 */
private fun Int.toDoubleDigitStr(): String = toString().padStart(2, '0')

/**
 * Returns the string representation of this [EventDate].
 *
 * The month used here is the [EventDate.monthInDb], which is 1-based.
 */
fun EventDate.toDisplayString(): String = toDbString().let {
    if (isRedacted) {
        it.redact()
    } else {
        it
    }
}

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

// Was this coincidence or did the Android team choose these format on purpose? Are these
// formats influenced by device local and/or OEM modifications? Only time will tell. The
// community should file bugs if they see any issues. Though, I should probably check this
// again when we get closer to v1.0.0.
private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val DATE_FORMAT_NO_YEAR = SimpleDateFormat("--MM-dd", Locale.US)