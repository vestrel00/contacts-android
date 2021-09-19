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

    class Factory : CommonDataEntityType.Factory<MutablePhone, Phone.Type, PhoneType> {

        override fun systemTypes(resources: Resources): MutableList<PhoneType> = Phone.Type.values()
            .asSequence()
            .map { type -> PhoneType(type, type.labelStr(resources, null)) }
            .toMutableList()

        override fun userCustomType(labelStr: String): PhoneType =
            PhoneType(Phone.Type.CUSTOM, labelStr)

        override fun from(resources: Resources, data: MutablePhone): PhoneType =
            (data.type ?: DEFAULT_TYPE).let { type ->
                PhoneType(type, type.labelStr(resources, data.label))
            }

        companion object {
            private val DEFAULT_TYPE = Phone.Type.MOBILE
        }
    }
}