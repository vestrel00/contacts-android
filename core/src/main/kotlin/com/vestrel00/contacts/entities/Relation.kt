package com.vestrel00.contacts.entities

import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Relation.Type
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Relation internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of relation.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The name of the relative as the user entered it.
     */
    val name: String?

) : DataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.RELATION

    // type and label are excluded from this check as they are useless information by themselves
    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(name)

    fun toMutableRelation() = MutableRelation(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        name = name
    )

    enum class Type(override val value: Int) : Entity.Type {

        // Order of declaration is the same as seen in the native contacts app
        ASSISTANT(CommonDataKinds.Relation.TYPE_ASSISTANT), // Default
        BROTHER(CommonDataKinds.Relation.TYPE_BROTHER),
        CHILD(CommonDataKinds.Relation.TYPE_CHILD),
        DOMESTIC_PARTNER(CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER),
        FATHER(CommonDataKinds.Relation.TYPE_FATHER),
        FRIEND(CommonDataKinds.Relation.TYPE_FRIEND),
        MANAGER(CommonDataKinds.Relation.TYPE_MANAGER),
        MOTHER(CommonDataKinds.Relation.TYPE_MOTHER),
        PARENT(CommonDataKinds.Relation.TYPE_PARENT),
        PARTNER(CommonDataKinds.Relation.TYPE_PARTNER),
        REFERRED_BY(CommonDataKinds.Relation.TYPE_REFERRED_BY),
        RELATIVE(CommonDataKinds.Relation.TYPE_RELATIVE),
        SISTER(CommonDataKinds.Relation.TYPE_SISTER),
        SPOUSE(CommonDataKinds.Relation.TYPE_SPOUSE),
        CUSTOM(CommonDataKinds.Relation.TYPE_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

@Parcelize
data class MutableRelation internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Relation.type].
     */
    var type: Type?,

    /**
     * See [Relation.label].
     */
    var label: String?,

    /**
     * See [Relation.name].
     */
    var name: String?

) : MutableDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.RELATION

    constructor() : this(
        null, null, null, false, false,
        null, null, null
    )

    // type and label are excluded from this check as they are useless information by themselves
    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(name)
}