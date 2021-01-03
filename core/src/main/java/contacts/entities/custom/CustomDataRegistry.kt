package contacts.entities.custom

import contacts.AbstractCustomDataField
import contacts.AbstractCustomDataFieldSet
import contacts.entities.MimeType

/**
 * A global instance of [CustomDataRegistry] that is used as the default throughout the API.
 *
 * Use this instance to register custom data entries without having to keep a reference to it to
 * pass it around. For example, this is the default registry used in all util functions in the
 * [contacts.util] package.
 */
val GlobalCustomDataRegistry = CustomDataRegistry()

/**
 * Provides functions required to support custom common data, which have [MimeType.Custom].
 */
class CustomDataRegistry {

    /**
     * Map of mime type value to an [Entry]. Entry types are casted to their base types.
     */
    private val entryMap = mutableMapOf<String, Entry<
            AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity>>()

    /**
     * Register a custom common data entry.
     */
    fun register(entry: Entry<*, *, *>) {
        // Cast the specific types as we don't need to know about them internally.
        @Suppress("UNCHECKED_CAST")
        entryMap[entry.mimeType.value] = entry as Entry<
                AbstractCustomDataField,
                AbstractCustomDataCursor<AbstractCustomDataField>,
                MutableCustomDataEntity>
    }

    internal fun entryOf(mimeType: MimeType.Custom):
            Entry<AbstractCustomDataField,
                    AbstractCustomDataCursor<AbstractCustomDataField>,
                    MutableCustomDataEntity>? = entryMap[mimeType.value]

    internal fun mimeTypeOf(
        mimeTypeValue: String
    ): MimeType.Custom? = entryMap[mimeTypeValue]?.mimeType

    internal fun mimeTypeOf(
        customDataField: AbstractCustomDataField
    ): MimeType.Custom? = entryMap.values.find {
        it.fieldSet.all.contains(customDataField)
    }?.mimeType

    internal fun allFields(): Set<AbstractCustomDataField> = entryMap.values
        .flatMap { it.fieldSet.all }
        .toSet()

    /**
     * A custom common data entry that provides all the required mechanisms to support queries,
     * inserts, updates, and deletes.
     *
     * ## Developer notes
     *
     * The specific types [F], [K], and [V] are not kept internally. Only the generic base types are
     * kept. These specific types provide compile-time checks to make sure consumers are providing
     * the correct implementations.
     */
    class Entry<F : AbstractCustomDataField, K : AbstractCustomDataCursor<F>,
            V : MutableCustomDataEntity>(
        internal val mimeType: MimeType.Custom,
        internal val fieldSet: AbstractCustomDataFieldSet<F>,
        internal val fieldMapper: CustomDataFieldMapper<F, V>,
        internal val countRestriction: CustomDataCountRestriction,
        internal val mapperFactory: AbstractCustomEntityMapper.Factory<F, K, V>,
        internal val operationFactory: AbstractCustomDataOperation.Factory<F, V>
    )
}