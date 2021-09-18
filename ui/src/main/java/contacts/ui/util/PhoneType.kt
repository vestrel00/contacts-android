package contacts.ui.util

import android.content.res.Resources
import contacts.entities.MutablePhone
import contacts.entities.Phone

/**
 * A wrapper around [Phone.Type] that pairs it with the String value of its
 * [Phone.Type.typeLabelResource].
 *
 * This is useful for displaying user and system types in a UI adapter
 * (e.g. [android.widget.ArrayAdapter] for [android.widget.Spinner]) without having to create
 * custom adapters.
 *
 * ## Developer Notes
 *
 * We could instead make the [Phone.Type] a sealed class to achieve the same effect. However,
 * that would complicate the [Phone] class because the @Parcelize extension does not support
 * sealed classes with the library's design.
 */
data class PhoneType internal constructor(
    override val type: Phone.Type,
    override val typeLabel: String,
    override val userCustomType: Boolean
) : CommonDataEntityType<Phone.Type> {

    override fun toString(): String = typeLabel

    companion object {

        private val DEFAULT_TYPE = Phone.Type.MOBILE

        /**
         * Returns all the system phone types.
         */
        fun systemTypes(resources: Resources): MutableList<PhoneType> = Phone.Type.values()
            .asSequence()
            .map { type -> PhoneType(type, resources.getString(type.typeLabelResource), false) }
            .toMutableList()

        /**
         * Creates a new [PhoneType] with the given [typeLabel] with a type of [Phone.Type.CUSTOM].
         */
        fun userCustomType(typeLabel: String): PhoneType = from(Phone.Type.CUSTOM, typeLabel)

        /**
         * Returns the [PhoneType] of the given [phone].
         *
         * If the [Phone.type] is null, it will default to [DEFAULT_TYPE]. If it is
         * [Phone.Type.CUSTOM], [PhoneType.userCustomType] will be true.
         */
        fun from(resources: Resources, phone: Phone): PhoneType =
            from(resources, phone.type, phone.label)

        /**
         * See [from].
         */
        fun from(resources: Resources, phone: MutablePhone): PhoneType =
            from(resources, phone.type, phone.label)

        private fun from(resources: Resources, type: Phone.Type?, label: String?): PhoneType {
            val nonNullType = type ?: DEFAULT_TYPE
            return from(nonNullType, nonNullType.typeLabel(resources, label))
        }

        private fun from(type: Phone.Type, typeLabel: String): PhoneType = PhoneType(
            type, typeLabel, type == Phone.Type.CUSTOM
        )
    }
}