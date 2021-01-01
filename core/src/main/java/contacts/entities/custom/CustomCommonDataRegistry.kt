package contacts.entities.custom

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
     */
    fun register(
        customMimeType: MimeType.Custom,
        customFieldSet: AbstractCustomCommonDataFieldSet<*>,
        customCommonDataMapperFactory: AbstractCustomCommonDataEntityMapper.Factory<*>,
        customCommonDataOperationFactory: AbstractCustomCommonDataOperation.Factory<*>
    ) {
        entryMap[customMimeType.value] = Entry(
            customMimeType,
            customFieldSet,
            customCommonDataMapperFactory,
            customCommonDataOperationFactory
        )
    }

    internal fun customMimeTypeOf(mimeTypeValue: String): MimeType.Custom? =
        entryMap[mimeTypeValue]?.customMimeType

    internal fun customFieldSetOf(mimeType: MimeType.Custom): AbstractCustomCommonDataFieldSet<*>? =
        entryMap[mimeType.value]?.customFieldSet

    internal fun customCommonDataMapperFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataEntityMapper.Factory<*>? =
        entryMap[mimeType.value]?.customCommonDataMapperFactory

    internal fun customCommonDataOperationFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomCommonDataOperation.Factory<*>? =
        entryMap[mimeType.value]?.customCommonDataOperationFactory

    private class Entry(
        val customMimeType: MimeType.Custom,
        val customFieldSet: AbstractCustomCommonDataFieldSet<*>,
        val customCommonDataMapperFactory: AbstractCustomCommonDataEntityMapper.Factory<*>,
        val customCommonDataOperationFactory: AbstractCustomCommonDataOperation.Factory<*>
    )
}