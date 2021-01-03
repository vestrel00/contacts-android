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
     * Map of mime type value to an [Entry].
     */
    private val entryMap = mutableMapOf<String, Entry>()

    /**
     * Register a custom common data entry.
     *
     * ## Developer notes
     *
     * The types [F], [K], and [V] are not kept internally. They are erased. These are simply here
     * as compile-time checks for matching the generic types of parameter instances to make sure
     * consumers are providing the correct implementations.
     */
    fun <F : AbstractCustomDataField, K : AbstractCustomDataCursor<F>,
            V : MutableCustomDataEntity> register(
        customMimeType: MimeType.Custom,
        customFieldSet: AbstractCustomDataFieldSet<F>,
        fieldMapper: CustomDataFieldMapper<F, V>,
        countRestriction: CustomDataCountRestriction,
        mapperFactory: AbstractCustomEntityMapper.Factory<F, K, V>,
        operationFactory: AbstractCustomDataOperation.Factory<F, V>
    ) {
        entryMap[customMimeType.value] = Entry(
            customMimeType,
            customFieldSet,
            fieldMapper,
            countRestriction,
            mapperFactory,
            operationFactory
        )
    }

    internal fun mimeTypeOf(
        mimeTypeValue: String
    ): MimeType.Custom? = entryMap[mimeTypeValue]?.mimeType

    internal fun mimeTypeOf(
        customDataField: AbstractCustomDataField
    ): MimeType.Custom? = entryMap.values.find {
        it.fieldSet.all.contains(customDataField)
    }?.mimeType

    internal fun fieldSetOf(
        mimeType: MimeType.Custom
    ): AbstractCustomDataFieldSet<*>? = entryMap[mimeType.value]?.fieldSet

    internal fun allFields(): Set<AbstractCustomDataField> = entryMap.values
        .flatMap { it.fieldSet.all }
        .toSet()

    internal fun fieldMapperOf(
        mimeType: MimeType.Custom
    ): CustomDataFieldMapper<*, *>? = entryMap[mimeType.value]?.fieldMapper

    internal fun countRestrictionOf(
        mimeType: MimeType.Custom
    ): CustomDataCountRestriction? = entryMap[mimeType.value]?.countRestriction

    internal fun mapperFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomEntityMapper.Factory<*, *, *>? = entryMap[mimeType.value]?.mapperFactory

    internal fun operationFactoryOf(
        mimeType: MimeType.Custom
    ): AbstractCustomDataOperation.Factory<*, *>? = entryMap[mimeType.value]?.operationFactory

    // TODO Add types and expose to consumers so that the register function is clean.
    // TODO Get rid of all internal fun and replace it with one internal function that returns the entry
    private class Entry(
        val mimeType: MimeType.Custom,
        val fieldSet: AbstractCustomDataFieldSet<*>,
        val fieldMapper: CustomDataFieldMapper<*, *>,
        val countRestriction: CustomDataCountRestriction,
        val mapperFactory: AbstractCustomEntityMapper.Factory<*, *, *>,
        val operationFactory: AbstractCustomDataOperation.Factory<*, *>
    )
}