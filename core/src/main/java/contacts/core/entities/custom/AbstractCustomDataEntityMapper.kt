package contacts.core.entities.custom

import android.database.Cursor
import contacts.core.AbstractCustomDataField
import contacts.core.entities.CustomDataEntity
import contacts.core.entities.ExistingCustomDataEntity
import contacts.core.entities.mapper.CustomDataEntityMapper

/**
 * Base type of all custom [CustomDataEntityMapper]s. It uses a [AbstractCustomDataCursor] [C]
 * (using fields of type [F]) and outputs a [CustomDataEntity] [E].
 */
abstract class AbstractCustomDataEntityMapper<
        F : AbstractCustomDataField,
        C : AbstractCustomDataCursor<F>,
        out E : ExistingCustomDataEntity>(
    private val cursor: C
) : CustomDataEntityMapper<E> {

    /**
     * Returns the custom data entity [E] created with values provided by the [cursor].
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
     * Creates instances of [AbstractCustomDataEntityMapper].
     */
    interface Factory<
            F : AbstractCustomDataField,
            C : AbstractCustomDataCursor<F>,
            out E : ExistingCustomDataEntity> {

        /**
         * Creates instances of [AbstractCustomDataEntityMapper] with the given [cursor].
         *
         * Only the data whose corresponding fields are in [includeFields] will be included in
         * query results. If [includeFields] is null, then all data will be included in the results.
         */
        fun create(cursor: Cursor, includeFields: Set<F>?): AbstractCustomDataEntityMapper<F, C, E>
    }
}