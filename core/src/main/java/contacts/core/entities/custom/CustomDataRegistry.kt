package contacts.core.entities.custom

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.entities.*

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
            CustomDataEntity>>()

    /**
     * Register custom data [entries].
     */
    @SafeVarargs
    fun register(vararg entries: Entry<*, *, *>) {
        for (entry in entries) {
            @Suppress("UNCHECKED_CAST")
            entryMap[entry.mimeType.value] = entry as Entry<
                    AbstractCustomDataField,
                    AbstractCustomDataCursor<AbstractCustomDataField>,
                    CustomDataEntity>
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
     *
     * The [customDataEntity] can be mutable (or immutable if there is no mutable type).
     */
    fun putCustomDataEntityInto(
        rawContact: MutableRawContact, customDataEntity: CustomDataEntity
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
     * Removes all instances of the given custom [entity] from the given [rawContact].
     */
    fun removeCustomDataEntityFrom(
        rawContact: MutableRawContact,
        byReference: Boolean,
        entity: CustomDataEntity
    ) {
        val entityHolder = rawContact.customDataEntities[entity.mimeType.value]
        entityHolder?.entities?.removeAll(entity, byReference)
    }

    /**
     * Removes any [CustomDataEntity]s associated with the [MimeType.Custom] contained in the
     * given [rawContact], if any.
     */
    fun removeAllCustomDataEntityFrom(rawContact: MutableRawContact, mimeType: MimeType.Custom) {
        val entityHolder = rawContact.customDataEntities[mimeType.value]
        entityHolder?.entities?.clear()
    }

    /**
     * Returns the list of [CustomDataEntity]s of type [T] associated with the [MimeType.Custom]
     * contained in the given [rawContact], if any.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.AT_MOST_ONE], then expect
     * 0 or 1 custom entity in the list.
     *
     * If the [Entry.countRestriction] is [CustomDataCountRestriction.NO_LIMIT], then expect
     * 0, 1, or more custom entities in the list.
     */
    fun <T : ImmutableCustomDataEntity> customDataEntitiesFor(
        rawContact: RawContact, mimeType: MimeType.Custom
    ): List<T> = customDataEntitiesFor(rawContact.customDataEntities, mimeType)

    /**
     * See [customDataEntitiesFor].
     */
    fun <T : CustomDataEntity> customDataEntitiesFor(
        rawContact: MutableRawContact, mimeType: MimeType.Custom
    ): List<T> = customDataEntitiesFor(rawContact.customDataEntities, mimeType)

    private fun <T : CustomDataEntity> customDataEntitiesFor(
        customDataEntities: Map<String, CustomDataEntityHolder>, mimeType: MimeType.Custom
    ): List<T> {
        val entityHolder = customDataEntities[mimeType.value]

        @Suppress("UNCHECKED_CAST")
        return entityHolder?.entities?.toList() as? List<T> ?: emptyList()
    }

    internal fun entryOf(mimeType: MimeType.Custom) = entryOf(mimeType.value)

    internal fun entryOf(mimeTypeValue: String): Entry<AbstractCustomDataField,
            AbstractCustomDataCursor<AbstractCustomDataField>,
            CustomDataEntity> = entryMap[mimeTypeValue]
        ?: throw CustomDataException("Missing custom data entry for $mimeTypeValue")

    internal fun mimeTypeOf(
        customDataField: AbstractCustomDataField
    ): MimeType.Custom = entryMap.values.find {
        it.fieldSet.all.contains(customDataField)
    }
        ?.mimeType
        ?: throw CustomDataException("Missing custom data entry for ${customDataField.mimeType}")

    /**
     * Returns all registered custom data fields.
     *
     * ## Dev note
     *
     * Make sure this remains public (not internal) because consumers may find this useful when
     * using include functions in query APIs.
     */
    fun allFields(): Set<AbstractCustomDataField> = entryMap.values
        .flatMap { it.fieldSet.all }
        .toSet()

    /**
     * A custom data entry provides all the required implementations to support queries,
     * inserts, updates, and deletes.
     */
    interface Entry<F : AbstractCustomDataField, C : AbstractCustomDataCursor<F>,
            E : CustomDataEntity> {
        val mimeType: MimeType.Custom
        val fieldSet: AbstractCustomDataFieldSet<F>
        val fieldMapper: CustomDataFieldMapper<F, E>
        val countRestriction: CustomDataCountRestriction
        val mapperFactory: AbstractCustomEntityMapper.Factory<F, C, E>
        val operationFactory: AbstractCustomDataOperation.Factory<F, E>
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