package contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.Field
import contacts.entities.CommonDataEntity
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A wrapper around a [Cursor] using fields of type [T]. This assumes that the cursor may change
 * positions at any time. Therefore, values returned should be dynamic.
 *
 * This provides type restrictions with specific fields and convenience functions and delegates.
 *
 * Used for extracting entity data from the [cursor] to construct entity instances.
 *
 * Subclasses may extract entity data from the private cursor in several ways;
 *
 * 1. By delegate functions.
 *
 *    ```
 *    val formattedAddress: String? by string(Fields.Address.FormattedAddress)
 *
 *    val type: Address.Type? by type(Fields.Address.Type, typeFromValue = Address.Type::fromValue)
 *    ```
 *
 * 2. By regular functions.
 *
 *    ```
 *    val formattedAddress: String?
 *        get() = getString(Fields.Address.FormattedAddress)
 *
 *    val type: Address.Type?
 *        get() = getType(Fields.Address.Type, typeFromValue = Address.Type::fromValue)
 *    ```
 *
 *    or
 *
 *    ```
 *    fun getFormattedAddress(): String? = getString(Fields.Address.FormattedAddress)
 *
 *    fun getType(): Address.Type? = getType(
 *        Fields.Address.Type,
 *        typeFromValue = Address.Type::fromValue
 *    )
 *    ```
 *
 *    For Java users, the only way would be to use the regular functions.
 *
 *    ```
 *    @Nullable
 *    String getFormattedAddress() {
 *        return getString(Fields.Address.FormattedAddress);
 *    }
 *
 *    @Nullable
 *    Address.Type getType() {
 *        return getType(Fields.Address.Type, Address.Type::fromValue);
 *    }
 *    ```
 *
 * **Delegated properties versus regular functions**
 *
 * Delegated properties use kotlin reflect, which may negatively affect performance. I have not
 * benchmarked the performance difference between delegated properties and regular functions. It
 * is probably trivial since these cursors are not used to process big data (millions of data).
 * Performance may be affected for some users that have thousands of contacts? Or maybe the
 * difference is negligible until we get to really big numbers like in the millions?
 *
 * Food for thought! One thing is for sure, delegated properties are prettier =)
 *
 * ## IMPORTANT!
 *
 * Cursor positions are dynamic! They may change at any time and may point at different data. This
 * means that properties or functions should be able to return data dynamically.
 *
 * Do not do this;
 *
 * ```
 * val formattedAddress: String? = getString(Fields.Address.FormattedAddress)
 * ```
 *
 * The value assigned to `formattedAddress` will most likely be null. Either way, the return value
 * of `formattedAddress` will always be the same. Bad!
 */
abstract class AbstractCursor<T : Field>(private val cursor: Cursor) {

    // region REGULAR FUNCTIONS - NULLABLE

    @JvmOverloads
    protected fun getString(field: T, default: String? = null): String? {
        val index = cursor.getColumnIndex(field.columnName)
        return if (index == -1) default else try {
            cursor.getString(index)
        } catch (e: Exception) {
            default
        }
    }

    @JvmOverloads
    protected fun getInt(field: T, default: Int? = null): Int? =
        getString(field)?.toIntOrNull() ?: default

    @JvmOverloads
    protected fun getLong(field: T, default: Long? = null): Long? =
        getString(field)?.toLongOrNull() ?: default

    @JvmOverloads
    protected fun getBoolean(field: T, default: Boolean? = null): Boolean? =
        getInt(field)?.let { it == 1 } ?: default

    @JvmOverloads
    protected fun getBlob(field: T, default: ByteArray? = null): ByteArray? {
        val index = cursor.getColumnIndex(field.columnName)
        return if (index == -1) default else try {
            // Should probably not use getString for getting a byte array.
            // Worst case the byte array would be null or empty
            cursor.getBlob(index)
        } catch (e: Exception) {
            default
        }
    }

    @JvmOverloads
    protected fun getUri(field: T, default: Uri? = null): Uri? {
        val uriStr = getString(field)
        return if (uriStr != null) Uri.parse(uriStr) else default
    }

    @JvmOverloads
    protected fun getDate(field: T, default: Date? = null): Date? {
        val dateMillis = getLong(field)
        return if (dateMillis != null && dateMillis > 0) Date(dateMillis) else default
    }

    @JvmOverloads
    protected fun <K : CommonDataEntity.Type> getType(
        field: T,
        default: K? = null,
        typeFromValue: (value: Int?) -> K?
    ): K? = typeFromValue(getInt(field)) ?: default

    // endregion

    // region REGULAR FUNCTIONS - NON-NULL

    @JvmOverloads
    protected fun getNonNullString(field: T, default: String = ""): String =
        getString(field) ?: default

    @JvmOverloads
    protected fun getNonNullInt(field: T, default: Int = 0): Int = getInt(field) ?: default

    @JvmOverloads
    protected fun getNonNullLong(field: T, default: Long = 0): Long = getLong(field) ?: default

    @JvmOverloads
    protected fun getNonNullBoolean(field: T, default: Boolean = false): Boolean =
        getBoolean(field) ?: default

    @JvmOverloads
    protected fun getNonNullBlob(field: T, default: ByteArray = ByteArray(0)): ByteArray =
        getBlob(field) ?: default

    @JvmOverloads
    protected fun getNonNullUri(field: T, default: Uri = Uri.EMPTY): Uri = getUri(field) ?: default

    @JvmOverloads
    protected fun getNonNullDate(field: T, default: Date = Date()): Date = getDate(field) ?: default

    // No getNonNullType because that would require us to introduce an UNKNOWN type.

    // endregion

    // region DELEGATE BY FIELD EXTENSION

    /*
    Commenting this out because of naming conflicts with the regular functions for Java users.
    E.G. val T.string conflicts with fun getInt(field: T): Int?
    A solution would be to change the names.
    E.G. T.string -> T.stringDelegate or fun getString() -> fun getStringFrom()
    But it would just make the code unnecessarily messy. Besides, this isn't very kotliny.
    protected val T.string get() = string(this)

    protected val T.int get() = int(this)

    protected val T.long get() = long(this)

    protected val T.boolean get() = boolean(this)

    protected val T.blob get() = blob(this)

    protected val T.uri get() = uri(this)

    protected val T.date get() = date(this)

    // field extension for type is not possible because it has two arguments.
     */

    // endregion

    // region DELEGATE BY FUNCTION - NULLABLE

    protected fun string(
        field: T,
        default: String? = null
    ): ReadOnlyProperty<AbstractCursor<T>, String?> = StringDelegate(field, default)

    protected fun int(field: T, default: Int? = null): ReadOnlyProperty<AbstractCursor<T>, Int?> =
        IntDelegate(field, default)

    protected fun long(
        field: T,
        default: Long? = null
    ): ReadOnlyProperty<AbstractCursor<T>, Long?> = LongDelegate(field, default)

    protected fun boolean(
        field: T,
        default: Boolean? = null
    ): ReadOnlyProperty<AbstractCursor<T>, Boolean?> = BooleanDelegate(field, default)

    protected fun blob(
        field: T,
        default: ByteArray? = null
    ): ReadOnlyProperty<AbstractCursor<T>, ByteArray?> = BlobDelegate(field, default)

    protected fun uri(field: T, default: Uri? = null): ReadOnlyProperty<AbstractCursor<T>, Uri?> =
        UriDelegate(field, default)

    protected fun date(
        field: T,
        default: Date? = null
    ): ReadOnlyProperty<AbstractCursor<T>, Date?> = DateDelegate(field, default)

    protected fun <K : CommonDataEntity.Type> type(
        field: T,
        default: K? = null,
        typeFromValue: (value: Int?) -> K?
    ): ReadOnlyProperty<AbstractCursor<T>, K?> = TypeDelegate(field, default, typeFromValue)

    // endregion

    // region DELEGATE BY FUNCTION - NON-NULL

    protected fun nonNullString(
        field: T,
        default: String = ""
    ): ReadOnlyProperty<AbstractCursor<T>, String> =
        NonNullStringDelegate(field, default)

    protected fun nonNullInt(field: T, default: Int = 0): ReadOnlyProperty<AbstractCursor<T>, Int> =
        NonNullIntDelegate(field, default)

    protected fun nonNullLong(
        field: T,
        default: Long = 0
    ): ReadOnlyProperty<AbstractCursor<T>, Long> = NonNullLongDelegate(field, default)

    protected fun nonNullBoolean(
        field: T,
        default: Boolean = false
    ): ReadOnlyProperty<AbstractCursor<T>, Boolean> =
        NonNullBooleanDelegate(field, default)

    protected fun nonNullBlob(
        field: T,
        default: ByteArray = ByteArray(0)
    ): ReadOnlyProperty<AbstractCursor<T>, ByteArray> =
        NonNullBlobDelegate(field, default)

    protected fun nonNullUri(
        field: T,
        default: Uri = Uri.EMPTY
    ): ReadOnlyProperty<AbstractCursor<T>, Uri> =
        NonNullUriDelegate(field, default)

    protected fun nonNullDate(
        field: T,
        default: Date = Date()
    ): ReadOnlyProperty<AbstractCursor<T>, Date> =
        NonNullDateDelegate(field, default)

    // No nonNullType because that would require us to introduce an UNKNOWN type.

    // endregion

    // region DELEGATE CLASSES - NULLABLE

    private inner class StringDelegate(
        private val field: T,
        private val default: String? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, String?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): String? =
            getString(field, default)
    }

    private inner class IntDelegate(
        private val field: T,
        private val default: Int? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, Int?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Int? =
            getInt(field, default)
    }

    private inner class LongDelegate(
        private val field: T,
        private val default: Long? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, Long?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Long? =
            getLong(field, default)
    }

    private inner class BooleanDelegate(
        private val field: T,
        private val default: Boolean? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, Boolean?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Boolean? =
            getBoolean(field, default)
    }

    private inner class BlobDelegate(
        private val field: T,
        private val default: ByteArray? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, ByteArray?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): ByteArray? =
            getBlob(field, default)
    }

    private inner class UriDelegate(
        private val field: T,
        private val default: Uri? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, Uri?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Uri? =
            getUri(field, default)
    }

    private inner class DateDelegate(
        private val field: T,
        private val default: Date? = null
    ) : ReadOnlyProperty<AbstractCursor<T>, Date?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Date? =
            getDate(field, default)
    }

    private inner class TypeDelegate<out K : CommonDataEntity.Type>(
        private val field: T,
        private val default: K? = null,
        private val typeFromValue: (value: Int?) -> K?
    ) : ReadOnlyProperty<AbstractCursor<T>, K?> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): K? =
            getType(field, default, typeFromValue)
    }

    // endregion

    // region DELEGATE CLASSES - NON-NULL

    private inner class NonNullStringDelegate(
        private val field: T,
        private val default: String = ""
    ) : ReadOnlyProperty<AbstractCursor<T>, String> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): String =
            getNonNullString(field, default)
    }

    private inner class NonNullIntDelegate(
        private val field: T,
        private val default: Int = 0
    ) : ReadOnlyProperty<AbstractCursor<T>, Int> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Int =
            getNonNullInt(field, default)
    }

    private inner class NonNullLongDelegate(
        private val field: T,
        private val default: Long = 0
    ) : ReadOnlyProperty<AbstractCursor<T>, Long> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Long =
            getNonNullLong(field, default)
    }

    private inner class NonNullBooleanDelegate(
        private val field: T,
        private val default: Boolean = false
    ) : ReadOnlyProperty<AbstractCursor<T>, Boolean> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Boolean =
            getNonNullBoolean(field, default)
    }

    private inner class NonNullBlobDelegate(
        private val field: T,
        private val default: ByteArray = ByteArray(0)
    ) : ReadOnlyProperty<AbstractCursor<T>, ByteArray> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): ByteArray =
            getNonNullBlob(field, default)
    }

    private inner class NonNullUriDelegate(
        private val field: T,
        private val default: Uri = Uri.EMPTY
    ) : ReadOnlyProperty<AbstractCursor<T>, Uri> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Uri =
            getNonNullUri(field, default)
    }

    private inner class NonNullDateDelegate(
        private val field: T,
        private val default: Date = Date()
    ) : ReadOnlyProperty<AbstractCursor<T>, Date> {
        override fun getValue(thisRef: AbstractCursor<T>, property: KProperty<*>): Date =
            getNonNullDate(field, default)
    }

    // No NonNullTypeDelegate because that would require us to introduce an UNKNOWN type.

    // endregion
}