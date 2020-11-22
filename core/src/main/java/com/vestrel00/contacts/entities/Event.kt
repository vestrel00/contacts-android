package com.vestrel00.contacts.entities

import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Event.Type
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

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
     * it in the where clause. Use [com.vestrel00.contacts.util.toWhereString] to convert [Date]s to
     * the correct format required to match event dates.
     *
     * Unlike other dates in other entities in this library that are stored as milliseconds, these
     * dates are stored in the Content Provider DB as strings in the format of yyyy-MM-dd
     * (e.g. 2019-08-21).
     */
    val date: Date?

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.EVENT

    // type and label are excluded from this check as they are useless information by themselves
    @IgnoredOnParcel
    override val isBlank: Boolean = propertiesAreAllNullOrBlank(date)

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

    enum class Type(override val value: Int) : Entity.Type {

        // Order of declaration is the same as seen in the native contacts app
        BIRTHDAY(CommonDataKinds.Event.TYPE_BIRTHDAY), // Default
        ANNIVERSARY(CommonDataKinds.Event.TYPE_ANNIVERSARY),
        OTHER(CommonDataKinds.Event.TYPE_OTHER),
        CUSTOM(CommonDataKinds.Event.TYPE_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

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
    var type: Type?,

    /**
     * See [Event.label].
     */
    var label: String?,

    /**
     * See [Event.date].
     */
    var date: Date?

) : MutableCommonDataEntity {

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.EVENT

    // type and label are excluded from this check as they are useless information by themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(date)
}