package com.vestrel00.contacts

@Suppress("FunctionName")
internal fun Include(vararg fields: Field): Include = Include(fields.asSequence())

internal class Include(fields: Sequence<Field>) {

    val fields: Set<AbstractField> by lazy(LazyThreadSafetyMode.NONE) {
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
    Fields.Contacts.fields
        .intersect(fields)
        .asSequence()
        .plus(Fields.Contacts.Id)
)

/**
 * Returns a new instance of [Include] where only RawContacts fields in [this] are included.
 */
internal fun Include.onlyRawContactFields() = Include(
    // Contacts.Id belong to the Contacts table. Contact.Id belongs to the Data table.
    // RawContacts.Id belong to the RawContacts table. RawContact.Id belongs to the Data table.
    // So we just add the Contacts.Id and RawContacts.Id since they are required anyways.
    Fields.RawContacts.fields
        .intersect(fields)
        .asSequence()
        .plus(Fields.Contacts.Id)
        .plus(Fields.RawContacts.Id)
)