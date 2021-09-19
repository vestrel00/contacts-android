package contacts.ui.util

import android.content.res.Resources
import contacts.entities.CommonDataEntity
import contacts.entities.MutableCommonDataEntity

/**
 * A wrapper around a [CommonDataEntity.Type] [T] that pairs it with the String value of its
 * [CommonDataEntity.Type.labelStr].
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
     * Creates instance of [CommonDataEntityType].
     */
    // TODO? Get rid of CommonDataEntityType and just do all of this in CommonDataEntity.Type ???
    interface Factory<K : MutableCommonDataEntity, T : CommonDataEntity.Type, V : CommonDataEntityType<T>> {
        /**
         * Returns all the system types.
         *
         * The [typeLabel] is assigned by the system.
         */
        fun systemTypes(resources: Resources): MutableList<V>

        /**
         * Creates a new user custom [CommonDataEntityType] [V] with the given [labelStr].
         *
         * The [typeLabel] is assigned by the user (assuming that the [typeLabel] came from user
         * input).
         *
         * A user custom type is a custom type whose label is from user input instead of the system.
         */
        fun userCustomType(labelStr: String): V

        /**
         * Returns the [CommonDataEntityType] [V] of the given [data] [K].
         *
         * If the [CommonDataEntity.Type] [T] is null, it should default to a non-null system type.
         *
         * The [typeLabel] is assigned by the label provided in the underlying [data] [K].
         *
         * This can be a user custom type or a system type.
         */
        fun from(resources: Resources, data: K): V
    }
}