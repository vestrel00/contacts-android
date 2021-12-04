package contacts.core.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.core.Field
import contacts.core.entities.DataEntity
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A wrapper around a [Cursor] using fields of type [F]. This assumes that the cursor may change
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
 *    val type: AddressEntity.Type? by type(
 *          Fields.Address.Type, typeFromValue = AddressEntity.Type::fromValue
 *    )
 *    ```
 *
 * 2. By regular functions.
 *
 *    ```
 *    val formattedAddress: String?
 *        get() = getString(Fields.Address.FormattedAddress)
 *
 *    val type: AddressEntity.Type?
 *        get() = getType(Fields.Address.Type, typeFromValue = AddressEntity.Type::fromValue)
 *    ```
 *
 *    or
 *
 *    ```
 *    fun getFormattedAddress(): String? = getString(Fields.Address.FormattedAddress)
 *
 *    fun getType(): AddressEntity.Type? = getType(
 *        Fields.Address.Type,
 *        typeFromValue = AddressEntity.Type::fromValue
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
 *    AddressEntity.Type getType() {
 *        return getType(Fields.Address.Type, AddressEntity.Type::fromValue);
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
 * Delegate and regular functions will return null if accessing a field that is not in
 * [includeFields] and is not required.
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
 *
 * Do this;
 *
 * ```
 * val formattedAddress: String? by string(Fields.Address.FormattedAddress)
 * ```
 */
abstract class AbstractEntityCursor<F : Field>(
    private val cursor: Cursor,
    private val includeFields: Set<F>
) {

    // region REGULAR FUNCTIONS - NULLABLE

    @JvmOverloads
    protected fun getString(field: F, default: String? = null): String? {
        if (!includeFields.contains(field) && !field.required) {
            return null
        }

        val index = cursor.getColumnIndex(field.columnName)
        return if (index == -1) default else try {
            cursor.getString(index)
        } catch (e: Exception) {
            default
        }
    }

    @JvmOverloads
    protected fun getInt(field: F, default: Int? = null): Int? =
        getString(field)?.toIntOrNull() ?: default

    @JvmOverloads
    protected fun getLong(field: F, default: Long? = null): Long? =
        getString(field)?.toLongOrNull() ?: default

    @JvmOverloads
    protected fun getBoolean(field: F, default: Boolean? = null): Boolean? =
        getInt(field)?.let { it == 1 } ?: default

    @JvmOverloads
    protected fun getBlob(field: F, default: ByteArray? = null): ByteArray? {
        if (!includeFields.contains(field)) {
            return null
        }

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
    protected fun getUri(field: F, default: Uri? = null): Uri? {
        val uriStr = getString(field)
        return if (uriStr != null) Uri.parse(uriStr) else default
    }

    @JvmOverloads
    protected fun getDate(field: F, default: Date? = null): Date? {
        val dateMillis = getLong(field)
        return if (dateMillis != null && dateMillis > 0) Date(dateMillis) else default
    }

    @JvmOverloads
    protected fun <T : DataEntity.Type> getType(
        field: F,
        default: T? = null,
        typeFromValue: (value: Int?) -> T?
    ): T? = typeFromValue(getInt(field)) ?: default

    // endregion

    // region REGULAR FUNCTIONS - NON-NULL

    @JvmOverloads
    protected fun getNonNullString(field: F, default: String = ""): String =
        getString(field) ?: default

    @JvmOverloads
    protected fun getNonNullInt(field: F, default: Int = 0): Int = getInt(field) ?: default

    @JvmOverloads
    protected fun getNonNullLong(field: F, default: Long = 0): Long = getLong(field) ?: default

    @JvmOverloads
    protected fun getNonNullBoolean(field: F, default: Boolean = false): Boolean =
        getBoolean(field) ?: default

    @JvmOverloads
    protected fun getNonNullBlob(field: F, default: ByteArray = ByteArray(0)): ByteArray =
        getBlob(field) ?: default

    @JvmOverloads
    protected fun getNonNullUri(field: F, default: Uri = Uri.EMPTY): Uri = getUri(field) ?: default

    @JvmOverloads
    protected fun getNonNullDate(field: F, default: Date = Date()): Date = getDate(field) ?: default

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
        field: F,
        default: String? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, String?> = StringDelegate(field, default)

    protected fun int(
        field: F,
        default: Int? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Int?> =
        IntDelegate(field, default)

    protected fun long(
        field: F,
        default: Long? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Long?> = LongDelegate(field, default)

    protected fun boolean(
        field: F,
        default: Boolean? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Boolean?> = BooleanDelegate(field, default)

    protected fun blob(
        field: F,
        default: ByteArray? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, ByteArray?> = BlobDelegate(field, default)

    protected fun uri(
        field: F,
        default: Uri? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Uri?> =
        UriDelegate(field, default)

    protected fun date(
        field: F,
        default: Date? = null
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Date?> = DateDelegate(field, default)

    protected fun <T : DataEntity.Type> type(
        field: F,
        default: T? = null,
        typeFromValue: (value: Int?) -> T?
    ): ReadOnlyProperty<AbstractEntityCursor<F>, T?> = TypeDelegate(field, default, typeFromValue)

    // endregion

    // region DELEGATE BY FUNCTION - NON-NULL

    protected fun nonNullString(
        field: F,
        default: String = ""
    ): ReadOnlyProperty<AbstractEntityCursor<F>, String> =
        NonNullStringDelegate(field, default)

    protected fun nonNullInt(
        field: F,
        default: Int = 0
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Int> =
        NonNullIntDelegate(field, default)

    protected fun nonNullLong(
        field: F,
        default: Long = 0
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Long> = NonNullLongDelegate(field, default)

    protected fun nonNullBoolean(
        field: F,
        default: Boolean = false
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Boolean> =
        NonNullBooleanDelegate(field, default)

    protected fun nonNullBlob(
        field: F,
        default: ByteArray = ByteArray(0)
    ): ReadOnlyProperty<AbstractEntityCursor<F>, ByteArray> =
        NonNullBlobDelegate(field, default)

    protected fun nonNullUri(
        field: F,
        default: Uri = Uri.EMPTY
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Uri> =
        NonNullUriDelegate(field, default)

    protected fun nonNullDate(
        field: F,
        default: Date = Date()
    ): ReadOnlyProperty<AbstractEntityCursor<F>, Date> =
        NonNullDateDelegate(field, default)

    // No nonNullType because that would require us to introduce an UNKNOWN type.

    // endregion

    // region DELEGATE CLASSES - NULLABLE

    private inner class StringDelegate(
        private val field: F,
        private val default: String? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, String?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): String? =
            getString(field, default)
    }

    private inner class IntDelegate(
        private val field: F,
        private val default: Int? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Int?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Int? =
            getInt(field, default)
    }

    private inner class LongDelegate(
        private val field: F,
        private val default: Long? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Long?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Long? =
            getLong(field, default)
    }

    private inner class BooleanDelegate(
        private val field: F,
        private val default: Boolean? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Boolean?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Boolean? =
            getBoolean(field, default)
    }

    private inner class BlobDelegate(
        private val field: F,
        private val default: ByteArray? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, ByteArray?> {
        override fun getValue(
            thisRef: AbstractEntityCursor<F>,
            property: KProperty<*>
        ): ByteArray? =
            getBlob(field, default)
    }

    private inner class UriDelegate(
        private val field: F,
        private val default: Uri? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Uri?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Uri? =
            getUri(field, default)
    }

    private inner class DateDelegate(
        private val field: F,
        private val default: Date? = null
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Date?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Date? =
            getDate(field, default)
    }

    private inner class TypeDelegate<out T : DataEntity.Type>(
        private val field: F,
        private val default: T? = null,
        private val typeFromValue: (value: Int?) -> T?
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, T?> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): T? =
            getType(field, default, typeFromValue)
    }

    // endregion

    // region DELEGATE CLASSES - NON-NULL

    private inner class NonNullStringDelegate(
        private val field: F,
        private val default: String = ""
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, String> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): String =
            getNonNullString(field, default)
    }

    private inner class NonNullIntDelegate(
        private val field: F,
        private val default: Int = 0
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Int> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Int =
            getNonNullInt(field, default)
    }

    private inner class NonNullLongDelegate(
        private val field: F,
        private val default: Long = 0
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Long> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Long =
            getNonNullLong(field, default)
    }

    private inner class NonNullBooleanDelegate(
        private val field: F,
        private val default: Boolean = false
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Boolean> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Boolean =
            getNonNullBoolean(field, default)
    }

    private inner class NonNullBlobDelegate(
        private val field: F,
        private val default: ByteArray = ByteArray(0)
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, ByteArray> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): ByteArray =
            getNonNullBlob(field, default)
    }

    private inner class NonNullUriDelegate(
        private val field: F,
        private val default: Uri = Uri.EMPTY
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Uri> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Uri =
            getNonNullUri(field, default)
    }

    private inner class NonNullDateDelegate(
        private val field: F,
        private val default: Date = Date()
    ) : ReadOnlyProperty<AbstractEntityCursor<F>, Date> {
        override fun getValue(thisRef: AbstractEntityCursor<F>, property: KProperty<*>): Date =
            getNonNullDate(field, default)
    }

    // No NonNullTypeDelegate because that would require us to introduce an UNKNOWN type.

    // endregion
}