package contacts.ui.entities

import contacts.core.entities.CommonDataEntity

/**
 * A wrapper around a [CommonDataEntity.Type] [T] that pairs the underlying type with its string
 * representation [typeLabel] (derived from the [CommonDataEntity.Type.labelStr]).
 *
 * This is useful for displaying user-created and system-provided types in a UI adapter
 * (e.g. [android.widget.ArrayAdapter] for [android.widget.Spinner]) without having to create
 * custom adapters.
 */
data class CommonDataEntityType<T : CommonDataEntity.Type> internal constructor(

    /**
     * The type of data.
     */
    val type: T,

    /**
     * The string value representing the [type].
     */
    val typeLabel: String,

    /**
     * True if the underlying [type] is a custom type and if the [typeLabel] provided comes from
     * user input instead of a system-defined string.
     */
    val isUserCustomType: Boolean

) {

    /**
     * True if the underlying [type] is a custom type and if the [typeLabel] provided comes from
     * a system-defined string.
     */
    val isSystemCustomType: Boolean
        get() = type.isCustomType && !isUserCustomType

    /**
     * The string representation of the underlying [type].
     */
    override fun toString(): String = typeLabel
}