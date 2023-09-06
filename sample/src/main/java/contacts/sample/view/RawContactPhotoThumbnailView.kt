package contacts.sample.view

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import contacts.async.util.photoThumbnailBitmapDrawableWithContext
import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContactEntity
import contacts.core.util.PhotoData
import contacts.core.util.removePhoto
import contacts.core.util.setPhoto
import contacts.sample.util.runIfExist
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * An [ImageView] that displays a [RawContactEntity]'s photo thumbnail and handles photo addition,
 * modification, and removal.
 *
 * Setting the [rawContact] will automatically update the views. Any modifications in the views will
 * also be made to the [rawContact]'s photo upon [onPhotoPicked] (only if it is mutable).
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
class RawContactPhotoThumbnailView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PhotoView(context, attributeSet, defStyleAttr) {

    /**
     * The RawContact whose photo thumbnail is shown in this view. Setting this will automatically
     * update the views. Any modifications in the views will also be made to the [rawContact]'s
     * photo upon insert or update.
     */
    private var rawContact: RawContactEntity? = null

    /**
     * Sets the RawContact shown and managed by this view to the given [rawContact] and uses the
     * given [contacts] API to perform operations on it.
     */
    fun setRawContact(rawContact: RawContactEntity?, contacts: Contacts) {
        this.rawContact = rawContact
        setPhotoThumbnailDrawableFromRawContact(contacts)
    }

    private var setRawContactPhotoJob: Job? = null

    override suspend fun onPhotoPicked(photoDrawable: BitmapDrawable?) {
        super.onPhotoPicked(photoDrawable)
        rawContact?.let {
            if (photoDrawable != null) {
                when (it) {
                    is NewRawContact -> {
                        it.setPhoto(PhotoData.from(photoDrawable))
                    }
                    is MutableRawContact -> it.setPhoto(PhotoData.from(photoDrawable))
                    else -> {
                        // Do nothing
                    }
                }
            } else {
                when (it) {
                    is NewRawContact -> it.removePhoto()
                    is MutableRawContact -> it.removePhoto()
                    else -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun setPhotoThumbnailDrawableFromRawContact(contacts: Contacts) {
        setRawContactPhotoJob?.cancel()
        setRawContactPhotoJob = launch {
            setPhotoDrawable(rawContact.runIfExist {
                it.photoThumbnailBitmapDrawableWithContext(contacts)
            })
        }
    }
}