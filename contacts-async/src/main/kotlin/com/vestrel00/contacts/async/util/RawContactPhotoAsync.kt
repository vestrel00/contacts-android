package com.vestrel00.contacts.async.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.util.*
import kotlinx.coroutines.withContext
import java.io.InputStream

// SET PHOTO

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.setPhoto].
 */
suspend fun RawContact.setPhotoAsync(context: Context, imageBytes: ByteArray): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, imageBytes) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.setPhoto].
 */
suspend fun RawContact.setPhotoAsync(context: Context, photoInputStream: InputStream): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, photoInputStream) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.setPhoto].
 */
suspend fun RawContact.setPhotoAsync(context: Context, photoBitmap: Bitmap): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, photoBitmap) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.setPhoto].
 */
suspend fun RawContact.setPhotoAsync(context: Context, photoDrawable: BitmapDrawable): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, photoDrawable) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.setPhoto].
 */
suspend fun MutableRawContact.setPhotoAsync(context: Context, imageBytes: ByteArray): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, imageBytes) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.setPhoto].
 */
suspend fun MutableRawContact.setPhotoAsync(
    context: Context, photoInputStream: InputStream
): Boolean = withContext(ASYNC_DISPATCHER) { setPhoto(context, photoInputStream) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.setPhoto].
 */
suspend fun MutableRawContact.setPhotoAsync(context: Context, photoBitmap: Bitmap): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, photoBitmap) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.setPhoto].
 */
suspend fun MutableRawContact.setPhotoAsync(
    context: Context, photoDrawable: BitmapDrawable
): Boolean = withContext(ASYNC_DISPATCHER) { setPhoto(context, photoDrawable) }

// REMOVE PHOTO

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.removePhoto].
 */
suspend fun RawContact.removePhotoAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { removePhoto(context) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.removePhoto].
 */
suspend fun MutableRawContact.removePhotoAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { removePhoto(context) }

// GET PHOTO

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoInputStream].
 */
suspend fun RawContact.photoInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoBytes].
 */
suspend fun RawContact.photoBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoBitmap].
 */
suspend fun RawContact.photoBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoBitmap].
 */
suspend fun RawContact.photoBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoBitmapDrawable(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoInputStream].
 */
suspend fun MutableRawContact.photoInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoBytes].
 */
suspend fun MutableRawContact.photoBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoBitmap].
 */
suspend fun MutableRawContact.photoBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoBitmapDrawable].
 */
suspend fun MutableRawContact.photoBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoBitmapDrawable(context) }

// GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoThumbnailInputStream].
 */
suspend fun RawContact.photoThumbnailInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoThumbnailBytes].
 */
suspend fun RawContact.photoThumbnailBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoThumbnailBitmap].
 */
suspend fun RawContact.photoThumbnailBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContact.photoThumbnailBitmap].
 */
suspend fun RawContact.photoThumbnailBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmapDrawable(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoThumbnailInputStream].
 */
suspend fun MutableRawContact.photoThumbnailInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoThumbnailBytes].
 */
suspend fun MutableRawContact.photoThumbnailBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoThumbnailBitmap].
 */
suspend fun MutableRawContact.photoThumbnailBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [MutableRawContact.photoThumbnailBitmap].
 */
suspend fun MutableRawContact.photoThumbnailBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmapDrawable(context) }
