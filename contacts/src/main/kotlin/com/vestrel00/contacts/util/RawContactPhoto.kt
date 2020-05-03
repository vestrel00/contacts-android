package com.vestrel00.contacts.util

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.cursor.photoCursor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

// region GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * The stream should be closed after use.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoInputStream(context: Context): InputStream? =
    photoInputStream(context, id)

/**
 * Returns the full-sized photo as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoBytes(context: Context): ByteArray? = photoInputStream(context)?.apply {
    it.readBytes()
}

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

/**
 * See [RawContact.photoInputStream].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoInputStream(context: Context): InputStream? =
    photoInputStream(context, id)

/**
 * See [RawContact.photoBytes].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoBytes(context: Context): ByteArray? =
    photoInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * See [RawContact.photoBitmap].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * See [RawContact.photoBitmapDrawable].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

private fun photoInputStream(context: Context, rawContactId: Long?): InputStream? {
    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return null
    }

    val photoFileId = context.contentResolver.query(
        Table.DATA,
        Include(Fields.Photo.PhotoFileId),
        (Fields.RawContact.Id equalTo rawContactId) and (Fields.MimeType equalTo MimeType.PHOTO)
    ) {
        if (it.moveToNext()) {
            it.photoCursor().photoFileId
        } else {
            null
        }
    }

    if (photoFileId != null) {
        val photoUri =
            ContentUris.withAppendedId(ContactsContract.DisplayPhoto.CONTENT_URI, photoFileId)

        var inputStream: InputStream? = null
        try {
            val fd = context.contentResolver.openAssetFileDescriptor(photoUri, "r")
            inputStream = fd?.createInputStream()
        } finally {
            return inputStream
        }
    }

    return null
}

internal inline fun <T> InputStream.apply(block: (InputStream) -> T): T {
    val t = block(this)
    close()
    return t
}

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * The stream should be closed after use.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoThumbnailInputStream(context: Context): InputStream? =
    photoThumbnailInputStream(context, id)

/**
 * Returns the photo thumbnail as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * Returns the photo thumbnail as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the photo thumbnail as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

/**
 * See [RawContact.photoThumbnailInputStream].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailInputStream(context: Context): InputStream? =
    photoThumbnailInputStream(context, id)

/**
 * See [RawContact.photoThumbnailBytes].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * See [RawContact.photoThumbnailBitmap].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * See [RawContact.photoThumbnailBitmapDrawable].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

private fun photoThumbnailInputStream(context: Context, rawContactId: Long?): InputStream? {
    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return null
    }

    return context.contentResolver.query(
        Table.DATA,
        Include(Fields.Photo.PhotoThumbnail),
        (Fields.RawContact.Id equalTo rawContactId)
                and (Fields.MimeType equalTo MimeType.PHOTO)
    ) {
        val photoThumbnail: ByteArray? = if (it.moveToNext()) {
            it.photoCursor().photoThumbnail
        } else {
            null
        }

        if (photoThumbnail != null) ByteArrayInputStream(photoThumbnail) else null
    }
}

// endregion

// region SET PHOTO

/**
 * Sets the photo of this [RawContact]. If a photo already exists, it will be overwritten.
 * The Contacts Provider automatically creates a downsized version of this as the thumbnail.
 *
 * If this [RawContact] is the only one that make up a [com.vestrel00.contacts.entities.Contact],
 * then the photo set here will also be used by the Contacts Provider as the contact photo.
 * Otherwise, it may or may not be the photo picked by the Contacts Provider as the contact photo.
 *
 * Returns true if the operation succeeds.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(context: Context, photoBytes: ByteArray): Boolean =
    setRawContactPhoto(context, id, photoBytes)

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(context: Context, photoInputStream: InputStream): Boolean =
    setPhoto(context, photoInputStream.readBytes())

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(context: Context, photoBitmap: Bitmap): Boolean =
    setPhoto(context, photoBitmap.bytes())

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(context: Context, photoDrawable: BitmapDrawable): Boolean =
    setPhoto(context, photoDrawable.bitmap.bytes())

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(context: Context, photoBytes: ByteArray): Boolean =
    setRawContactPhoto(context, id, photoBytes)

/**
 * See [MutableRawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(context: Context, photoInputStream: InputStream): Boolean =
    setPhoto(context, photoInputStream.readBytes())

/**
 * See [MutableRawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(context: Context, photoBitmap: Bitmap): Boolean =
    setPhoto(context, photoBitmap.bytes())

/**
 * See [MutableRawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(context: Context, photoDrawable: BitmapDrawable): Boolean =
    setPhoto(context, photoDrawable.bitmap.bytes())

/**
 * The function body is mostly taken from the sample code from the [RawContacts.DisplayPhoto] class
 * documentation.
 */
internal fun setRawContactPhoto(
    context: Context, rawContactId: Long?, photoBytes: ByteArray
): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == null) {
        return false
    }

    var isSuccessful = false
    try {
        val photoUri = Uri.withAppendedPath(
            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
            RawContacts.DisplayPhoto.CONTENT_DIRECTORY
        )

        // Didn't want to force unwrap because I'm trying to keep the codebase free of it.
        // I wanted to fold the if-return using ?: but it results in a lint error about unreachable
        // code (it's not unreachable).
        val fd = context.contentResolver.openAssetFileDescriptor(photoUri, "rw")
        if (fd != null) {
            val os = fd.createOutputStream()

            os.write(photoBytes)

            os.close()
            fd.close()

            isSuccessful = true
        }
    } finally {
        return isSuccessful
    }
}

internal fun Bitmap.bytes(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

// endregion

// region REMOVE PHOTO

/**
 * Removes the photo of this [RawContact], if one exists.
 *
 * If this [RawContact] is the only one that make up a [com.vestrel00.contacts.entities.Contact],
 * then the contact photo will also be removed. Otherwise, it may or may not affect the contact
 * photo.
 *
 * Returns true if the operation succeeds.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.removePhoto(context: Context): Boolean = removeRawContactPhoto(context, id)

/**
 * See [RawContact.removePhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.removePhoto(context: Context): Boolean = removeRawContactPhoto(context, id)

// Removing the photo data row does the trick!
private fun removeRawContactPhoto(context: Context, rawContactId: Long?): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == null) {
        return false
    }

    val deleteRawContactPhotoOperation = ContentProviderOperation.newDelete(Table.DATA.uri)
        .withSelection(
            "${(Fields.RawContact.Id equalTo rawContactId)
                    and (Fields.MimeType equalTo MimeType.PHOTO)}",
            null
        )
        .build()

    // Delete returns the number of rows deleted, which doesn't indicate if the delete operation
    // succeeded or not because there may have not been a row to delete. Therefore, we use
    // applyBatch instead, which should indicate success or failure via exception throwing.
    try {
        context.contentResolver.applyBatch(
            ContactsContract.AUTHORITY,
            arrayListOf(deleteRawContactPhotoOperation)
        )
    } catch (exception: Exception) {
        return false
    }

    return true
}

// endregion