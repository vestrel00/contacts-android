package com.vestrel00.contacts.entities

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Relation.Type
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Relation internal constructor(

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
     * The [Type] of relation. Defaults to [Type.ASSISTANT].
     */
    val type: Type,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The name of the relative as the user entered it.
     */
    val name: String?

) : DataEntity, Parcelable {

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(label, name)

    fun toMutableRelation() = MutableRelation(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

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

            fun fromValue(value: Int?): Type = values().find { it.value == value } ?: ASSISTANT
        }
    }
}

@Parcelize
data class MutableRelation internal constructor(

    /**
     * See [Relation.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Relation.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [Relation.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [Relation.type].
     */
    var type: Type,

    /**
     * See [Relation.label].
     */
    var label: String?,

    /**
     * See [Relation.name].
     */
    var name: String?

) : DataEntity, Parcelable {

    constructor() : this(INVALID_ID, INVALID_ID, INVALID_ID, Type.ASSISTANT, null, null)

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(label, name)

    internal fun toRelation() = Relation(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        type = type,
        label = label,

        name = name
    )
}