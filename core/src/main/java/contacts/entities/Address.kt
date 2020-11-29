package contacts.entities

import android.provider.ContactsContract.CommonDataKinds
import contacts.entities.Address.Type
import contacts.util.unsafeLazy
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The [Type] of address.
     */
    val type: Type?,

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?,

    /**
     * The full, unstructured postal address. This must be consistent with any structured data.
     *
     * The [formattedAddress] is the unstructured representation of the postal address. It is made
     * up of structured components; [street], [poBox], [neighborhood], [city], [region], [postcode],
     * and [country].
     *
     * When updating or inserting;
     *
     * - If the [formattedAddress] is null and there are non-null structured components provided,
     *   the Contacts Provider will automatically set the formatted address by combining the
     *   structured components.
     *
     * - If the [formattedAddress] is not null and all structured components are null, the Contacts
     *   Provider automatically sets the street value to the formatted address.
     *
     * - If the [formattedAddress] and structured components are not null, the Contacts Provider
     *   does nothing automatically.
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

) : CommonDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Address

    // type and label are excluded from this check as they are useless information by themselves
    @IgnoredOnParcel
    override val isBlank: Boolean by unsafeLazy {
        propertiesAreAllNullOrBlank(
            formattedAddress, street, poBox, neighborhood, city, region, postcode, country
        )
    }

    fun toMutableAddress() = MutableAddress(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

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

    enum class Type(override val value: Int) : CommonDataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        HOME(CommonDataKinds.StructuredPostal.TYPE_HOME), // Default
        WORK(CommonDataKinds.StructuredPostal.TYPE_WORK),
        OTHER(CommonDataKinds.StructuredPostal.TYPE_OTHER),
        CUSTOM(CommonDataKinds.StructuredPostal.TYPE_CUSTOM);

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

@Parcelize
data class MutableAddress internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Address.type].
     */
    var type: Type?,

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

) : MutableCommonDataEntity {

    constructor() : this(
        null, null, null, false, false, null, null, null,
        null, null, null, null, null, null, null
    )

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.Address

    // type and label are excluded from this check as they are useless information by themselves
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            formattedAddress, street, poBox, neighborhood, city, region, postcode, country
        )
}