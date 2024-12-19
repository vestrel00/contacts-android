package contacts.ui.entities

import contacts.core.entities.DataEntity

/**
 * A wrapper around a [DataEntity.Type] [T] that pairs the underlying type with its string
 * representation [typeLabel] (derived from the [DataEntity.Type.labelStr]).
 *
 * This is useful for displaying user-created and system-provided types in a UI adapter
 * (e.g. [android.widget.ArrayAdapter] for [android.widget.Spinner]) without having to create
 * custom adapters.
 */
@ConsistentCopyVisibility
data class DataEntityType<T : DataEntity.Type> internal constructor(

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