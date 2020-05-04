package com.vestrel00.contacts.async.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.vestrel00.contacts.async.ASYNC_DISPATCHER
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.util.*
import kotlinx.coroutines.withContext
import java.io.InputStream

// region GET PHOTO

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoInputStream].
 */
suspend fun RawContactEntity.photoInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoBytes].
 */
suspend fun RawContactEntity.photoBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoBitmap].
 */
suspend fun RawContactEntity.photoBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoBitmap].
 */
suspend fun RawContactEntity.photoBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoBitmapDrawable(context) }

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoThumbnailInputStream].
 */
suspend fun RawContactEntity.photoThumbnailInputStreamAsync(context: Context): InputStream? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailInputStream(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoThumbnailBytes].
 */
suspend fun RawContactEntity.photoThumbnailBytesAsync(context: Context): ByteArray? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBytes(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoThumbnailBitmap].
 */
suspend fun RawContactEntity.photoThumbnailBitmapAsync(context: Context): Bitmap? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmap(context) }

/**
 * Suspends the current coroutine, gets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.photoThumbnailBitmap].
 */
suspend fun RawContactEntity.photoThumbnailBitmapDrawableAsync(context: Context): BitmapDrawable? =
    withContext(ASYNC_DISPATCHER) { photoThumbnailBitmapDrawable(context) }

// endregion

// region SET PHOTO

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoAsync(context: Context, imageBytes: ByteArray): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, imageBytes) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoAsync(
    context: Context, photoInputStream: InputStream
): Boolean = withContext(ASYNC_DISPATCHER) { setPhoto(context, photoInputStream) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoAsync(context: Context, photoBitmap: Bitmap): Boolean =
    withContext(ASYNC_DISPATCHER) { setPhoto(context, photoBitmap) }

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.setPhoto].
 */
suspend fun RawContactEntity.setPhotoAsync(
    context: Context, photoDrawable: BitmapDrawable
): Boolean = withContext(ASYNC_DISPATCHER) { setPhoto(context, photoDrawable) }

// endregion

// region REMOVE PHOTO

/**
 * Suspends the current coroutine, sets the photo in background, then returns the control flow to
 * the calling coroutine scope.
 *
 * See [RawContactEntity.removePhoto].
 */
suspend fun RawContactEntity.removePhotoAsync(context: Context): Boolean =
    withContext(ASYNC_DISPATCHER) { removePhoto(context) }

// endregion