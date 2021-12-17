package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.AddressEntity.Type
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing a postal addresses.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface AddressEntity : DataEntityWithTypeAndLabel<Type> {

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

    /**
     * The [formattedAddress].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::formattedAddress
    override val primaryValue: String?
        get() = formattedAddress

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
 * We only create abstractions when they are necessary!
 *
 * Apart from AddressEntity, there is only one interface that extends it; MutableAddressEntity.
 *
 * The MutableAddressEntity interface is used for library constructs that require an AddressEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableAddress and NewAddress. With this, we can create constructs that can
 * keep a reference to MutableAddress(es) or NewAddress(es) through the MutableAddressEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewAddressEntity, ExistingAddressEntity, and
 * ImmutableAddressEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

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

    /**
     * The [formattedAddress].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::formattedAddress
    override var primaryValue: String?
        get() = formattedAddress
        set(value) {
            formattedAddress = value
        }
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

) : AddressEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableAddress> {

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

) : AddressEntity, ExistingDataEntity, MutableAddressEntity

/**
 * A new mutable [AddressEntity].
 */
@Parcelize
data class NewAddress @JvmOverloads constructor(

    override var type: Type? = null,
    override var label: String? = null,

    override var formattedAddress: String? = null,
    override var street: String? = null,
    override var poBox: String? = null,
    override var neighborhood: String? = null,
    override var city: String? = null,
    override var region: String? = null,
    override var postcode: String? = null,
    override var country: String? = null

) : AddressEntity, NewDataEntity, MutableAddressEntity