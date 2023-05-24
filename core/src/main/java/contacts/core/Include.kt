package contacts.core

import android.content.ContentResolver
import contacts.core.util.query

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
    fields.toSet()
)

@Suppress("FunctionName")
internal fun <T : Field> Include(fieldSet: FieldSet<T>) = Include(fieldSet.all)

/**
 * Contains a set of [fields] to include.
 *
 * ## Dev notes
 *
 * Note that the Android contacts Data table uses generic column names (e.g. data1, data2, ...)
 * to distinguish between the different kinds of data it represents. For example, the column
 * name of [NameFields.DisplayName] is the same as [AddressFields.FormattedAddress], which is
 * 'data1'. This means that [AddressFields.FormattedAddress] is also included when
 * [NameFields.DisplayName] is included. There is no workaround for this because the
 * [ContentResolver.query] function only takes in an array of column names.
 *
 * What we can do in order to not include data that consumers are not interested in is to use
 * [fields] to filter out data fields in the query results that are not included. This does not save
 * CPU but does save RAM. This way, consumers don't have to retain references to hundreds or
 * thousands of entities that contain data they don't use and did not ask for.
 */
internal class Include<out T : Field>(
    /**
     * The typed set of data fields that should be included in the results.
     */
    val fields: Set<T>,
) {

    /**
     * The string representations of [fields].
     *
     * Read the **Dev notes** in the class doc for more info.
     */
    val columnNames: Set<String> by lazy {
        fields.map { it.columnName }.toSet()
    }

    override fun toString(): String = columnNames.joinToString(", ")
}

internal fun Contacts.includeAllFields(): Include<AbstractDataField> =
    Include(Fields.all + customDataRegistry.allFields())

/**
 * Returns a new instance of [Include] where only [ContactsFields] in [this] are included.
 *
 * This is used to convert an [Include] of [DataFields] to [ContactsFields].
 */
internal fun Include<AbstractDataField>.onlyContactsFields(): Include<ContactsField> = Include(
    // We are unable to use the Iterable.intersect function because sets of DataFields and
    // RawContactsFields have no intersection because they cannot be equal. We could override the
    // equals and hashcode functions but it may have unwanted side effects so we won't for now.
    ContactsFields.all
        .filter { columnNames.contains(it.columnName) }
        // DataContactsFields.Id has a different columnName than ContactsFields.Id.
        .plus(ContactsFields.Id)
)

/**
 * Returns true if there is at least one field belonging to the Data table that is in this includes.
 *
 * Note that [AbstractDataField] has 4 different direct subtypes;
 *
 * - [DataRawContactsField]
 * - [DataContactsField]
 * - [GenericDataField]
 * - [DataField]
 *
 * Instances of [DataRawContactsField] and [DataContactsField] represent joined fields from the
 * RawContacts and Contacts tables respectively.
 *
 * Instances of [GenericDataField] represent fields from the Data table. However, they do not
 * represent any intrinsic data (e.g. email, phone). Furthermore, all of them are a part of
 * [RequiredDataFields] so they are always included.
 *
 * The only fields we are interested in are instances of [DataField].
 */
internal val Include<AbstractDataField>.containsAtLeastOneDataField: Boolean
    get() = fields.find { it is DataField } != null