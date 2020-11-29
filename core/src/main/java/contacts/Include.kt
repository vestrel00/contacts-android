package contacts

@Suppress("FunctionName")
internal fun <T : Field> Include(vararg fields: T) = Include(fields.asSequence())

@Suppress("FunctionName")
internal fun <T : Field> Include(fields: Collection<T>) = Include(fields.asSequence())

@Suppress("FunctionName")
internal fun <T : Field> Include(fields: Sequence<T>): Include<T> = Include(
    // The call toSet is important because it gets rid of duplicates. We can also call
    // distinct() but then we can't call toTypedArray after it. And no, this is not more
    // expensive than calling distinct, it is actually cheaper. Distinct uses a HashSet
    // internally in addition to extra computations.
    fields.map { it.columnName }.toSet()
)

@Suppress("FunctionName")
internal fun <T : Field> Include(fieldSet: FieldSet<T>) = Include(fieldSet.all)

/**
 * Contains a set of column names to include.
 *
 * ## Developer notes
 *
 * The type [T] is not exactly used in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time.
 */
internal class Include<out T : Field>(val columnNames: Set<String>) {

    override fun toString(): String = columnNames.joinToString(", ")
}

/**
 * Returns a new instance of [Include] where only [ContactsFields] in [this] are included.
 *
 * This is used to convert an [Include] of [DataFields] to [ContactsFields].
 */
internal fun Include<AbstractDataField>.onlyContactsFields() = Include<ContactsField>(
    Include(ContactsFields.all).columnNames
        .intersect(columnNames)
        .asSequence()
        // DataContactsFields.Id has a different columnName than ContactsFields.Id.
        .plus(ContactsFields.Id.columnName)
        .toSet()
)

/**
 * Returns a new instance of [Include] where only [RawContactsFields] in [this] are included.
 *
 * This is used to convert an [Include] of [DataFields] to [RawContactsFields].
 */
internal fun Include<AbstractDataField>.onlyRawContactsFields() = Include<RawContactsField>(
    Include(RawContactsFields.all).columnNames
        .intersect(columnNames)
        .asSequence()
        // DataRawContactsFields.Id has a different columnName than RawContactsFields.Id.
        .plus(RawContactsFields.Id.columnName)
        // DataContactsFields.Id has a different columnName than RawContactsFields.ContactId.
        .plus(RawContactsFields.ContactId.columnName)
        .toSet()
)