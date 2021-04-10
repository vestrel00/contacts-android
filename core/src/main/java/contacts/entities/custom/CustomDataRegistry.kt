package contacts.entities.custom

import contacts.AbstractCustomDataField
import contacts.AbstractCustomDataFieldSet
import contacts.entities.MimeType
import contacts.entities.MutableRawContact
import contacts.entities.RawContact

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
     * Map of [MimeType.Custom.value] to an [Entry].
     */
    private val entryMap = mutableMapOf<String, Entry<
            AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity>>()

    /**
     * Register custom data [entries].
     */
    fun register(vararg entries: Entry<*, *, *>) {
        for (entry in entries) {
            @Suppress("UNCHECKED_CAST")
            entryMap[entry.mimeType.value] = entry as Entry<
                    AbstractCustomDataField,
                    AbstractCustomDataCursor<AbstractCustomDataField>,
                    MutableCustomDataEntity>
        }
    }

    /**
     * Register custom data entries via the given [registrations].
     */
    fun register(vararg registrations: EntryRegistration) {
        for (registration in registrations) {
            registration.registerTo(this)
        }
    }

    /**
     * Puts the given [customDataEntity] into the [rawContact].
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.AT_MOST_ONE], then the given
     * [customDataEntity] will replace any existing custom entity.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.NO_LIMIT], then the given
     * [customDataEntity] will be added and existing custom entities will remain.
     */
    fun putCustomDataEntityInto(
        rawContact: MutableRawContact, customDataEntity: MutableCustomDataEntity
    ) {
        val entry = entryOf(customDataEntity.mimeType)
        val entityHolder = rawContact.customDataEntities.getOrPut(entry.mimeType.value) {
            CustomDataEntityHolder(mutableListOf(), entry.countRestriction)
        }

        // Use when instead of if (entry.countRestriction == AT_MOST_ONE) for exhaustive checks.
        when (entry.countRestriction) {
            CustomDataCountRestriction.AT_MOST_ONE -> {
                // There can only be one of this type of entity so clear the list first.
                entityHolder.entities.clear()
                entityHolder.entities.add(customDataEntity)
            }
            CustomDataCountRestriction.NO_LIMIT -> {
                entityHolder.entities.add(customDataEntity)
            }
        }
    }

    /**
     * Removes any [MutableCustomDataEntity]s associated with the [MimeType.Custom] contained in the
     * given [rawContact], if any.
     */
    fun removeAllCustomDataEntityFrom(rawContact: MutableRawContact, mimeType: MimeType.Custom) {
        val entityHolder = rawContact.customDataEntities[mimeType.value]
        entityHolder?.entities?.clear()
    }

    /**
     * Returns the list of [MutableCustomDataEntity]s of type [T] associated with the
     * [MimeType.Custom] contained in the given [rawContact], if any.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.AT_MOST_ONE], then expect
     * 0 or 1 custom entity in the list.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.NO_LIMIT], then expect
     * 0, 1, or more custom entities in the list.
     */
    fun <T : MutableCustomDataEntity> customDataEntitiesFor(
        rawContact: RawContact, mimeType: MimeType.Custom
    ): List<T> = customDataEntitiesFor(rawContact.customDataEntities, mimeType)

    /**
     * See [customDataEntitiesFor].
     */
    fun <T : MutableCustomDataEntity> customDataEntitiesFor(
        rawContact: MutableRawContact, mimeType: MimeType.Custom
    ): List<T> = customDataEntitiesFor(rawContact.customDataEntities, mimeType)

    private fun <T : MutableCustomDataEntity> customDataEntitiesFor(
        customDataEntities: Map<String, CustomDataEntityHolder>, mimeType: MimeType.Custom
    ): List<T> {
        val entityHolder = customDataEntities[mimeType.value]

        @Suppress("UNCHECKED_CAST")
        return entityHolder?.entities?.toList() as? List<T> ?: emptyList()
    }

    internal fun entryOf(mimeType: MimeType.Custom) = entryOf(mimeType.value)

    internal fun entryOf(mimeTypeValue: String): Entry<AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity> = entryMap[mimeTypeValue]
        ?: throw CustomDataException("Missing custom data entry for $mimeTypeValue")

    internal fun mimeTypeOf(
        customDataField: AbstractCustomDataField
    ): MimeType.Custom = entryMap.values.find {
        it.fieldSet.all.contains(customDataField)
    }
        ?.mimeType
        ?: throw CustomDataException("Missing custom data entry for ${customDataField.mimeType}")

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

    /**
     * Performs the registration of an [Entry] to a [CustomDataRegistry].
     *
     * This is useful for library modules that want to keep their [Entry] implementation internal,
     * hidden from consumers.
     */
    interface EntryRegistration {

        /**
         * Registers an [Entry] to the given [customDataRegistry].
         */
        fun registerTo(customDataRegistry: CustomDataRegistry)
    }

}