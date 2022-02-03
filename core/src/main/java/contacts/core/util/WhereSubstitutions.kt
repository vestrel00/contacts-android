package contacts.core.util

import android.content.ContentResolver
import android.util.Log
import contacts.core.*

/**
 * Converts [this] Data where clause to a where clause that is usable for the Contacts table.
 *
 * More specifically, this translates the following column names to work with the Contacts table;
 *
 * - RawContacts.CONTACT_ID -> Contacts._ID
 * - Data.CONTACT_ID -> Contacts._ID
 *
 * This does no translate anything else. So any fields used that does not exist in the Contacts
 * table will remain.
 */
internal fun Where<AbstractDataField>.inContactsTable(): Where<ContactsField> =
    copyWithFieldValueSubstitutions(
        substituteField = { fieldHolder ->
            when (fieldHolder.field) {
                // Technically, RawContactsFields.ContactId and Fields.Contact.Id have the same columnName.
                // For the sake of OCD and just-in-case, I'm performing this redundant replacement. SUE ME!
                RawContactsFields.ContactId, Fields.Contact.Id -> FieldHolder(ContactsFields.Id)
                else -> fieldHolder // no substitution
            }
        }
    )

/**
 * Converts [this] Data where clause to a where clause that is usable for the RawContacts table.
 *
 * More specifically, this translates the following column names to work with the RawContacts table;
 *
 * - Data.RAW_CONTACT_ID -> RawContacts._ID
 *
 * This does no translate anything else. So any fields used that does not exist in the RawContacts
 * table will remain.
 */
internal fun Where<AbstractDataField>.inRawContactsTable(): Where<RawContactsField> =
    copyWithFieldValueSubstitutions(
        substituteField = { fieldHolder ->
            when (fieldHolder.field) {
                Fields.RawContact.Id -> FieldHolder(RawContactsFields.Id)
                else -> fieldHolder // no substitution
            }
        }
    )

/**
 * Returns a copy of this [Where] such that LHS fields are substituted with the output of
 * [substituteField] and RHS values are substituted with the output of [substituteValue].
 *
 * This also allows for mutating the type [T] to any type [R].
 */
internal fun <T : Field, R : Field> Where<T>.copyWithFieldValueSubstitutions(
    /**
     * Return a substitute for the given field.
     */
    substituteField: (FieldHolder) -> FieldHolder = { it },
    /**
     * Return a substitute for the given value.
     */
    substituteValue: (ValueHolder) -> ValueHolder = { it }
): Where<R> =
    /*
     * Okay. Time for some "recursion" hehehe =). You know, I can't believe this interview
     * skill is actually coming in handy... for once LOL! Ohh I'm so excited to have encountered
     * this problem in the REAL LIFE! Ohh, I'm so hyped! Anyways, this is probably an easy level
     * question in leet code. Standard tree traversal. It's essentially "find leaf nodes of a
     * binary tree"...
     *
     * Given the root node (this) of a binary tree, use the substituteField and substituteValue
     * functions to replace the lhs and rhs of the leaf nodes. See the class documentation for
     * the binary tree structure.
     *
     * Without further ado, here is the code!
     */
    if (lhs is FieldHolder && operator is Operator.Match && rhs is ValueHolder) {
        // Base case. Perform the substitution.
        Where(
            lhs = substituteField(lhs),
            operator = operator,
            rhs = substituteValue(rhs),
            options = options,
            isRedacted = isRedacted
        )
    } else if (lhs is WhereHolder && operator is Operator.Combine && rhs is WhereHolder) {
        // Recursive case. Traverse tree.
        Where(
            lhs = WhereHolder(
                lhs.where.copyWithFieldValueSubstitutions(
                    substituteField,
                    substituteValue
                )
            ),
            operator = operator,
            rhs = WhereHolder(
                rhs.where.copyWithFieldValueSubstitutions(
                    substituteField,
                    substituteValue
                )
            ),
            options = options,
            isRedacted = isRedacted
        )
    } else {
        // This should not happen with the current structure. If this does happen, it means
        // that we made some changes that broke the structure.
        throw ContactsException(
            "Unhandled Where form lhs: ${lhs.javaClass.simpleName}, rhs: ${rhs.javaClass.simpleName}"
        )
    }

/**
 * Returns a "reduced" copy of the given [where] clause to ensure that it is able match rows in the
 * Data table.
 *
 * The following will always match 0 rows in the Data table,
 *
 * ```kotlin
 * Email.Address.isNotNull() and Phone.Number.isNotNull()
 * ```
 *
 * because it is impossible to have a row in the Data table that represents both an email and a
 * phone number! Each row in the data table can only have one mimetype!
 *
 * > Note that it does not matter what operator is used (it does not have to be "isNotNull").
 *
 * Therefore, we need to perform a Data table query for each statement in the where clause that;
 *
 * - are combined using "AND"
 * - when the mime types are different
 *
 * Example #1,
 *
 * where  -> Email and Phone
 * reduce -> ContactIDs and Phone
 *
 * Example #2,
 *
 * where  -> (((Email and Phone) and Organization) and Website)
 * reduce  -> (((ContactIDs and Phone) and Organization) and Website)
 * reduce  -> ((ContactIDs and Organization) and Website)
 * reduce  -> (ContactIDs and Website)
 *
 * Example #3,
 *
 * where  -> (Email and Phone) and (Organization and Website)
 * reduce -> (ContactIDs and Phone) and (Organization and Website)
 * reduce -> ContactIDs and (Organization and Website)
 * reduce -> ContactIDs and (ContactIDs and Website)
 *
 * Example #4,
 *
 * where  -> (Email or Phone) and (Organization or Website)
 * reduce -> (ContactIDs) and (Organization or Website)
 * reduce -> (ContactIDs) and (ContactIDs)
 * optimize -> ContactIDs intersect ContactIDs
 *
 * The idea is to reduce the amount of mimetype-aware matches such that there is 0 mimetype-aware
 * match in one side of the AND.
 *
 * More context in https://github.com/vestrel00/contacts-android/issues/142#issuecomment-1000948529
 */
internal fun ContentResolver.reduce(
    where: Where<AbstractDataField>,
    cancel: () -> Boolean
): Where<AbstractDataField> = if (cancel()) {
    where
} else {

    Log.d("YOLO", "WHERE: $where")

    where.copyWithSubstitutions { lhs, operator, rhs, options, isRedacted ->
        Log.d(
            "YOLO",
            """
                substitute...
                lhs: $lhs
                operator: $operator
                rhs: $rhs
            """.trimIndent()
        )

        // The following code block assumes that the where binary tree is being traversed in
        // post-order, making substitutions to reduce the number of mimeTypes and keep the
        // aggregate mimetype count of lhs and rhs to at most one.

        if (operator == Operator.Combine.And && TODO()) {
            // Operator is AND plus TODO...
            Where(
                WhereHolder(Fields.Contact.Id `in` findContactIdsInDataTable(lhs, cancel)),
                operator,
                WhereHolder(rhs),
                options,
                isRedacted
            )

        } else {
            // No need to reduce. Just return this.
            // E.G. Email OR Phone, ContactIDs and Phone, ContactIDs, Email AND Email
            Where(WhereHolder(lhs), operator, WhereHolder(rhs), options, isRedacted)
        }
    }.apply {
        Log.d("YOLO", "REDUCED: $this")
    }
}

/**
 * Performs a post-order traversal (lhs, rhs, this) of the Where binary tree structure and returns
 * a copy of this by making substitutions along the way using [substitute].
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Field> Where<T>.copyWithSubstitutions(
    /**
     * A function that takes in a where clause of the form "where combine where" and returns a
     * substitute where.
     */
    substitute: (
        lhs: Where<T>,
        operator: Operator.Combine,
        rhs: Where<T>,
        options: String?,
        isRedacted: Boolean
    ) -> Where<T>
): Where<T> = if (lhs is FieldHolder && operator is Operator.Match && rhs is ValueHolder) {
    // Base case. No substitutions at this level. Just return this as is.
    this
} else if (lhs is WhereHolder && operator is Operator.Combine && rhs is WhereHolder) {
    // Recursive case. Traverse tree (post-order).
    val lhsSub = (lhs.where as Where<T>).copyWithSubstitutions(substitute)
    val rhsSub = (rhs.where as Where<T>).copyWithSubstitutions(substitute)

    // Pass in this Where's evaluated components instead of the entire where to avoid requiring
    // callers to perform the casting.
    substitute(
        lhsSub,
        operator,
        rhsSub,
        options,
        isRedacted
    )
} else {
    // This should not happen with the current structure. If this does happen, it means
    // that we made some changes that broke the structure.
    throw ContactsException(
        "Unhandled Where form lhs: ${lhs.javaClass.simpleName}, rhs: ${rhs.javaClass.simpleName}"
    )
}