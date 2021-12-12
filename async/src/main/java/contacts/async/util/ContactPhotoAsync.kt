package contacts.async.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ExistingContactEntity
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
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoInputStream].
 */
suspend fun ExistingContactEntity.photoInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoBytes].
 */
suspend fun ExistingContactEntity.photoBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoBitmap].
 */
suspend fun ExistingContactEntity.photoBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoBitmap].
 */
suspend fun ExistingContactEntity.photoBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoBitmapDrawable(contacts) }

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoThumbnailInputStream].
 */
suspend fun ExistingContactEntity.photoThumbnailInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoThumbnailInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoThumbnailBytes].
 */
suspend fun ExistingContactEntity.photoThumbnailBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoThumbnailBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoThumbnailBitmap].
 */
suspend fun ExistingContactEntity.photoThumbnailBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoThumbnailBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.photoThumbnailBitmap].
 */
suspend fun ExistingContactEntity.photoThumbnailBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoThumbnailBitmapDrawable(contacts) }

// endregion

// region SET PHOTO

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.setPhoto].
 */
suspend fun ExistingContactEntity.setPhotoWithContext(
    contacts: Contacts, imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, imageBytes) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.setPhoto].
 */
suspend fun ExistingContactEntity.setPhotoWithContext(
    contacts: Contacts, photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoInputStream) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.setPhoto].
 */
suspend fun ExistingContactEntity.setPhotoWithContext(
    contacts: Contacts, photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoBitmap) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.setPhoto].
 */
suspend fun ExistingContactEntity.setPhotoWithContext(
    contacts: Contacts, photoDrawable: BitmapDrawable,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ExistingContactEntity.removePhoto].
 */
suspend fun ExistingContactEntity.removePhotoWithContext(
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
 * See [ExistingContactEntity.photoInputStream].
 */
fun ExistingContactEntity.photoInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> = CoroutineScope(coroutineContext).async { photoInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.photoBytes].
 */
fun ExistingContactEntity.photoBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.photoBitmap].
 */
fun ExistingContactEntity.photoBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.photoBitmap].
 */
fun ExistingContactEntity.photoBitmapDrawableAsync(
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
 * See [ExistingContactEntity.photoThumbnailInputStream].
 */
fun ExistingContactEntity.photoThumbnailInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> =
    CoroutineScope(coroutineContext).async { photoThumbnailInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.photoThumbnailBytes].
 */
fun ExistingContactEntity.photoThumbnailBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoThumbnailBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.photoThumbnailBitmap].
 */
fun ExistingContactEntity.photoThumbnailBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoThumbnailBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.photoThumbnailBitmap].
 */
fun ExistingContactEntity.photoThumbnailBitmapDrawableAsync(
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
 * See [ExistingContactEntity.setPhoto].
 */
fun ExistingContactEntity.setPhotoAsync(
    contacts: Contacts, imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, imageBytes) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.setPhoto].
 */
fun ExistingContactEntity.setPhotoAsync(
    contacts: Contacts, photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> =
    CoroutineScope(coroutineContext).async { setPhoto(contacts, photoInputStream) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.setPhoto].
 */
fun ExistingContactEntity.setPhotoAsync(
    contacts: Contacts, photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoBitmap) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.setPhoto].
 */
fun ExistingContactEntity.setPhotoAsync(
    contacts: Contacts, photoDrawable: BitmapDrawable,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ExistingContactEntity.removePhoto].
 */
fun ExistingContactEntity.removePhotoAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { removePhoto(contacts) }

// endregion

// endregion