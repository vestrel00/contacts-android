package contacts.core

import android.database.DatabaseUtils
import contacts.core.entities.DataEntity
import contacts.core.entities.EventDate
import contacts.core.entities.MimeType
import contacts.core.entities.toWhereString
import contacts.core.util.copyWithFieldValueSubstitutions
import contacts.core.util.unsafeLazy
import java.util.*

// Java consumers would have to access these static functions via Wherekt instead of Where.
// Using @file:JvmName("Where") will not work because of the name clash with the Where class.
// In order for Java consumers to use these via OrderBy instead of Wherekt, we could redefine
// these functions in a companion object within the Where class. However, we won't do this just
// because it creates duplicate code. Java users just need to migrate to Kotlin already...

// TODO? Verify that LIKE or NOT LIKE operations are case-sensitive when outside of ASCII range.
// From my limited testing, it seems like it is case-insensitive even when outside of ASCII range.
// Perhaps, I was not using non-ascii characters when I was testing it out. For now, I'll just
// mention that "String comparison is case-insensitive when within ASCII range". I'll wait until the
// community sees an issue and raises it.

// region Operators

/**
 * String comparison is case-sensitive.
 */
infix fun <T : Field> T.equalTo(value: Any): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.Equal,
    rhs = ValueHolder(value)
)

/**
 * String comparison is case-sensitive.
 */
infix fun <T : Field> T.notEqualTo(value: Any): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.NotEqual,
    rhs = ValueHolder(value)
)

infix fun <T : Field> T.greaterThan(value: Any): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.GreaterThan,
    rhs = ValueHolder(value)
)

infix fun <T : Field> T.greaterThanOrEqual(value: Any): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.GreaterThanOrEqual,
    rhs = ValueHolder(value)
)

infix fun <T : Field> T.lessThan(value: Any): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.LessThan,
    rhs = ValueHolder(value)
)

infix fun <T : Field> T.lessThanOrEqual(value: Any): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.LessThanOrEqual,
    rhs = ValueHolder(value)
)

infix fun <T : Field> T.`in`(values: Collection<Any>): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.In,
    rhs = ValueHolder(values)
)

infix fun <T : Field> T.`in`(values: Sequence<Any>): Where<T> = `in`(values.toList())
infix fun <T : Field> T.notIn(values: Collection<Any>): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.NotIn,
    rhs = ValueHolder(values)
)

infix fun <T : Field> T.notIn(values: Sequence<Any>): Where<T> = notIn(values.toList())

/**
 * Same as `LIKE '[value]'`.
 *
 * See [like] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.equalToIgnoreCase(value: String): Where<T> =
    like(value, VALUE_PLACEHOLDER)

/**
 * Same as `LIKE '[value]%'`.
 *
 * See [like] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.startsWith(value: String): Where<T> =
    like(value, "${VALUE_PLACEHOLDER}%")

/**
 * Same as `LIKE '%[value]'`.
 *
 * See [like] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.endsWith(value: String): Where<T> =
    like(value, "%${VALUE_PLACEHOLDER}")

/**
 * Same as `LIKE '%[value]%'`.
 *
 * See [like] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.contains(value: String): Where<T> =
    like(value, "%${VALUE_PLACEHOLDER}%")

/**
 * Same as `NOT LIKE '[value]'`.
 *
 * See [notLike] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.notEqualToIgnoreCase(value: String): Where<T> =
    notLike(value, VALUE_PLACEHOLDER)

/**
 * Same as `NOT LIKE '[value]%'`.
 *
 * See [notLike] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.doesNotStartWith(value: String): Where<T> =
    notLike(value, "${VALUE_PLACEHOLDER}%")

/**
 * Same as `NOT LIKE '%[value]'`.
 *
 * See [notLike] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.doesNotEndWith(value: String): Where<T> =
    notLike(value, "%${VALUE_PLACEHOLDER}")

/**
 * Same as `NOT LIKE '%[value]%'`.
 *
 * See [notLike] for more info.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.doesNotContain(value: String): Where<T> =
    notLike(value, "%${VALUE_PLACEHOLDER}%")

/**
 * A [Where] of the form of `Field LIKE pattern`.
 *
 * The pattern is resolved from the [value], [decorator], and [placeholder]. For example, in order
 * to produce the pattern "%gmail%",
 *
 * - [value]: "gmail"
 * - [decorator]: "%placeholder%"
 * - [placeholder]: "placeholder"
 *
 * If the [value] contains % or _ that should be escaped, provide the [escapeExpression] to use
 * escape the [value]. For example, [contains] uses the pattern "%[value]%". That alone has a flaw
 * that it does not escape the % and _ inside the value. This means that the pattern
 * "%LOVE_IS_%BLIND%" can be matched with raw values such as "loverisnotblind". This flaw allows
 * user input [value] to use wildcards. That is why [contains] adds the [escapeExpression]
 * (typically "\") to the value first. Then it calls this function with the pattern
 * "%$escapedValue%" and [escapeExpression] "\". Correcting the previous user input [value] to
 * "%LOVE\_IS\_\%BLIND%".
 *
 * **Warning!** This is one of the more (relatively) advanced, free-form operators provided by this
 * library. Use at your own expertise and knowledge of how it works. If you are not familiar with
 * SQLite LIKE operator, read entire documentation below. Better yet, visit this website where the
 * below documentation was copied from; https://www.sqlitetutorial.net/sqlite-like/
 *
 * String comparison is case-insensitive when within ASCII range.
 *
 * ## The LIKE operator explained
 *
 * SQLite provides two wildcards for constructing patterns; percent sign % and underscore _;
 *
 * - The percent sign % wildcard matches any sequence of zero or more characters.
 * - The underscore _ wildcard matches any single character.
 *
 * ## The percent sign % wildcard examples
 *
 * The s% pattern that uses the percent sign wildcard (%) matches any string that starts with s
 * e.g.,son and so.
 *
 * The %er pattern matches any string that ends with er like peter, clever, etc.
 *
 * And the %per% pattern matches any string that contains per such as percent and peeper.
 *
 * #### The underscore _ wildcard examples
 *
 * The h_nt pattern matches hunt, hint, etc. The __pple pattern matches topple, supple, tipple, etc.
 *
 * #### ESCAPE clause
 *
 * If the pattern that you want to match contains % or _, you must use an escape character in an
 * optional ESCAPE clause as follows
 *
 * When you specify the ESCAPE clause, the LIKE operator will evaluate the expression that follows
 * the ESCAPE keyword to a string which consists of a single character, or an escape character.
 *
 * Then you can use this escape character in the pattern to include literal percent sign (%) or
 * underscore (_). The LIKE operator evaluates the percent sign (%) or underscore (_) that follows
 * the escape character as a literal string, not a wildcard character.
 *
 * Suppose you want to match the string 10% in a column of a table. However, SQLite interprets the
 * percent symbol % as the wildcard character. Therefore,  you need to escape this percent symbol %
 * using an escape character:
 *
 * `column_1 LIKE '%10\%%' ESCAPE '\'`
 *
 * In this expression, the LIKE operator interprets the first % and last % percent signs as
 * wildcards and the second percent sign as a literal percent symbol.
 *
 * Note that you can use other characters as the escape character e.g., /, @, $.
 *
 * ## Note of Attribution
 *
 * The above explanation of the % and _ wildcards are copied from
 * https://www.sqlitetutorial.net/sqlite-like/. I do not take credit for it. All credit goes to
 * that website. I just wanted the simplest documentation for this and I found that that is the
 * simplest/best. Credits to them. Don't sue me, please. If this is an issue, I'll change the
 * documentation above. AM I BEING PARANOID HERE?!?!
 */
@JvmOverloads
fun <T : Field> T.like(
    value: String,
    decorator: String,
    placeholder: String = VALUE_PLACEHOLDER,
    escapeExpression: String? = LIKE_ESCAPE_EXPR
): Where<T> = wherePattern(
    Operator.Match.Pattern.Like,
    value, decorator, placeholder, escapeExpression
)

/**
 * Same as [like] but preceded with a NOT.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
@JvmOverloads
fun <T : Field> T.notLike(
    value: String,
    decorator: String,
    placeholder: String = VALUE_PLACEHOLDER,
    escapeExpression: String? = LIKE_ESCAPE_EXPR
): Where<T> = wherePattern(
    Operator.Match.Pattern.NotLike,
    value, decorator, placeholder, escapeExpression
)

private fun <T : Field> T.wherePattern(
    operator: Operator.Match.Pattern,
    value: String,
    decorator: String,
    placeholder: String,
    escapeExpression: String?
): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = operator,
    rhs = ValueHolder(
        escapeExpression?.let(value::likeWildcardsEscaped) ?: value,
        decorator,
        placeholder
    ),
    options = escapeExpression?.let { "ESCAPE '$escapeExpression'" }
)

/**
 * ANDs [this] and [where]. If [where] is null, returns [this].
 */
infix fun <T : Field> Where<T>.and(where: Where<T>?): Where<T> = if (where != null) {
    Where(
        lhs = WhereHolder(this),
        operator = Operator.Combine.And,
        rhs = WhereHolder(where)
    )
} else {
    this
}

/**
 * ORs [this] and [where]. If [where] is null, returns [this].
 */
infix fun <T : Field> Where<T>.or(where: Where<T>?): Where<T> = if (where != null) {
    Where(
        lhs = WhereHolder(this),
        operator = Operator.Combine.Or,
        rhs = WhereHolder(where)
    )
} else {
    this
}

// region Non-infix convenience functions

/**
 * A shorthand extension function to ease the construction of [Where] instances with multiple
 * operators on a single [Field].
 */
inline operator fun <F : Field> F.invoke(where: F.() -> Where<F>): Where<F> = where(this)

/**
 * A shorthand extension function to ease the construction of [Where] instances with (multiple)
 * operators on multiple fields of a [FieldSet].
 */
inline operator fun <T : Field, FS : FieldSet<T>> FS.invoke(where: FS.() -> Where<T>): Where<T> =
    where(this)

/**
 * ## Developer notes
 *
 * Note that functions for "isNull" or "isNullOrEmpty" are not exposed to consumers to prevent
 * making misleading queries.
 *
 * Removing a piece of existing data results in the deletion of the row in the Data table if that
 * row no longer contains any meaningful data (no meaningful non-null "datax" columns left). This is
 * the behavior of the native Contacts app. Therefore, querying for null fields is not possible.
 * For example, there may be no Data rows that exist where the email address is null. Thus, a query
 * to search for all contacts with no email addresses may return 0 contacts even if there are some
 * contacts that do not have at least one email address.
 *
 * If you want to match contacts that has no particular type of data, you will have to make two
 * queries. One to get contacts that have that particular type of data and another to get contacts
 * that were not part of the first query results.
 */
fun <T : Field> T.isNotNull(): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.IsNot,
    rhs = ValueHolder(null),
)

/**
 * ## Developer notes
 *
 * Note that functions for "isNull" or "isNullOrEmpty" are not exposed to consumers to prevent
 * making misleading queries.
 *
 * Removing a piece of existing data results in the deletion of the row in the Data table if that
 * row no longer contains any meaningful data (no meaningful non-null "datax" columns left). This is
 * the behavior of the native Contacts app. Therefore, querying for null fields is not possible.
 * For example, there may be no Data rows that exist where the email address is null. Thus, a query
 * to search for all contacts with no email addresses may return 0 contacts even if there are some
 * contacts that do not have at least one email address.
 *
 * If you want to match contacts that has no particular type of data, you will have to make two
 * queries. One to get contacts that have that particular type of data and another to get contacts
 * that were not part of the first query results.
 */
fun <T : Field> T.isNotNullOrEmpty(): Where<T> = isNotNull() and notEqualTo("")

/**
 * Keep this function internal. Do not expose to consumers. Read the docs on [isNotNull] or
 * [isNotNullOrEmpty].
 *
 * Do not use this for Data table queries.
 */
internal fun <T : Field> T.isNull(): Where<T> = Where(
    lhs = FieldHolder(this),
    operator = Operator.Match.Is,
    rhs = ValueHolder(null),
)

// endregion

// endregion

// region Collections

/**
 * Transforms each item in this collection to a [Where] and combines them with the "OR" operator.
 *
 * For example;
 *
 * ```
 * val letters = listOf("a", "b", "c")
 * val whereStartsWithLetter = letters whereOr { Fields.Contact.DisplayNamePrimary startsWith it }
 * ```
 *
 * Outputs
 *
 * ```
 * // (display_name LIKE 'a%') OR (display_name LIKE 'b%') OR (display_name LIKE 'c%')
 * ```
 *
 * Another, more useful example is a where starting with a number;
 *
 * ```
 * val whereStartsWithNumber = (0..9).asSequence()
 *      .whereOr { Fields.Contact.DisplayNamePrimary startsWith "$it" }
 * ```
 *
 * This may also be applied to a collection of [Field]s. For example,
 *
 * ```
 * val fields = listOf(Fields.Contact.DisplayNamePrimary, Fields.Email.Address)
 * val whereFieldsStartsWithLetter = fields whereOr { it startsWith "letter" }
 * ```
 *
 * Outputs
 *
 * ```
 * // (display_name LIKE 'letter%') OR (data1 LIKE 'letter%' <omitted for brevity>)
 * ```
 */
// Not inlined because of private functions and classes.
infix fun <F : Field, V : Any?> Collection<V>.whereOr(generateWhere: (V) -> Where<F>): Where<F>? =
    asSequence().combineWhere(generateWhere, Operator.Combine.Or)

/**
 * See [whereOr].
 */
// Not inlined because of private functions and classes.
infix fun <F : Field, V : Any?> Sequence<V>.whereOr(generateWhere: (V) -> Where<F>): Where<F>? =
    combineWhere(generateWhere, Operator.Combine.Or)

/**
 * Transforms each item in this collection to a [Where] and combines them with the "AND" operator.
 *
 * For example;
 *
 * ```
 * val letters = listOf("a", "b", "c")
 * val whereDoesNotStartWithLetter =
 *      letters whereAnd { Fields.Contact.DisplayNamePrimary doesNotStartWith it }
 * ```
 *
 * Outputs
 *
 * ```
 * (display_name NOT LIKE 'a%') AND (display_name NOT LIKE 'b%') AND (display_name NOT LIKE 'c%')
 * ```
 *
 * This may also be applied to a collection of [Field]s. For example,
 *
 * ```
 * val fields = listOf(Fields.Contact.DisplayNamePrimary, Fields.Email.Address)
 * val whereFieldsDoesNotStartWithLetter = fields whereAnd { it doesNotStartWith "letter" }
 * ```
 *
 * Outputs
 *
 * ```
 * // (display_name NOT LIKE 'letter%') AND (data1 NOT LIKE 'letter%' <omitted for brevity>)
 */
// Not inlined because of private functions and classes.
infix fun <F : Field, V : Any?> Collection<V>.whereAnd(generateWhere: (V) -> Where<F>): Where<F>? =
    asSequence().combineWhere(generateWhere, Operator.Combine.And)

/**
 * See [whereAnd].
 */
// Not inlined because of private functions and classes.
infix fun <F : Field, V : Any?> Sequence<V>.whereAnd(generateWhere: (V) -> Where<F>): Where<F>? =
    combineWhere(generateWhere, Operator.Combine.And)

/**
 * See [whereOr].
 */
infix fun <T : Field> FieldSet<T>.whereOr(where: (T) -> Where<T>): Where<T>? = all.whereOr(where)

/**
 * See [whereAnd].
 */
infix fun <T : Field> FieldSet<T>.whereAnd(where: (T) -> Where<T>): Where<T>? = all.whereAnd(where)

// Note that the above functions are not inlined because it requires this private fun to be public.
private fun <F : Field, V : Any?> Sequence<V>.combineWhere(
    generateWhere: (V) -> Where<F>,
    operator: Operator.Combine
): Where<F>? {
    var combinedWhere: Where<F>? = null

    for (value in this) {
        combinedWhere = if (combinedWhere == null) {
            generateWhere(value)
        } else {
            Where(
                lhs = WhereHolder(combinedWhere),
                operator = operator,
                rhs = WhereHolder(generateWhere(value))
            )
        }
    }
    return combinedWhere
}

// endregion

// region Where

/**
 * The WHERE clause of a database query made up of a specific type of [Field] ([T]).
 *
 * ## Developer notes
 *
 * The type [T] is not exactly "used" in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time. For example, this allows us to create a
 * function that takes in a Where of GroupsField. The caller of that function can then only provide
 * a Where composed of one or more GroupsField.
 *
 * The type [T] is not enforced within the class itself in order to support mutating functions
 * such as [inRawContactsTable] and [inContactsTable]. This will allow us to construct a Where<X>
 * from a Where<Y>.
 *
 * ### Binary tree structure
 *
 * The form "lhs operator rhs" naturally forms a binary tree. A where can only be constructed in
 * two different ways (hence the private constructor and two secondary constructors).
 *
 * - Base case: (lhs=FieldHolder, rhs=ValueHolder)
 *     - This can have a parent or siblings but cannot have children. AKA a leaf node.
 *     - This can be the only node (the root node) in a tree.
 * - Recursive case: (lhs=WhereHolder, rhs=WhereHolder)
 *     - This can have a parent or siblings and MUST have children.
 *     - This cannot be the only node in a tree though it can be the root node.
 *
 * For example,
 *
 *                                 WhereHolder
 *     FieldHolder-ValueHolder                            WhereHolder
 *                                           WhereHolder               FieldHolder-ValueHolder
 *                        FieldHolder-ValueHolder    FieldHolder-ValueHolder
 *
 *  With this in mind, we can do some cool stuff like in [copyWithNewFieldType]!
 */
class Where<out T : Field> private constructor(
    internal val lhs: LeftHandSide,
    internal val operator: Operator,
    internal val rhs: RightHandSide,

    /**
     * More WHERE clause functions to add to the statement. E.G. ESCAPE.
     */
    internal val options: String?,

    override val isRedacted: Boolean = false
) : Redactable {

    /**
     * Construct a where in the form of "field match value".
     *
     * E.G. email.address contains "gmail".
     */
    internal constructor(
        lhs: FieldHolder, operator: Operator.Match, rhs: ValueHolder,
        options: String? = null, isRedacted: Boolean = false
    ) : this(
        lhs = lhs as LeftHandSide,
        operator = operator,
        rhs = rhs as RightHandSide,
        options = options,
        isRedacted = isRedacted
    )

    /**
     * Construct a where in the form of "where combine where".
     *
     * E.G. (email contains "gmail") and (name startsWith "i")
     */
    internal constructor(
        lhs: WhereHolder, operator: Operator.Combine, rhs: WhereHolder,
        options: String? = null, isRedacted: Boolean = false
    ) : this(
        lhs = lhs as LeftHandSide,
        operator = operator,
        rhs = rhs as RightHandSide,
        options = options,
        isRedacted = isRedacted
    )

    override fun redactedCopy(): Where<T> = copyWithFieldValueSubstitutions(
        substituteValue = { it.redactedCopy() }
    )

    /**
     * Returns the [MimeType]s of all of the fields (for wheres in the form of "field match value")
     * in this where tree.
     *
     * The order of traversal is preorder (this, lhs, rhs). The first mimeType in the list will be
     * the mimeType of this (if it holds a field).
     */
    internal val mimeTypes: Set<MimeType> by unsafeLazy {
        mutableSetOf<MimeType>().apply {
            if (lhs is FieldHolder && operator is Operator.Match && rhs is ValueHolder) {
                if (lhs.field is DataField) {
                    add(lhs.field.mimeType)
                }
            } else if (lhs is WhereHolder && operator is Operator.Combine && rhs is WhereHolder) {
                addAll(lhs.where.mimeTypes)
                addAll(rhs.where.mimeTypes)
            } else {
                throw InvalidWhereFormException(this@Where)
            }
        }
    }

    // Only evaluate this once to save some CPU. This assumes that property values are immutable.
    // If there are mutable property values, then this will be evaluated at the time of invocation
    // and will not mutate along with the mutable property values (e.g. a mutable list). I don't
    // think consumers expect this to mutate anyways if they happen to save a reference to it.
    private val evaluatedWhereString: String by unsafeLazy {
        var whereString = when (operator) {
            // Recursive case. Traverse lhs and rhs.
            is Operator.Combine -> "($lhs) $operator ($rhs)"
            // Base case. Evaluate the expression.
            is Operator.Match -> "$lhs $operator $rhs"
        }

        if (options != null) {
            whereString += " $options"
        }

        if (
            lhs is FieldHolder &&
            lhs.field is DataField &&
            lhs.field.mimeType.value.isNotBlank()
        ) {
            /*
             * Each where expression is paired with its mimetype because the contacts Data table
             * uses generic column names (e.g. data1, data2, etc) using the column 'mimetype' to
             * distinguish the type of data in that generic column.
             *
             * For example, querying for contacts with name LIKE 'john' AND address LIKE 'colorado';
             *
             * ```
             * WHERE (data1 = 'john' AND mimetype = 'vnd.android.cursor.item/name')
             *   AND (data1 = 'colorado' AND mimetype = 'vnd.android.cursor.item/postal-address_v2')
             * ```
             *
             * This is important because if the mimetypes are not paired with the query;
             *
             * ```
             * WHERE (data1 = 'johnson' AND data1 = 'colorado')
             * ```
             *
             * The above will never match any row because 'johnson' = 'colorado' is never true.
             */
            whereString += " AND ${Fields.MimeType.columnName} = '${lhs.field.mimeType.value}'"
        }

        whereString
    }

    override fun toString(): String = evaluatedWhereString
}

/**
 * Each element in a where statement has the structure; LHS OPERATOR RHS.
 *
 * The left hand side (LHS) can either be another where element OR it can be a field.
 */
internal sealed interface LeftHandSide

/**
 * Each element in a where statement has the structure; LHS OPERATOR RHS.
 *
 * The right hand side (RHS) can either be another where element OR it can be a value.
 */
internal sealed interface RightHandSide

/**
 * The [where] can take one of the following forms;
 *
 * - "where combine where"
 * - "field operator value"
 */
internal class WhereHolder(val where: Where<Field>) : LeftHandSide, RightHandSide {
    override fun toString(): String = where.toString()
}

internal class FieldHolder(val field: Field) : LeftHandSide {
    override fun toString(): String = field.columnName
}

internal class ValueHolder private constructor(

    /**
     * The underlying value that will be used for matching.
     */
    private val value: Any?,

    /**
     * The string representation of the [value] is injected into the [ValueDecorator.decorator]
     * string when evaluating the overall [toString] value of this holder. This is used to ensure
     * that only the actual value is redacted when it is surrounded by characters such as those used
     * for "LIKE" operators.
     *
     * If this is null, the value is used directly.
     */
    private val valueDecorator: ValueDecorator?,

    /**
     * True if the [value] should be redacted in the [toString] function. The [valueDecorator] will
     * not be redacted.
     */
    override val isRedacted: Boolean

) : RightHandSide, Redactable {

    constructor(value: Any?) : this(value, null, false)

    constructor(value: String, decorator: String, placeholder: String) : this(
        value,
        ValueDecorator(decorator, placeholder),
        false
    )

    override fun redactedCopy() = ValueHolder(
        // The value cannot be redacted at this point because the conversion to an SQL string occurs
        // at a later point. It must be redacted at the same time that it is converted for SQL.
        value,
        valueDecorator,
        true
    )

    override fun toString(): String =
        valueDecorator?.decoratedValueToSqlString(value.toString(), isRedacted)
            ?: value.toSqlString(isRedacted)

    private class ValueDecorator(
        /**
         * The string representation of the [value] is injected into this string when evaluating the
         * overall [toString] value of this holder. This is used to ensure that only the actual
         * value is redacted when it is surrounded by other strings such as those used for "LIKE"
         * operators.
         */
        private val decorator: String,

        /**
         * A substring in the [decorator] that will be replaced by the [value].
         */
        private val placeholder: String
    ) {

        /**
         * Returns the SQL string where the [value] replaces the [placeholder] in the
         * [decorator]. The [value] is redacted if [redactValue] is true.
         */
        fun decoratedValueToSqlString(value: String, redactValue: Boolean): String =
            decorator.replace(
                placeholder,
                if (redactValue) {
                    value.redactString()
                } else {
                    value
                }
            )
                // intentionally not redacting the combined string as the redaction for the
                // underlying value has already been executed.
                .toSqlString(false)
    }
}

/**
 * Each element in a where statement has the structure; LHS OPERATOR RHS.
 *
 * The operator is an SQL operator.
 */
internal sealed class Operator(private val operator: String) {

    sealed class Combine(operator: String) : Operator(operator) {
        object And : Combine("AND")
        object Or : Combine("OR")
    }

    // Alternative name is "NoneCombine". Anything that is not AND, OR.
    sealed class Match(operator: String) : Operator(operator) {
        object Equal : Match("=")
        object NotEqual : Match("!=")

        object GreaterThan : Match(">")
        object GreaterThanOrEqual : Match(">=")

        object LessThan : Match("<")
        object LessThanOrEqual : Match("<=")

        object Is : Match("IS")
        object IsNot : Match("IS NOT")

        object In : Match("IN")
        object NotIn : Match("NOT IN")

        sealed class Pattern(operator: String) : Match(operator) {
            object Like : Pattern("LIKE")
            object NotLike : Pattern("NOT LIKE")
        }
    }

    override fun toString(): String = operator
}

// endregion

// region Helpers

private const val LIKE_ESCAPE_EXPR = "\\"
private const val VALUE_PLACEHOLDER = "placeholder"

/**
 * Returns a new String that escapes the LIKE wildcards (% and _) by prepending the
 * [escapeExpression] to each instance of the wildcards in this object's string representation.
 */
@JvmOverloads
fun Any.likeWildcardsEscaped(escapeExpression: String = LIKE_ESCAPE_EXPR): String {
    /* This function is the same as the below expression, except better in performance and memory.
        toString()
            .replace("_", "${escapeExpression}_")
            .replace("%", "${escapeExpression}%")
     */
    val str = toString()
    val builder = StringBuilder()

    for (char in str) {
        builder.append(
            when (char) {
                '%', '_' -> "$escapeExpression$char"
                else -> char
            }
        )
    }

    return builder.toString()
}

/**
 * Exception thrown if the given where is not one of the following valid forms,
 *
 * - "field match value"
 * - "where combine where"
 */
internal class InvalidWhereFormException(where: Where<*>): ContactsException(
    """
        lhs: ${where.lhs.javaClass.simpleName}
        operator: ${where.operator}
        rhs: ${where.rhs.javaClass.simpleName}
    """.trimIndent()
)

private fun Any?.toSqlString(redactStringValue: Boolean): String = when (this) {
    null -> "NULL"
    is Boolean -> if (this) "1" else "0"
    is String -> DatabaseUtils.sqlEscapeString(
        if (redactStringValue) {
            this.redactString()
        } else {
            this
        }
    )
    is Array<*> -> this.asSequence().toSqlString(redactStringValue)
    is Collection<*> -> this.asSequence().toSqlString(redactStringValue)
    is Sequence<*> -> this.map { it?.toSqlString(redactStringValue) }
        .joinToString(separator = ", ", prefix = "(", postfix = ")")
    is DataEntity.Type -> value.toSqlString(redactStringValue)
    is Date -> time.toString() // we will not assume that all dates are for EventDate comparisons.
    is EventDate -> toWhereString()
    is MimeType -> value.toSqlString(redactStringValue)
    else -> this.toString().toSqlString(redactStringValue)
}

// endregion