package com.vestrel00.contacts.ui.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.vestrel00.contacts.ui.R

@JvmOverloads
fun Activity.showPhotoPickerDialog(
    withRemovePhotoOption: Boolean = false,
    removePhoto: () -> Unit = {}
) {
    val items = if (withRemovePhotoOption) {
        R.array.contact_photo_picker_dialog_choices_1
    } else {
        R.array.contact_photo_picker_dialog_choices_2
    }

    AlertDialog.Builder(this)
        .setTitle(R.string.contact_photo_picker_dialog_title)
        .setItems(items) { _, choice ->
            when (choice) {
                0 -> takeNewPhoto()
                1 -> selectPhoto()
                2 -> removePhoto()
            }
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

fun Activity.takeNewPhoto() {
    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val component = takePhotoIntent.resolveActivity(packageManager)
    if (component != null) {
        startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO)
    } else {
        Toast.makeText(this, R.string.contact_photo_take_error, Toast.LENGTH_SHORT).show()
    }
}

fun Activity.selectPhoto() {
    val selectPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
    val component = selectPhotoIntent.resolveActivity(packageManager)
    if (component != null) {
        startActivityForResult(selectPhotoIntent, REQUEST_SELECT_PHOTO)
    } else {
        Toast.makeText(this, R.string.contact_photo_select_error, Toast.LENGTH_SHORT).show()
    }
}

fun onPhotoPicked(
    requestCode: Int, resultCode: Int, intent: Intent?,
    photoBitmapPicked: (bitmap: Bitmap) -> Unit,
    photoUriPicked: (uri: Uri) -> Unit
) {
    if (resultCode != Activity.RESULT_OK) {
        return
    }

    when (requestCode) {
        REQUEST_TAKE_PHOTO -> {
            val bitmap = intent?.extras?.get("data") as Bitmap?
            bitmap?.let(photoBitmapPicked)
        }
        REQUEST_SELECT_PHOTO -> {
            val uri = intent?.data
            uri?.let(photoUriPicked)
        }
    }
}

private const val REQUEST_TAKE_PHOTO = 9001
private const val REQUEST_SELECT_PHOTO = 9002