package com.vestrel00.contacts.sample.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.ImageView
import com.vestrel00.contacts.async.util.photoBitmapDrawableWithContext
import com.vestrel00.contacts.async.util.removePhotoWithContext
import com.vestrel00.contacts.async.util.setPhotoWithContext
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.sample.R
import com.vestrel00.contacts.ui.util.onPhotoPicked
import com.vestrel00.contacts.ui.util.showPhotoPickerDialog
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * An [ImageView] that displays a Contact's photo and handles photo addition, modification, and
 * removal.
 *
 * ## Note
 *
 * This is a very simple view that is not styled or made to look good. Consumers of the library may
 * choose to use this as is or simply as a reference on how to implement this part of native
 * Contacts app.
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
class PhotoView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attributeSet, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val job: Job = SupervisorJob()
    private var setContactPhotoJob: Job? = null

    private var photoDrawable: BitmapDrawable? = null
    private var shouldSavePhoto: Boolean = false

    var contact: MutableContact? = null
        set(value) {
            field = value

            setPhotoDrawableFromContact()
        }

    fun init(activity: Activity) {
        setOnClickListener {
            activity.showPhotoPickerDialog(withRemovePhotoOption = photoDrawable != null) {
                shouldSavePhoto = true
                launch { setPhotoDrawable(null) }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onPhotoPicked(requestCode, resultCode, data,
            photoBitmapPicked = { bitmap ->
                shouldSavePhoto = true
                launch { setPhotoDrawable(BitmapDrawable(resources, bitmap)) }
            },
            photoUriPicked = { uri ->
                shouldSavePhoto = true
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

                    setPhotoDrawable(BitmapDrawable(resources, bitmap))
                }
            }
        )
    }

    suspend fun saveContactPhoto(): Boolean {
        if (!shouldSavePhoto) {
            return true
        }

        val contact = contact
        val photoDrawable = photoDrawable

        if (contact == null) {
            return false
        }

        return if (photoDrawable != null) {
            contact.setPhotoWithContext(context, photoDrawable)
        } else {
            contact.removePhotoWithContext(context)
        }
    }

    private fun setPhotoDrawableFromContact() {
        setContactPhotoJob?.cancel()
        setContactPhotoJob = launch {
            setPhotoDrawable(contact?.photoBitmapDrawableWithContext(context))
        }
    }

    private suspend fun setPhotoDrawable(photoDrawable: BitmapDrawable?) {
        this.photoDrawable = photoDrawable

        if (photoDrawable != null) {
            scaleType = ScaleType.CENTER_CROP

            setImageDrawable(photoDrawable)
        } else {
            scaleType = ScaleType.FIT_CENTER

            val placeHolderImageDrawable = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    context.getDrawable(R.drawable.placeholder_photo)
                } else {
                    @Suppress("Deprecation")
                    context.resources.getDrawable(R.drawable.placeholder_photo)
                }
            }
            setImageDrawable(placeHolderImageDrawable)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }
}