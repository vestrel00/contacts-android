package com.vestrel00.contacts.sample.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.ImageView
import com.vestrel00.contacts.async.util.photoBitmapDrawableAsync
import com.vestrel00.contacts.async.util.removePhotoAsync
import com.vestrel00.contacts.async.util.setPhotoAsync
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.sample.R
import com.vestrel00.contacts.ui.util.onPhotoPicked
import com.vestrel00.contacts.ui.util.showPhotoPickerDialog
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

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
                    val bitmap = withContext(Dispatchers.IO) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
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
            contact.setPhotoAsync(photoDrawable, context)
        } else {
            contact.removePhotoAsync(context)
        }
    }

    private fun setPhotoDrawableFromContact() {
        setContactPhotoJob?.cancel()
        setContactPhotoJob = launch {
            setPhotoDrawable(contact?.photoBitmapDrawableAsync(context))
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
                context.resources.getDrawable(R.drawable.placeholder_photo, null)
            }
            setImageDrawable(placeHolderImageDrawable)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }
}