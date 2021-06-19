package contacts.ui.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import contacts.ui.R

/**
 * Shows an alert dialog with two options; take a new photo using the camera and elect an existing
 * photo.
 *
 * If the [withRemovePhotoOption] is true, then a third option is available; remove photo.
 *
 * This is used in conjunction with [onPhotoPicked] to process the results.
 *
 * ## Important!
 *
 * This uses [Activity.takeNewPhoto], which only provides a thumbnail version of the photo taken.
 * To get full-sized photos, see https://developer.android.com/training/camera/photobasics#TaskPath
 */
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

/**
 * Launches an external activity to take a new photo using the camera.
 *
 * This is used in the [showPhotoPickerDialog] but can also be used on its own in conjunction
 * with [onPhotoPicked].
 *
 * The photo will be available as a bitmap thumbnail in [Activity.onActivityResult];
 *
 * ```kotlin
 * val bitmap = intent?.extras?.get("data") as Bitmap?
 * ```
 *
 * #### Manifest
 *
 * Starting with Android 11 (API 30), you must include the following to your manifest in order to
 * successfully use this function.
 *
 * ```
 * <queries>
 *     <intent>
 *         <action android:name="android.media.action.IMAGE_CAPTURE" />
 *     </intent>
 * </queries>
 * ```
 * ## Important!
 *
 * This only provides a thumbnail version of the photo taken. To get full-sized photos, see
 * https://developer.android.com/training/camera/photobasics#TaskPath
 *
 * TODO? Implement full-sized photos version and deprecate this.
 */
fun Activity.takeNewPhoto() {
    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    @SuppressLint("QueryPermissionsNeeded")
    val component = takePhotoIntent.resolveActivity(packageManager)
    if (component != null) {
        startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO)
    } else {
        Toast.makeText(this, R.string.contact_photo_take_error, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Launches an external activity to select an existing photo.
 *
 * This is used in the [showPhotoPickerDialog] but can also be used on its own in conjunction
 * with [onPhotoPicked].
 *
 * #### Manifest
 *
 * Starting with Android 11 (API 30), you must include the following to your manifest in order to
 * successfully use this function.
 *
 * ```
 * <queries>
 *     <intent>
 *         <action android:name="android.intent.action.PICK" />
 *     </intent>
 * </queries>
 * ```
 */
fun Activity.selectPhoto() {
    val selectPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

    @SuppressLint("QueryPermissionsNeeded")
    val component = selectPhotoIntent.resolveActivity(packageManager)
    if (component != null) {
        startActivityForResult(selectPhotoIntent, REQUEST_SELECT_PHOTO)
    } else {
        Toast.makeText(this, R.string.contact_photo_select_error, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Call this in [Activity.onActivityResult] to get the image bitmap or uri after the take or select
 * photo activity has finished. This is used in conjunction with [showPhotoPickerDialog] or
 * [takeNewPhoto] or [selectPhoto].
 */
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