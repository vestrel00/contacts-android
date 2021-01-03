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
     * Map of [EntryId] to an [Entry].
     */
    private val entryMap = mutableMapOf<EntryId, Entry<
            AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity>>()

    /**
     * Map of [MimeType.Custom.value] to an [EntryId].
     */
    private val mimeTypeEntryIdMap = mutableMapOf<String, EntryId>()

    /**
     * Register custom data [entries].
     */
    fun register(vararg entries: Entry<*, *, *>) {
        for (entry in entries) {
            mimeTypeEntryIdMap[entry.mimeType.value] = entry.id

            @Suppress("UNCHECKED_CAST")
            entryMap[entry.id] = entry as Entry<
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
     * Puts the given [customDataEntity] associated with the [entryId] into the [rawContact].
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.AT_MOST_ONE], then the given
     * [customDataEntity] will replace any existing custom entity.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.NO_LIMIT], then the given
     * [customDataEntity] will be added and existing custom entities will remain.
     */
    fun putCustomDataEntityInto(
        rawContact: MutableRawContact, customDataEntity: MutableCustomDataEntity, entryId: EntryId
    ) {
        val entry = entryOf(entryId)
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
     * Removes any [MutableCustomDataEntity]s associated with the [entryId] contained in the given
     * [rawContact], if any.
     *
     * This does not remove any other [MutableCustomDataEntity]s not associated with the [entryId].
     */
    fun removeAllCustomDataEntityFrom(rawContact: MutableRawContact, entryId: EntryId) {
        val entry = entryOf(entryId)
        val entityHolder = rawContact.customDataEntities[entry.mimeType.value]
        entityHolder?.entities?.clear()
    }

    /**
     * Returns the list of [MutableCustomDataEntity]s of type [T] associated with the [entryId]
     * contained in the given [rawContact], if any.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.AT_MOST_ONE], then expect
     * 0 or 1 custom entity in the list.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.NO_LIMIT], then expect
     * 0, 1, or more custom entities in the list.
     */
    fun <T : MutableCustomDataEntity> customDataEntitiesFor(
        rawContact: RawContact, entryId: EntryId
    ): List<T> = customDataEntitiesFor(rawContact.customDataEntities, entryId)

    /**
     * See [customDataEntitiesFor].
     */
    fun <T : MutableCustomDataEntity> customDataEntitiesFor(
        rawContact: MutableRawContact, entryId: EntryId
    ): List<T> = customDataEntitiesFor(rawContact.customDataEntities, entryId)

    private fun <T : MutableCustomDataEntity> customDataEntitiesFor(
        customDataEntities: Map<String, CustomDataEntityHolder>, entryId: EntryId
    ): List<T> {
        val mimeTypeValue = entryOf(entryId).mimeType.value
        val entityHolder = customDataEntities[mimeTypeValue]

        @Suppress("UNCHECKED_CAST")
        return entityHolder?.entities?.toList() as? List<T> ?: emptyList()
    }

    private fun entryOf(entryId: EntryId): Entry<AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity> {

        return entryMap[entryId]
            ?: throw CustomDataException("Missing custom data entry for $entryId")
    }

    internal fun entryOf(mimeTypeValue: String) = entryOf(
        entryMap[mimeTypeEntryIdMap[mimeTypeValue]]
            ?.mimeType
            ?: throw CustomDataException("Missing custom data entry for $mimeTypeValue")
    )

    internal fun entryOf(mimeType: MimeType.Custom): Entry<AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            MutableCustomDataEntity> {

        // Not using fun x() = here because this looks neater in this case.
        return entryMap[mimeTypeEntryIdMap[mimeType.value]]
            ?: throw CustomDataException("Missing custom data entry for ${mimeType.value}")
    }

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
     * Any object that serves as the unique ID to an [Entry], which allows entry registrations to
     * interface with a [CustomDataRegistry] instance for things like getting the custom data
     * attached to a RawContact.
     *
     * A reference to this object should be kept in order to interface with the [CustomDataRegistry]
     * instance.
     *
     * This allows library modules to access [CustomDataRegistry] functions without exposing it to
     * consumers. This can be done by keeping the [EntryId] object internal to the library module.
     * For this reason, this should NOT be anything that consumers have access to such as the
     * [MimeType.Custom].
     *
     * This is not enforced. Do what you want. This simply provides a way to keep unnecessary
     * implementations hidden from consumers. Whether you practice this or not is up to you.
     *
     * FIXME? Does Google's @RestrictTo(RestrictTo.Scope.LIBRARY) work here?
     * https://github.com/google/error-prone/issues/812
     * Stuff like this wouldn't be necessary if we have the ability to restrict access by using
     * annotations.
     */
    interface EntryId

    /**
     * A custom common data entry provides all the required implementations to support queries,
     * inserts, updates, and deletes.
     */
    interface Entry<F : AbstractCustomDataField, K : AbstractCustomDataCursor<F>,
            V : MutableCustomDataEntity> {
        val id: EntryId
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