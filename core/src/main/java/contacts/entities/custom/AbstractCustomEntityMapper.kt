package contacts.entities.custom

import android.database.Cursor
import contacts.AbstractCustomDataField
import contacts.entities.mapper.EntityMapper

/**
 * An abstract class that is used as a base of all custom [EntityMapper]s. It uses a
 * [AbstractCustomDataCursor] [K] (using fields of type [F]) and outputs a [CustomDataEntity] [V].
 */
abstract class AbstractCustomEntityMapper<
        F : AbstractCustomDataField,
        K : AbstractCustomDataCursor<F>,
        out V : MutableCustomDataEntity>(
    private val cursor: K
) : EntityMapper<V> {

    /**
     * Returns the custom common data entity [V] created with values provided by the [cursor].
     */
    protected abstract fun value(cursor: K): V

    /*
     * Invokes the abstract value function to force consumers to not assign a value for this and
     * instead calculate it every time this is invoked. This prevents consumers from making the
     * mistake of assigning a value to this instead of using a getter.
     */
    final override val value: V
        get() = value(cursor)

    /**
     * Creates instances of [AbstractCustomEntityMapper].
     */
    interface Factory<
            F : AbstractCustomDataField,
            K : AbstractCustomDataCursor<F>,
            out V : MutableCustomDataEntity> {

        /**
         * Creates instances of [AbstractCustomEntityMapper] with the given [cursor].
         */
        fun create(cursor: Cursor): AbstractCustomEntityMapper<F, K, V>
    }
}