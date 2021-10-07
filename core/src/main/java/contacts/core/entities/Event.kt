package contacts.core.entities

import android.content.res.Resources
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.Event.Type
import contacts.core.entities.mapper.EventMapper
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * A data kind representing an event.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * Local RawContacts (those that are not associated with an Account) **should not** have any entries
 * of this data kind.
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
     * Only the day, month, and year will be recorded!
     *
     * ## Query matching
     *
     * To match event dates in queries, the [Date] must be converted to a string first before using
     * it in the where clause. Use [contacts.core.util.toWhereString] to convert [Date]s to the
     * correct format required to match event dates.
     *
     * Unlike other dates in other entities in this library that are stored as milliseconds, these
     * dates are stored in the Content Provider DB as strings in the format of yyyy-MM-dd
     * (e.g. 2019-08-21).
     */
    val date: Date?

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
    var date: Date?

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

    override var primaryValue: String?
        get() = EventMapper.dateToString(date)
        set(value) {
            date = EventMapper.dateFromString(value)
        }
}