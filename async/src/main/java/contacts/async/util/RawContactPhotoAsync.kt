package contacts.async.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.RawContactEntity
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
 * See [RawContactEntity.photoInputStream].
 */
suspend fun RawContactEntity.photoInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoBytes].
 */
suspend fun RawContactEntity.photoBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoBitmap].
 */
suspend fun RawContactEntity.photoBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoBitmap].
 */
suspend fun RawContactEntity.photoBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoBitmapDrawable(contacts) }

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoThumbnailInputStream].
 */
suspend fun RawContactEntity.photoThumbnailInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoThumbnailInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoThumbnailBytes].
 */
suspend fun RawContactEntity.photoThumbnailBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoThumbnailBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoThumbnailBitmap].
 */
suspend fun RawContactEntity.photoThumbnailBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoThumbnailBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.photoThumbnailBitmap].
 */
suspend fun RawContactEntity.photoThumbnailBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoThumbnailBitmapDrawable(contacts) }

// endregion

// region SET PHOTO

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoWithContext(
    contacts: Contacts,
    imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, imageBytes) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoWithContext(
    contacts: Contacts,
    photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoInputStream) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoWithContext(
    contacts: Contacts,
    photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoBitmap) }

/**
 * Suspends the current coroutine, performs the operation in the given [coroutineContext], then
 * returns the result.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoWithContext(
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
 * See [RawContactEntity.removePhoto].
 */
suspend fun RawContactEntity.removePhotoWithContext(
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
 * See [RawContactEntity.photoInputStream].
 */
fun RawContactEntity.photoInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> = CoroutineScope(coroutineContext).async { photoInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.photoBytes].
 */
fun RawContactEntity.photoBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.photoBitmap].
 */
fun RawContactEntity.photoBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.photoBitmap].
 */
fun RawContactEntity.photoBitmapDrawableAsync(
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
 * See [RawContactEntity.photoThumbnailInputStream].
 */
fun RawContactEntity.photoThumbnailInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> =
    CoroutineScope(coroutineContext).async { photoThumbnailInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.photoThumbnailBytes].
 */
fun RawContactEntity.photoThumbnailBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoThumbnailBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.photoThumbnailBitmap].
 */
fun RawContactEntity.photoThumbnailBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoThumbnailBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.photoThumbnailBitmap].
 */
fun RawContactEntity.photoThumbnailBitmapDrawableAsync(
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
 * See [RawContactEntity.setPhoto].
 */
fun RawContactEntity.setPhotoAsync(
    contacts: Contacts,
    imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, imageBytes) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.setPhoto].
 */
fun RawContactEntity.setPhotoAsync(
    contacts: Contacts,
    photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> =
    CoroutineScope(coroutineContext).async { setPhoto(contacts, photoInputStream) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.setPhoto].
 */
fun RawContactEntity.setPhotoAsync(
    contacts: Contacts,
    photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoBitmap) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [RawContactEntity.setPhoto].
 */
fun RawContactEntity.setPhotoAsync(
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
 * See [RawContactEntity.removePhoto].
 */
fun RawContactEntity.removePhotoAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { removePhoto(contacts) }

// endregion

// endregion