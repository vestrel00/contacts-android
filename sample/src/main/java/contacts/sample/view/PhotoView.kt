package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.ImageView
import contacts.sample.R
import contacts.ui.util.onPhotoPicked
import contacts.ui.util.showPhotoPickerDialog
import contacts.ui.view.activity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * An abstract [ImageView] that displays a photo and handles photo addition, modification, and
 * removal.
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of AOSP Contacts app.
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 *
 * The community may contribute by styling and adding more features and customizations with these
 * views if desired.
 *
 * ## Developer Notes
 *
 * I usually am a proponent of passive views and don't add any logic to views. However, I will make
 * an exception for this basic view that I don't really encourage consumers to use.
 *
 * This is in the sample and not in the contacts-ui module because it requires concurrency. We
 * should not add coroutines and contacts-async as dependencies to contacts-ui just for this.
 * Consumers may copy and paste this into their projects or if the community really wants it, we may
 * move this to a separate module (contacts-ui-async).
 */
abstract class PhotoView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attributeSet, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    private var photoDrawable: BitmapDrawable? = null
    private var isPickingPhoto: Boolean = false

    /**
     * When [onPhotoPicked] is invoked, this photo view's [onPhotoPicked] will also be invoked.
     * This is useful for propagating the pick event between a Contact and its primary photo holder
     * (a RawContact).
     */
    var invokeOnPhotoPicked: PhotoView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnClickListener {
            activity?.let {
                isPickingPhoto = true
                it.showPhotoPickerDialog(
                    withRemovePhotoOption = photoDrawable != null,
                    removePhoto = {
                        launch {
                            onPhotoPicked(null)
                            isPickingPhoto = false
                        }
                    },
                    onCancelled = {
                        isPickingPhoto = false
                    }
                )
            }
        }
    }

    /**
     * Invoke this method on the host activity's onActivityResult in order to process the picked
     * photo (if any). This will do nothing if the request did not originate from this view.
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!isPickingPhoto) {
            return
        }
        onPhotoPicked(requestCode, resultCode, data,
            photoBitmapPicked = { bitmap ->
                launch {
                    onPhotoPicked(BitmapDrawable(resources, bitmap))
                    isPickingPhoto = false
                }
            },
            photoUriPicked = { uri ->
                launch {
                    // FIXME This suppression should no longer be necessary once the compiler is
                    // smart enough to realize that these blocking calls are ran using the
                    // Dispatchers.IO, which is designed to handle blocking calls.
                    // May require READ_EXTERNAL_STORAGE
                    @Suppress("BlockingMethodInNonBlockingContext")
                    val bitmap = withContext(Dispatchers.IO) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(context.contentResolver, uri)
                            )
                        } else {
                            // Ugh, this suppression should not be necessary because this is only
                            // called pre Android P. I doubt "they" will ever fix this...
                            @Suppress("Deprecation")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                    }

                    onPhotoPicked(BitmapDrawable(resources, bitmap))
                    isPickingPhoto = false
                }
            }
        )
    }

    protected suspend fun setPhotoDrawable(photoDrawable: BitmapDrawable?) {
        this.photoDrawable = photoDrawable

        if (photoDrawable != null) {
            scaleType = ScaleType.CENTER_CROP

            setImageDrawable(photoDrawable)
        } else {
            scaleType = ScaleType.FIT_CENTER

            val placeHolderImageDrawable = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    context.getDrawable(R.drawable.contact_placeholder_photo)
                } else {
                    @Suppress("Deprecation")
                    context.resources.getDrawable(R.drawable.contact_placeholder_photo)
                }
            }
            setImageDrawable(placeHolderImageDrawable)
        }
    }

    protected open suspend fun onPhotoPicked(photoDrawable: BitmapDrawable?) {
        setPhotoDrawable(photoDrawable)

        // Prevent an infinite loop by only invoking this from the on photo picked origin.
        if (isPickingPhoto) {
            invokeOnPhotoPicked?.onPhotoPicked(photoDrawable)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}