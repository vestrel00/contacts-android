package contacts.ui.util

import android.content.res.Resources
import contacts.entities.MutablePhone
import contacts.entities.Phone

/**
 * A wrapper around [Phone.Type] that pairs it with the value of [Phone.Type.labelStr].
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
) : CommonDataEntityType<Phone.Type> {

    override fun toString(): String = typeLabel

    companion object {

        private val DEFAULT_TYPE = Phone.Type.MOBILE

        /**
         * Returns all the system phone types.
         *
         * The [typeLabel] is assigned by the system.
         */
        fun systemTypes(resources: Resources): MutableList<PhoneType> = Phone.Type.values()
            .asSequence()
            .map { type -> PhoneType(type, type.labelStr(resources, null)) }
            .toMutableList()

        /**
         * Creates a new [PhoneType] with the given [typeLabel] with a type of [Phone.Type.CUSTOM].
         *
         * The [typeLabel] is assigned by the user (assuming that the [typeLabel] came from user
         * input).
         */
        fun userCustomType(typeLabel: String): PhoneType = PhoneType(Phone.Type.CUSTOM, typeLabel)

        /**
         * Returns the [PhoneType] of the given [phone].
         *
         * If the [Phone.type] is null, it will default to [DEFAULT_TYPE].
         *
         * The [typeLabel] is assigned by the [Phone.label].
         */
        fun from(resources: Resources, phone: MutablePhone): PhoneType =
            (phone.type ?: DEFAULT_TYPE).let { type ->
                PhoneType(type, type.labelStr(resources, phone.label))
        }
    }
}