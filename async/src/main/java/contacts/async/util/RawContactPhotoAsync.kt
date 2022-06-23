package contacts.async.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ExistingRawContactEntityWithContactId
import contacts.core.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

// region WITH CONTEXT

// region GET PHOTO

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoInputStream].
 */
suspend fun ExistingRawContactEntityWithContactId.photoInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoBytes].
 */
suspend fun ExistingRawContactEntityWithContactId.photoBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoBitmap].
 */
suspend fun ExistingRawContactEntityWithContactId.photoBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoBitmap].
 */
suspend fun ExistingRawContactEntityWithContactId.photoBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoBitmapDrawable(contacts) }

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailInputStream].
 */
suspend fun ExistingRawContactEntityWithContactId.photoThumbnailInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoThumbnailInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailBytes].
 */
suspend fun ExistingRawContactEntityWithContactId.photoThumbnailBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoThumbnailBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailBitmap].
 */
suspend fun ExistingRawContactEntityWithContactId.photoThumbnailBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoThumbnailBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailBitmap].
 */
suspend fun ExistingRawContactEntityWithContactId.photoThumbnailBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoThumbnailBitmapDrawable(contacts) }

// endregion

// region SET PHOTO

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
suspend fun ExistingRawContactEntityWithContactId.setPhotoWithContext(
    contacts: Contacts,
    imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, imageBytes) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
suspend fun ExistingRawContactEntityWithContactId.setPhotoWithContext(
    contacts: Contacts,
    photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoInputStream) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
suspend fun ExistingRawContactEntityWithContactId.setPhotoWithContext(
    contacts: Contacts,
    photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoBitmap) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
suspend fun ExistingRawContactEntityWithContactId.setPhotoWithContext(
    contacts: Contacts,
    photoDrawable: BitmapDrawable,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [ExistingRawContactEntityWithContactId.removePhoto].
 */
suspend fun ExistingRawContactEntityWithContactId.removePhotoWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { removePhoto(contacts) }

// endregion

// endregion

// region ASYNC

// region GET PHOTO

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoInputStream].
 */
fun ExistingRawContactEntityWithContactId.photoInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> = CoroutineScope(coroutineContext).async { photoInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoBytes].
 */
fun ExistingRawContactEntityWithContactId.photoBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoBitmap].
 */
fun ExistingRawContactEntityWithContactId.photoBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoBitmap].
 */
fun ExistingRawContactEntityWithContactId.photoBitmapDrawableAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<BitmapDrawable?> =
    CoroutineScope(coroutineContext).async { photoBitmapDrawable(contacts) }

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailInputStream].
 */
fun ExistingRawContactEntityWithContactId.photoThumbnailInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> =
    CoroutineScope(coroutineContext).async { photoThumbnailInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailBytes].
 */
fun ExistingRawContactEntityWithContactId.photoThumbnailBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoThumbnailBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailBitmap].
 */
fun ExistingRawContactEntityWithContactId.photoThumbnailBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoThumbnailBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.photoThumbnailBitmap].
 */
fun ExistingRawContactEntityWithContactId.photoThumbnailBitmapDrawableAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<BitmapDrawable?> =
    CoroutineScope(coroutineContext).async { photoThumbnailBitmapDrawable(contacts) }

// endregion

// region SET PHOTO

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
fun ExistingRawContactEntityWithContactId.setPhotoAsync(
    contacts: Contacts,
    imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, imageBytes) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
fun ExistingRawContactEntityWithContactId.setPhotoAsync(
    contacts: Contacts,
    photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> =
    CoroutineScope(coroutineContext).async { setPhoto(contacts, photoInputStream) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
fun ExistingRawContactEntityWithContactId.setPhotoAsync(
    contacts: Contacts,
    photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoBitmap) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.setPhoto].
 */
fun ExistingRawContactEntityWithContactId.setPhotoAsync(
    contacts: Contacts,
    photoDrawable: BitmapDrawable,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingRawContactEntityWithContactId.removePhoto].
 */
fun ExistingRawContactEntityWithContactId.removePhotoAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { removePhoto(contacts) }

// endregion

// endregion