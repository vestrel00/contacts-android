package com.vestrel00.contacts.entities

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Email.Type
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Email internal constructor(

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
     * The [Type] of email. Defaults to [Type.HOME].
     */
    val type: Type,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The email address.
     */
    val address: String?

) : DataEntity, Parcelable {

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(label, address)

    fun toMutableEmail() = MutableEmail(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        type = type,
        label = label,

        address = address
    )

    enum class Type(override val value: Int) : Entity.Type {

        // Order of declaration is the same as seen in the native contacts app
        HOME(CommonDataKinds.Email.TYPE_HOME), // Default
        WORK(CommonDataKinds.Email.TYPE_WORK),
        OTHER(CommonDataKinds.Email.TYPE_OTHER),
        MOBILE(CommonDataKinds.Email.TYPE_MOBILE),
        CUSTOM(CommonDataKinds.Email.TYPE_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Type = values().find { it.value == value } ?: HOME
        }
    }
}

@Parcelize
data class MutableEmail internal constructor(

    /**
     * See [Email.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Email.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [Email.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [Email.type].
     */
    var type: Type,

    /**
     * See [Email.label].
     */
    var label: String?,

    /**
     * See [Email.address].
     */
    var address: String?

) : DataEntity, Parcelable {

    constructor() : this(INVALID_ID, INVALID_ID, INVALID_ID, Type.HOME, null, null)

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(label, address)

    internal fun toEmail() = Email(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        type = type,
        label = label,

        address = address
    )
}