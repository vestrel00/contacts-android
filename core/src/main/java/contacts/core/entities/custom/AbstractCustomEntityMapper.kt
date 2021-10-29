package contacts.core.entities.custom

import android.database.Cursor
import contacts.core.AbstractCustomDataField
import contacts.core.entities.mapper.EntityMapper

/**
 * An abstract class that is used as a base of all custom [EntityMapper]s. It uses a
 * [AbstractCustomDataCursor] [C] (using fields of type [F]) and outputs a [CustomDataEntity] [E].
 */
abstract class AbstractCustomEntityMapper<
        F : AbstractCustomDataField,
        C : AbstractCustomDataCursor<F>,
        out E : MutableCustomDataEntity>(
    private val cursor: C
) : EntityMapper<E> {

    /**
     * Returns the custom common data entity [E] created with values provided by the [cursor].
     */
    protected abstract fun value(cursor: C): E

    /*
     * Invokes the abstract value function to force consumers to not assign a value for this and
     * instead calculate it every time this is invoked. This prevents consumers from making the
     * mistake of assigning a value to this instead of using a getter.
     */
    final override val value: E
        get() = value(cursor)

    /**
     * Creates instances of [AbstractCustomEntityMapper].
     */
    interface Factory<
            F : AbstractCustomDataField,
            C : AbstractCustomDataCursor<F>,
            out E : MutableCustomDataEntity> {

        /**
         * Creates instances of [AbstractCustomEntityMapper] with the given [cursor].
         *
         * Only the fields specified in [includeFields] will be included in query results.
         */
        fun create(cursor: Cursor, includeFields: Set<F>): AbstractCustomEntityMapper<F, C, E>
    }
}