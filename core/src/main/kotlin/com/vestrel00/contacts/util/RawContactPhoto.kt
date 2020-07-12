package com.vestrel00.contacts.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.cursor.photoCursor
import com.vestrel00.contacts.entities.operation.newDelete
import com.vestrel00.contacts.entities.operation.withSelection
import com.vestrel00.contacts.entities.table.Table
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

// region GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * It is up to the caller to close the [InputStream].
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
fun RawContactEntity.photoInputStream(context: Context): InputStream? {
    val rawContactId = id

    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return null
    }

    val photoFileId = context.contentResolver.query(
        Table.Data,
        Include(Fields.Photo.PhotoFileId),
        (Fields.RawContact.Id equalTo rawContactId) and (Fields.MimeType equalTo MimeType.PHOTO)
    ) {
        it.getNextOrNull { it.photoCursor().photoFileId }
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
fun RawContactEntity.photoBytes(context: Context): ByteArray? = photoInputStream(context)?.apply {
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
fun RawContactEntity.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
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
fun RawContactEntity.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
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
 * It is up to the caller to close the [InputStream].
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
fun RawContactEntity.photoThumbnailInputStream(context: Context): InputStream? {
    val rawContactId = id

    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return null
    }

    return context.contentResolver.query(
        Table.Data,
        Include(Fields.Photo.PhotoThumbnail),
        (Fields.RawContact.Id equalTo rawContactId)
                and (Fields.MimeType equalTo MimeType.PHOTO)
    ) {
        val photoThumbnail = it.getNextOrNull { it.photoCursor().photoThumbnail }
        photoThumbnail?.let(::ByteArrayInputStream)
    }
}

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
fun RawContactEntity.photoThumbnailBytes(context: Context): ByteArray? =
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
fun RawContactEntity.photoThumbnailBitmap(context: Context): Bitmap? =
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
fun RawContactEntity.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

// endregion

// region SET PHOTO

/**
 * Sets the photo of this [RawContactEntity]. If a photo already exists, it will be overwritten.
 * The Contacts Provider automatically creates a downsized version of this as the thumbnail.
 *
 * If this [RawContactEntity] is the only one that make up a
 * [com.vestrel00.contacts.entities.ContactEntity], then the photo set here will also be used by the
 * Contacts Provider as the contact photo. Otherwise, it may or may not be the photo picked by the
 * Contacts Provider as the contact photo.
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
 *
 * ## Developer notes
 *
 * The function body is mostly taken from the sample code from the [RawContacts.DisplayPhoto] class
 * documentation.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.setPhoto(context: Context, photoBytes: ByteArray): Boolean =
    setRawContactPhoto(context, id, photoBytes)

/**
 * See [RawContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.setPhoto(context: Context, photoInputStream: InputStream): Boolean =
    setPhoto(context, photoInputStream.readBytes())

/**
 * See [RawContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.setPhoto(context: Context, photoBitmap: Bitmap): Boolean =
    setPhoto(context, photoBitmap.bytes())

/**
 * See [RawContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.setPhoto(context: Context, photoDrawable: BitmapDrawable): Boolean =
    setPhoto(context, photoDrawable.bitmap.bytes())

internal fun setRawContactPhoto(context: Context, rawContactId: Long?, photoBytes: ByteArray):
        Boolean {

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
 * Removes the photo of this [RawContactEntity], if one exists.
 *
 * If this [RawContactEntity] is the only one that make up a
 * [com.vestrel00.contacts.entities.ContactEntity], then the contact photo will also be removed.
 * Otherwise, it may or may not affect the contact photo.
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
fun RawContactEntity.removePhoto(context: Context): Boolean {
    val rawContactId = id

    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == null) {
        return false
    }

    return context.contentResolver.applyBatch(
        newDelete(Table.Data)
            .withSelection(
                (Fields.RawContact.Id equalTo rawContactId)
                        and (Fields.MimeType equalTo MimeType.PHOTO)
            )
            .build()
    ) != null
}

// endregion