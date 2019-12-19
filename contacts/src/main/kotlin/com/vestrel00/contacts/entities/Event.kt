package com.vestrel00.contacts.entities

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Event.Type
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Event internal constructor(

    /**
     * The id of this row in the Data table.
     */
    override val id: Long,

    /**
     * The id of the [RawContact] this data belongs to.
     */
    override val rawContactId: Long,

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    override val contactId: Long,

    /**
     * The [Type] of event. Defaults to [Type.BIRTHDAY].
     */
    val type: Type,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The event date as the user entered it.
     *
     * Only the day, month, and year will be recorded!
     *
     * For query matching, use [com.vestrel00.contacts.util.toWhereString] to convert these event
     * dates to the correct format.
     */
    val date: Date?

) : DataEntity, Parcelable {

    fun toMutableEvent() = MutableEvent(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

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

            fun fromValue(value: Int?): Type = values().find { it.value == value } ?: BIRTHDAY
        }
    }
}

@Parcelize
data class MutableEvent internal constructor(

    /**
     * See [Event.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Event.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [Event.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [Event.type].
     */
    var type: Type,

    /**
     * See [Event.label].
     */
    var label: String?,

    /**
     * See [Event.date].
     */
    var date: Date?

) : DataEntity, Parcelable {

    constructor() : this(INVALID_ID, INVALID_ID, INVALID_ID, Type.BIRTHDAY, null, null)

    internal fun toEvent() = Event(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        type = type,
        label = label,

        date = date
    )
}