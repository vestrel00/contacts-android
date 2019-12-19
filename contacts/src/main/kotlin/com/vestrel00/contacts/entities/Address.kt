package com.vestrel00.contacts.entities

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import com.vestrel00.contacts.entities.Address.Type
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address internal constructor(

    /**
     * The id of this row in the Data table.
     */
    override val id: Long,

    /**
     * The id of the [RawContact] this data is associated with.
     */
    override val rawContactId: Long,

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    override val contactId: Long,

    /**
     * The [Type] of address. Defaults to [Type.HOME].
     */
    val type: Type,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The full, unstructured postal address.
     *
     * This field must be consistent with any structured data.
     *
     * Note! This is automatically set the the Contacts Provider if null.
     */
    val formattedAddress: String?,

    /**
     *
     * Can be street, avenue, road, etc. This element also includes the house number and
     * room/apartment/flat/floor number.
     */
    val street: String?,

    /**
     * Covers actual P.O. boxes, drawers, locked bags, etc. This is usually but not always mutually
     * exclusive with street.
     */
    val poBox: String?,

    /**
     * This is used to disambiguate a street address when a city contains more than one street with
     * the same name, or to specify a small place whose mail is routed through a larger postal town.
     * In China it could be a county or a minor city.
     */
    val neighborhood: String?,

    /**
     * Can be city, village, town, borough, etc. This is the postal town and not necessarily the
     * place of residence or place of business.
     */
    val city: String?,

    /**
     * A state, province, county (in Ireland), Land (in Germany), department (in France), etc.
     */
    val region: String?,

    /**
     * Postal code. Usually country-wide, but sometimes specific to the city (e.g. "2" in "Dublin 2,
     * Ireland" addresses).
     */
    val postcode: String?,

    /**
     * The name or code of the country.
     */
    val country: String?

) : DataEntity, Parcelable {

    fun toMutableAddress() = MutableAddress(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        type = type,
        label = label,

        formattedAddress = formattedAddress,
        street = street,
        poBox = poBox,
        neighborhood = neighborhood,
        city = city,
        region = region,
        postcode = postcode,
        country = country
    )

    enum class Type(override val value: Int) : Entity.Type {

        // Order of declaration is the same as seen in the native contacts app
        HOME(CommonDataKinds.StructuredPostal.TYPE_HOME), // Default
        WORK(CommonDataKinds.StructuredPostal.TYPE_WORK),
        OTHER(CommonDataKinds.StructuredPostal.TYPE_OTHER),
        CUSTOM(CommonDataKinds.StructuredPostal.TYPE_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Type = values().find { it.value == value } ?: HOME
        }
    }
}

@Parcelize
data class MutableAddress internal constructor(

    /**
     * See [Address.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Address.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [Address.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [Address.type].
     */
    var type: Type,

    /**
     * See [Address.label].
     */
    var label: String?,

    /**
     * See [Address.formattedAddress].
     */
    var formattedAddress: String?,

    /**
     * See [Address.street].
     */
    var street: String?,

    /**
     * See [Address.poBox].
     */
    var poBox: String?,

    /**
     * See [Address.neighborhood].
     */
    var neighborhood: String?,

    /**
     * See [Address.city].
     */
    var city: String?,

    /**
     * See [Address.region].
     */
    var region: String?,

    /**
     * See [Address.postcode].
     */
    var postcode: String?,

    /**
     * See [Address.country].
     */
    var country: String?

) : DataEntity, Parcelable {

    constructor() : this(
        INVALID_ID, INVALID_ID, INVALID_ID, Type.HOME, null, null, null,
        null, null, null, null, null, null
    )

    internal fun toAddress() = Address(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        type = type,
        label = label,

        formattedAddress = formattedAddress,
        street = street,
        poBox = poBox,
        neighborhood = neighborhood,
        city = city,
        region = region,
        postcode = postcode,
        country = country
    )
}