package com.vestrel00.contacts.async.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.util.*
import kotlinx.coroutines.withContext
import java.io.InputStream

// SET PHOTO

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.setPhoto].
 */
suspend fun Contact.setPhotoAsync(imageBytes: ByteArray, context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(imageBytes, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.setPhoto].
 */
suspend fun Contact.setPhotoAsync(photoInputStream: InputStream, context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(photoInputStream, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.setPhoto].
 */
suspend fun Contact.setPhotoAsync(photoBitmap: Bitmap, context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(photoBitmap, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.setPhoto].
 */
suspend fun Contact.setPhotoAsync(photoDrawable: BitmapDrawable, context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(photoDrawable, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.setPhoto].
 */
suspend fun MutableContact.setPhotoAsync(imageBytes: ByteArray, context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(imageBytes, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.setPhoto].
 */
suspend fun MutableContact.setPhotoAsync(
    photoInputStream: InputStream,
    context: Context
): Boolean = withContext(ASYNC_DISPATCHER) { setPhoto(photoInputStream, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.setPhoto].
 */
suspend fun MutableContact.setPhotoAsync(photoBitmap: Bitmap, context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(photoBitmap, context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.setPhoto].
 */
suspend fun MutableContact.setPhotoAsync(
    photoDrawable: BitmapDrawable,
    context: Context
): Boolean = withContext(ASYNC_DISPATCHER) { setPhoto(photoDrawable, context) }

// GET PHOTO

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoInputStream].
 */
suspend fun Contact.photoInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoBytes].
 */
suspend fun Contact.photoBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoBitmap].
 */
suspend fun Contact.photoBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoBitmap].
 */
suspend fun Contact.photoBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoBitmapDrawable(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoInputStream].
 */
suspend fun MutableContact.photoInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoBytes].
 */
suspend fun MutableContact.photoBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoBitmap].
 */
suspend fun MutableContact.photoBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoBitmapDrawable].
 */
suspend fun MutableContact.photoBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoBitmapDrawable(context) }

// REMOVE PHOTO

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.removePhoto].
 */
suspend fun Contact.removePhotoAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { removePhoto(context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.removePhoto].
 */
suspend fun MutableContact.removePhotoAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { removePhoto(context) }

// GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoThumbnailInputStream].
 */
suspend fun Contact.photoThumbnailInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoThumbnailBytes].
 */
suspend fun Contact.photoThumbnailBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoThumbnailBitmap].
 */
suspend fun Contact.photoThumbnailBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [Contact.photoThumbnailBitmap].
 */
suspend fun Contact.photoThumbnailBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmapDrawable(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoThumbnailInputStream].
 */
suspend fun MutableContact.photoThumbnailInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoThumbnailBytes].
 */
suspend fun MutableContact.photoThumbnailBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoThumbnailBitmap].
 */
suspend fun MutableContact.photoThumbnailBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableContact.photoThumbnailBitmap].
 */
suspend fun MutableContact.photoThumbnailBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmapDrawable(context) }
