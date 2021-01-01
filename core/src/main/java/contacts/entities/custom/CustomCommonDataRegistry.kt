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

    internal fun customMimeTypeOf(mimeTypeValue: String): MimeType.Custom? =
        entryMap[mimeTypeValue]?.customMimeType

    internal fun customFieldSetOf(mimeType: MimeType.Custom): AbstractCustomCommonDataFieldSet<*>? =
        entryMap[mimeType.value]?.customFieldSet

    internal fun customCommonDataCountRestrictionOf(
        mimeType: MimeType.Custom
    ): CustomCommonDataEntityCountRestriction? =
        entryMap[mimeType.value]?.customCommonDataCountRestriction

    internal fun customCommonDataMapperFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataEntityMapper.Factory<*, *>? =
        entryMap[mimeType.value]?.customCommonDataMapperFactory

    internal fun customCommonDataOperationFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataOperation.Factory<*>? =
        entryMap[mimeType.value]?.customCommonDataOperationFactory

    private class Entry(
        val customMimeType: MimeType.Custom,
        val customFieldSet: AbstractCustomCommonDataFieldSet<*>,
        val customCommonDataCountRestriction: CustomCommonDataEntityCountRestriction,
        val customCommonDataMapperFactory: AbstractCustomCommonDataEntityMapper.Factory<*, *>,
        val customCommonDataOperationFactory: AbstractCustomCommonDataOperation.Factory<*>
    )
}