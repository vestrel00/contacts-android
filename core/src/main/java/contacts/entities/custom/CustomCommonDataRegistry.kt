package contacts.entities.custom

import contacts.AbstractCustomCommonDataField
import contacts.AbstractCustomCommonDataFieldSet
import contacts.entities.MimeType

/**
 * A global instance of [CustomCommonDataRegistry] that is used as the default throughout the API.
 * Use this instance to register custom data entries without having to keep a reference to it to
 * pass it around. For example, this is the default registry used in all util functions in the
 * [contacts.util] package.
 */
val GlobalCustomCommonDataRegistry = CustomCommonDataRegistry()

/**
 * Provides functions required to support custom common data, which have [MimeType.Custom].
 */
class CustomCommonDataRegistry {

    /**
     * Map of mime type value to an [Entry].
     */
    private val entryMap = mutableMapOf<String, Entry>()

    /**
     * Register a custom common data entry.
     *
     * ## Developer notes
     *
     * The types [F], [K], and [V] are not kept internally. They are erased. These are simply here
     * as compile-time checks for matching the generic types of parameter instances.
     */
    fun <F : AbstractCustomCommonDataField, K : AbstractCustomCommonDataCursor,
            V : AbstractMutableCustomCommonDataEntity> register(
        customMimeType: MimeType.Custom,
        customFieldSet: AbstractCustomCommonDataFieldSet<F>,
        customCommonDataCountRestriction: CustomCommonDataEntityCountRestriction,
        customCommonDataMapperFactory: AbstractCustomCommonDataEntityMapper.Factory<K, V>,
        customCommonDataOperationFactory: AbstractCustomCommonDataOperation.Factory<V>
    ) {
        entryMap[customMimeType.value] = Entry(
            customMimeType,
            customFieldSet,
            customCommonDataCountRestriction,
            customCommonDataMapperFactory,
            customCommonDataOperationFactory
        )
    }

    internal fun mimeTypeOf(
        mimeTypeValue: String
    ): MimeType.Custom? = entryMap[mimeTypeValue]?.mimeType

    internal fun fieldSetOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataFieldSet<*>? = entryMap[mimeType.value]?.fieldSet

    internal fun countRestrictionOf(
        mimeType: MimeType.Custom
    ): CustomCommonDataEntityCountRestriction? = entryMap[mimeType.value]?.countRestriction

    internal fun mapperFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataEntityMapper.Factory<*, *>? = entryMap[mimeType.value]?.mapperFactory

    internal fun operationFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataOperation.Factory<*>? = entryMap[mimeType.value]?.operationFactory

    private class Entry(
        val mimeType: MimeType.Custom,
        val fieldSet: AbstractCustomCommonDataFieldSet<*>,
        val countRestriction: CustomCommonDataEntityCountRestriction,
        val mapperFactory: AbstractCustomCommonDataEntityMapper.Factory<*, *>,
        val operationFactory: AbstractCustomCommonDataOperation.Factory<*>
    )
}