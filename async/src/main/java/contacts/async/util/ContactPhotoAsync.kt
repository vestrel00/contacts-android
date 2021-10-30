package contacts.async.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import contacts.async.ASYNC_DISPATCHER
import contacts.core.Contacts
import contacts.core.entities.ContactEntity
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
 * See [ContactEntity.photoInputStream].
 */
suspend fun ContactEntity.photoInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoBytes].
 */
suspend fun ContactEntity.photoBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoBitmap].
 */
suspend fun ContactEntity.photoBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoBitmap].
 */
suspend fun ContactEntity.photoBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoBitmapDrawable(contacts) }

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoThumbnailInputStream].
 */
suspend fun ContactEntity.photoThumbnailInputStreamWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): InputStream? = withContext(coroutineContext) { photoThumbnailInputStream(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoThumbnailBytes].
 */
suspend fun ContactEntity.photoThumbnailBytesWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): ByteArray? = withContext(coroutineContext) { photoThumbnailBytes(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoThumbnailBitmap].
 */
suspend fun ContactEntity.photoThumbnailBitmapWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Bitmap? = withContext(coroutineContext) { photoThumbnailBitmap(contacts) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.photoThumbnailBitmap].
 */
suspend fun ContactEntity.photoThumbnailBitmapDrawableWithContext(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): BitmapDrawable? = withContext(coroutineContext) { photoThumbnailBitmapDrawable(contacts) }

// endregion

// region SET PHOTO

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.setPhoto].
 */
suspend fun ContactEntity.setPhotoWithContext(
    contacts: Contacts, imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, imageBytes) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.setPhoto].
 */
suspend fun ContactEntity.setPhotoWithContext(
    contacts: Contacts, photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoInputStream) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.setPhoto].
 */
suspend fun ContactEntity.setPhotoWithContext(
    contacts: Contacts, photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoBitmap) }

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.setPhoto].
 */
suspend fun ContactEntity.setPhotoWithContext(
    contacts: Contacts, photoDrawable: BitmapDrawable,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Boolean = withContext(coroutineContext) { setPhoto(contacts, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Suspends the current coroutine, performs the operation in background, then returns the control
 * flow to the calling coroutine scope.
 *
 * See [ContactEntity.removePhoto].
 */
suspend fun ContactEntity.removePhotoWithContext(
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
 * See [ContactEntity.photoInputStream].
 */
fun ContactEntity.photoInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> = CoroutineScope(coroutineContext).async { photoInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.photoBytes].
 */
fun ContactEntity.photoBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.photoBitmap].
 */
fun ContactEntity.photoBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.photoBitmap].
 */
fun ContactEntity.photoBitmapDrawableAsync(
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
 * See [ContactEntity.photoThumbnailInputStream].
 */
fun ContactEntity.photoThumbnailInputStreamAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<InputStream?> =
    CoroutineScope(coroutineContext).async { photoThumbnailInputStream(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.photoThumbnailBytes].
 */
fun ContactEntity.photoThumbnailBytesAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<ByteArray?> = CoroutineScope(coroutineContext).async { photoThumbnailBytes(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.photoThumbnailBitmap].
 */
fun ContactEntity.photoThumbnailBitmapAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Bitmap?> = CoroutineScope(coroutineContext).async { photoThumbnailBitmap(contacts) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.photoThumbnailBitmap].
 */
fun ContactEntity.photoThumbnailBitmapDrawableAsync(
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
 * See [ContactEntity.setPhoto].
 */
fun ContactEntity.setPhotoAsync(
    contacts: Contacts, imageBytes: ByteArray,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, imageBytes) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.setPhoto].
 */
fun ContactEntity.setPhotoAsync(
    contacts: Contacts, photoInputStream: InputStream,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> =
    CoroutineScope(coroutineContext).async { setPhoto(contacts, photoInputStream) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.setPhoto].
 */
fun ContactEntity.setPhotoAsync(
    contacts: Contacts, photoBitmap: Bitmap,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoBitmap) }

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.setPhoto].
 */
fun ContactEntity.setPhotoAsync(
    contacts: Contacts, photoDrawable: BitmapDrawable,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { setPhoto(contacts, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Creates a [CoroutineScope] with the given [coroutineContext], performs the operation in that
 * scope, then returns the [Deferred] result.
 *
 * See [ContactEntity.removePhoto].
 */
fun ContactEntity.removePhotoAsync(
    contacts: Contacts,
    coroutineContext: CoroutineContext = ASYNC_DISPATCHER
): Deferred<Boolean> = CoroutineScope(coroutineContext).async { removePhoto(contacts) }

// endregion

// endregion