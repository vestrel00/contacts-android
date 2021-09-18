package contacts.ui.util

import contacts.entities.CommonDataEntity

/**
 * A wrapper around a [CommonDataEntity.Type] [T] that pairs it with the String value of its
 * [CommonDataEntity.Type.typeLabelResource].
 *
 * This is useful for displaying user and system types in a UI adapter
 * (e.g. [android.widget.ArrayAdapter] for [android.widget.Spinner]) without having to create
 * custom adapters.
 */
interface CommonDataEntityType<T : CommonDataEntity.Type> {
    /**
     * The type of data.
     */
    val type: T

    /**
     * The string value representing the [type].
     */
    val typeLabel: String

    /**
     * True if this is a user created custom type.
     */
    val userCustomType: Boolean
}