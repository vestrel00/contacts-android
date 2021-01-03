package contacts.entities.custom

import contacts.AbstractCustomDataField
import contacts.AbstractCustomDataFieldSet
import contacts.Contacts
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
 * Registry of custom data components, enabling queries, inserts, updates, and deletes for custom
 * data.
 */
class CustomDataRegistry {

    /**
     * Map of mime type value to an [Entry].
     */
    private val entryMap = mutableMapOf<String, Entry<
            AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity>>()

    /**
     * Register a custom common data entry.
     */
    fun register(entry: Entry<*, *, *>) {
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
     * A custom common data entry provides all the required implementations to support queries,
     * inserts, updates, and deletes.
     */
    interface Entry<F : AbstractCustomDataField, K : AbstractCustomDataCursor<F>,
            V : MutableCustomDataEntity> {
        val mimeType: MimeType.Custom
        val fieldSet: AbstractCustomDataFieldSet<F>
        val fieldMapper: CustomDataFieldMapper<F, V>
        val countRestriction: CustomDataCountRestriction
        val mapperFactory: AbstractCustomEntityMapper.Factory<F, K, V>
        val operationFactory: AbstractCustomDataOperation.Factory<F, V>
    }

}