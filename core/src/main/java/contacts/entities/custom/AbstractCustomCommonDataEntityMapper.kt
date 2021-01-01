package contacts.entities.custom

import android.database.Cursor
import contacts.entities.mapper.EntityMapper

/**
 * An abstract class that is used as a base of all custom [EntityMapper]s. It uses a
 * [AbstractCustomCommonDataCursor] [K] and outputs a [CustomCommonDataEntity] [V].
 */
abstract class AbstractCustomCommonDataEntityMapper<K : AbstractCustomCommonDataCursor,
        out V : MutableCustomCommonDataEntity>(
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
     * Creates instances of [AbstractCustomCommonDataEntityMapper].
     */
    abstract class Factory<K : AbstractCustomCommonDataCursor,
            out V : MutableCustomCommonDataEntity> {

        /**
         * Creates instances of [AbstractCustomCommonDataEntityMapper] with the given [cursor].
         */
        abstract fun create(cursor: Cursor): AbstractCustomCommonDataEntityMapper<K, V>
    }
}