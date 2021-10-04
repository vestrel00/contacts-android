package contacts.entities.custom.handlename

import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry

/**
 * Returns the sequence of [HandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun RawContact.handleNames(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Sequence<HandleName> {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<MutableHandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence().map { it.toHandleName() }
}

/**
 * Returns the list of [HandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun RawContact.handleNameList(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): List<HandleName> = handleNames(customDataRegistry).toList()

/**
 * Returns the sequence of [MutableHandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun MutableRawContact.handleNames(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Sequence<MutableHandleName> {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<MutableHandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MutableHandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun MutableRawContact.handleNameList(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): List<MutableHandleName> = handleNames(customDataRegistry).toList()

/**
 * Adds the given [handleName] to this RawContact.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.addHandleName(
    handleName: MutableHandleName,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    customDataRegistry.putCustomDataEntityInto(this, handleName)
}

/**
 * Adds a handle name (configured by [configureHandleName]) to this RawContact.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.addHandleName(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    configureHandleName: MutableHandleName.() -> Unit
) {
    addHandleName(MutableHandleName().apply(configureHandleName), customDataRegistry)
}

/**
 * Removes all instances of the given [handleName] from this RawContact.
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableRawContact] object.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableRawContact.removeHandleName(
    handleName: MutableHandleName,
    byReference: Boolean = false,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    customDataRegistry.removeCustomDataEntityFrom(this, byReference, handleName)
}

/**
 * Removes all handle names from this RawContact.
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableRawContact] object.
 */
fun MutableRawContact.removeAllHandleNames(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    customDataRegistry.removeAllCustomDataEntityFrom(this, HandleNameMimeType)
}
