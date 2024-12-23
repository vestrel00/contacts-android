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
     * ## Insert/update operations
     *
     * You have three different options when inserting/updating an [AddressEntity],
     *
     * 1. If the [formattedAddress] is null and there are non-null structured components provided
     *    (e.g. [street] and [city]), the Contacts Provider will automatically set the
     *   [formattedAddress] by combining the structured components.
     *
     * 2. If the [formattedAddress] is not null and all structured components are null, the Contacts
     *   Provider automatically (to the best of its ability) sets the values for all the structured
     *   components.
     *
     * 3. If the [formattedAddress] and structured components are not null, the Contacts Provider
     *    does nothing automatically.
     *
     * #### Important things to know about
     *
     * If your app only allows users to update the structured components and not the combined
     * [formattedAddress], you should set the [formattedAddress] to null when performing an update.
     * This means **option 1 is for you**. Otherwise, if you are trying to set all structured
     * components to null but you leave the [formattedAddress] not null, the Contacts Provider will
     * automatically set the value(s) of the structured components to a derived value from the
     * [formattedAddress]. In effect, your app would seemingly not allow users to clear the address.
     *
     * If your app only allows users to update the [formattedAddress] and not the structured
     * components, you should set the structured components to null when performing an update. This
     * means **option 2 is for you**. Otherwise, if you are trying to set the [formattedAddress] to
     * null but you leave the structured components not null, the Contacts Provider will
     * automatically set the value of the [formattedAddress] to a combined value from the structured
     * components. In effect, your app would seemingly not allow users to clear the
     * [formattedAddress].
     *
     * If you want to manually update both the [formattedAddress] and structured components with
     * your own custom algorithm, you may do so at your own discretion =)
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

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): AddressEntity

    enum class Type(override val value: Int) : DataEntity.Type {

        // Order of declaration is the same as seen in the AOSP contacts app
        HOME(CommonDataKinds.StructuredPostal.TYPE_HOME), // Default
        WORK(CommonDataKinds.StructuredPostal.TYPE_WORK),
        OTHER(CommonDataKinds.StructuredPostal.TYPE_OTHER),
        CUSTOM(CommonDataKinds.StructuredPostal.TYPE_CUSTOM);

        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.StructuredPostal.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = entries.find { it.value == value }
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

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableAddressEntity
}

/**
 * An existing immutable [AddressEntity].
 */
@ConsistentCopyVisibility
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
    override val country: String?,

    override val isRedacted: Boolean

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
        country = country,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        formattedAddress = formattedAddress?.redact(),
        street = street?.redact(),
        poBox = poBox?.redact(),
        neighborhood = neighborhood?.redact(),
        city = city?.redact(),
        region = region?.redact(),
        postcode = postcode?.redact(),
        country = country?.redact()
    )
}

/**
 * An existing mutable [AddressEntity].
 */
@ConsistentCopyVisibility
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
    override var country: String?,

    override val isRedacted: Boolean

) : AddressEntity, ExistingDataEntity, MutableAddressEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        formattedAddress = formattedAddress?.redact(),
        street = street?.redact(),
        poBox = poBox?.redact(),
        neighborhood = neighborhood?.redact(),
        city = city?.redact(),
        region = region?.redact(),
        postcode = postcode?.redact(),
        country = country?.redact()
    )
}

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
    override var country: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : AddressEntity, NewDataEntity, MutableAddressEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        formattedAddress = formattedAddress?.redact(),
        street = street?.redact(),
        poBox = poBox?.redact(),
        neighborhood = neighborhood?.redact(),
        city = city?.redact(),
        region = region?.redact(),
        postcode = postcode?.redact(),
        country = country?.redact()
    )
}