package com.vestrel00.contacts

@Suppress("FunctionName")
internal fun Include(vararg fields: Field) = Include(fields.asSequence())

@Suppress("FunctionName")
internal fun Include(fields: Collection<Field>) = Include(fields.asSequence())

@Suppress("FunctionName")
internal fun Include(fields: Sequence<Field>) = Include(
    // The call toSet is important because it gets rid of duplicates. We can also call
    // distinct() but then we can't call toTypedArray after it. And no, this is not more
    // expensive than calling distinct, it is actually cheaper. Distinct uses a HashSet
    // internally in addition to extra computations.
    fields.map { it.columnName }.toSet()
)

@Suppress("FunctionName")
internal fun Include(fieldSet: FieldSet<*>) = Include(fieldSet.all)

internal class Include(val columnNames: Set<String>) {

    override fun toString(): String = columnNames.joinToString(", ")
}

/**
 * Returns a new instance of [Include] where only the given [fields] in [this] are included.
 */
internal fun Include.onlyFieldsIn(fields: Collection<Field>) = Include(
    Include(fields).columnNames.intersect(columnNames)
)

/**
 * Returns a new instance of [Include] where only [ContactsFields] in [this] are included.
 *
 * This is used to convert an [Include] of [DataFields] to [ContactsFields].
 */
internal fun Include.onlyContactsFields() = Include(
    Include(ContactsFields.all).columnNames
        .intersect(columnNames)
        .asSequence()
        // JoinedContactsFields.Id has a different columnName than ContactsFields.Id.
        .plus(ContactsFields.Id.columnName)
        .toSet()
)

/**
 * Returns a new instance of [Include] where only [RawContactsFields] in [this] are included.
 *
 * This is used to convert an [Include] of [DataFields] to [RawContactsFields].
 */
internal fun Include.onlyRawContactsFields() = Include(
    Include(RawContactsFields.all).columnNames
        .intersect(columnNames)
        .asSequence()
        // JoinedRawContactsFields.Id has a different columnName than RawContactsFields.Id.
        .plus(RawContactsFields.Id.columnName)
        // JoinedContactsFields.Id has a different columnName than RawContactsFields.ContactId.
        .plus(RawContactsFields.ContactId.columnName)
        .toSet()
)