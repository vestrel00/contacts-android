package com.vestrel00.contacts

@Suppress("FunctionName")
internal fun Include(vararg fields: Field): Include = Include(fields.asSequence())

internal class Include(fields: Sequence<Field>) {

    val fields: Set<AbstractField> by lazy(LazyThreadSafetyMode.NONE) {
        // Couldn't find a clean way of writing this as fields.map... because of the varying types.
        // We don't always have to use map or flatMap =)
        mutableSetOf<AbstractField>().apply {
            for (field in fields) {
                when (field) {
                    is AbstractField -> add(field)
                    is FieldSet -> addAll(field.fields)
                }
            }
        }
    }

    val columnNames: Array<out String> by lazy(LazyThreadSafetyMode.NONE) {
        // The call toSet is important because it gets rid of duplicates. We can also call
        // distinct() but then we can't call toTypedArray after it. And no, this is not more
        // expensive than calling distinct, it is actually cheaper. Distinct uses a HashSet
        // internally in addition to extra computations.
        this.fields.asSequence().map { it.columnName }.toSet().toTypedArray()
    }

    override fun toString(): String = columnNames.joinToString(", ")
}

/**
 * Returns a new instance of [Include] where only Contacts fields in [this] are included.
 */
internal fun Include.onlyContactsFields() = Include(
    // Contacts.Id belong to the Contacts table. Contact.Id belongs to the Data table.
    // So we just add the Contacts.Id since it is required anyways.
    ContactsFields.fields
        .intersect(fields)
        .asSequence()
        .plus(ContactsFields.Id)
)

/**
 * Returns a new instance of [Include] where only RawContacts fields in [this] are included.
 */
internal fun Include.onlyRawContactFields() = Include(
    // Contacts.Id belong to the Contacts table. Contact.Id belongs to the Data table.
    // RawContacts.Id belong to the RawContacts table. RawContact.Id belongs to the Data table.
    // So we just add the Contacts.Id and RawContacts.Id since they are required anyways.
    RawContactsFields.fields
        .intersect(fields)
        .asSequence()
        .plus(ContactsFields.Id)
        .plus(RawContactsFields.Id)
)