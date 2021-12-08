package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.AddressEntity.Type
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a postal addresses.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface AddressEntity : DataEntity {

    /**
     * The [Type] of address.
     */
    val type: Type?

    /**
     * The name of the custom type. Used when the [type] is [Type.CUSTOM].
     */
    val label: String?

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
    val formattedAddress: String?

    /**
     *
     * Can be street, avenue, road, etc. This element also includes the house number and
     * room/apartment/flat/floor number.
     */
    val street: String?

    /**
     * Covers actual P.O. boxes, drawers, locked bags, etc. This is usually but not always mutually
     * exclusive with street.
     */
    val poBox: String?

    /**
     * This is used to disambiguate a street address when a city contains more than one street with
     * the same name, or to specify a small place whose mail is routed through a larger postal town.
     * In China it could be a county or a minor city.
     */
    val neighborhood: String?

    /**
     * Can be city, village, town, borough, etc. This is the postal town and not necessarily the
     * place of residence or place of business.
     */
    val city: String?

    /**
     * A state, province, county (in Ireland), Land (in Germany), department (in France), etc.
     */
    val region: String?

    /**
     * Postal code. Usually country-wide, but sometimes specific to the city (e.g. "2" in "Dublin 2,
     * Ireland" addresses).
     */
    val postcode: String?

    /**
     * The name or code of the country.
     */
    val country: String?

    override val mimeType: MimeType
        get() = MimeType.Address

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            formattedAddress, street, poBox, neighborhood,
            city, region, postcode, country
        )

    enum class Type(override val value: Int) : DataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        HOME(CommonDataKinds.StructuredPostal.TYPE_HOME), // Default
        WORK(CommonDataKinds.StructuredPostal.TYPE_WORK),
        OTHER(CommonDataKinds.StructuredPostal.TYPE_OTHER),
        CUSTOM(CommonDataKinds.StructuredPostal.TYPE_CUSTOM);


        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.StructuredPostal.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary! That is when there are two separate concrete
 * types that we want to perform an operation on.
 *
 * Apart from AddressEntity, there are only two interfaces that extends it;
 * ExistingAddressEntity and MutableAddressEntity.
 *
 * The ExistingAddressEntity interface is used for library functions that require an AddressEntity
 * with an ID, which means that it exists in the database. There are two variants of this;
 * Address and MutableAddress. With this, we can create functions (or extensions) that can take in
 * (or have as the receiver) either Address or MutableAddress through the ExistingAddressEntity
 * abstraction/facade.
 *
 * The MutableAddressEntity interface is used for library functions that require an AddressEntity
 * that can be mutated. There are two variants of this; MutableAddress and NewAddress. With this,
 * we can create functions (or extensions) that can take in (or have as the receiver) either
 * MutableAddress or NewAddress through the MutableAddressEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewRawContactEntity and ImmutableRawContactEntity.
 * There are currently no library functions that exist that need them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * An [AddressEntity] that has already been inserted into the database.
 */
sealed interface ExistingAddressEntity : AddressEntity, ExistingDataEntity

/**
 * A mutable [AddressEntity].
 */
sealed interface MutableAddressEntity : AddressEntity, MutableDataEntityWithTypeAndLabel<Type> {
    override var formattedAddress: String?
    override var street: String?
    override var poBox: String?
    override var neighborhood: String?
    override var city: String?
    override var region: String?
    override var postcode: String?
    override var country: String?
}

/**
 * An existing immutable [AddressEntity].
 */
@Parcelize
data class Address internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val formattedAddress: String?,
    override val street: String?,
    override val poBox: String?,
    override val neighborhood: String?,
    override val city: String?,
    override val region: String?,
    override val postcode: String?,
    override val country: String?

) : ExistingAddressEntity, ImmutableDataEntityWithMutableType<MutableAddress> {

    override fun mutableCopy() = MutableAddress(
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
}

/**
 * An existing mutable [AddressEntity].
 */
@Parcelize
data class MutableAddress internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,

    override var formattedAddress: String?,
    override var street: String?,
    override var poBox: String?,
    override var neighborhood: String?,
    override var city: String?,
    override var region: String?,
    override var postcode: String?,
    override var country: String?

) : ExistingAddressEntity, MutableAddressEntity {

    @IgnoredOnParcel
    override var primaryValue: String? by this::formattedAddress
}

/**
 * A new mutable [AddressEntity].
 */
@Parcelize
data class NewAddress internal constructor(

    override var type: Type?,
    override var label: String?,

    override var formattedAddress: String?,
    override var street: String?,
    override var poBox: String?,
    override var neighborhood: String?,
    override var city: String?,
    override var region: String?,
    override var postcode: String?,
    override var country: String?

) : AddressEntity, NewDataEntity, MutableAddressEntity {

    // For consumer use.
    constructor() : this(null, null, null, null, null, null, null, null, null, null)

    @IgnoredOnParcel
    override var primaryValue: String? by this::formattedAddress

    @IgnoredOnParcel
    override val isPrimary: Boolean = false

    @IgnoredOnParcel
    override val isSuperPrimary: Boolean = false
}