package com.vestrel00.contacts.entities

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Email.Type
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Email internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of email.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The email address.
     */
    val address: String?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.EMAIL

    // type and label are excluded from this check as they are useless information by themselves
    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(address)

    fun toMutableEmail() = MutableEmail(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

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

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

@Parcelize
data class MutableEmail internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Email.type].
     */
    var type: Type?,

    /**
     * See [Email.label].
     */
    var label: String?,

    /**
     * See [Email.address].
     */
    var address: String?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.EMAIL

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    // type and label are excluded from this check as they are useless information by themselves
    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(address)
}