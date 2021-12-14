package contacts.sample.view

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import contacts.async.util.photoBitmapDrawableWithContext
import contacts.async.util.removePhotoWithContext
import contacts.async.util.setPhotoWithContext
import contacts.core.Contacts
import contacts.core.entities.ContactEntity
import contacts.sample.util.runIfExist
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * An [ImageView] that displays a [ContactEntity]'s photo and handles photo addition, modification,
 * and removal.
 *
 * Setting the [contact] will automatically update the views. Any modifications in the views will
 * also be made to the [contact]'s photo upon [savePhoto] (only if it is mutable).
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of native Contacts app.
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
class ContactPhotoView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PhotoView(context, attributeSet, defStyleAttr) {

    private var setContactPhotoJob: Job? = null

    /**
     * The Contact whose photo is shown in this view. Setting this will automatically update the
     * views. Any modifications in the views will also be made to the [contact]'s photo upon
     * [savePhoto].
     */
    private var contact: ContactEntity? = null

    /**
     * Sets the Contact shown and managed by this view to the given [contact] and uses the given
     * [contacts] API to perform operations on it.
     *
     * The [contact] will only be mutated if it is mutable.
     */
    fun setContact(contact: ContactEntity?, contacts: Contacts) {
        this.contact = contact
        setPhotoDrawableFromContact(contacts)
    }

    override suspend fun savePhotoToDb(photoDrawable: BitmapDrawable, contacts: Contacts): Boolean =
        contact.runIfExist {
            it.setPhotoWithContext(contacts, photoDrawable)
        } == true

    override suspend fun removePhotoFromDb(contacts: Contacts): Boolean =
        contact.runIfExist {
            it.removePhotoWithContext(contacts)
        } == true

    private fun setPhotoDrawableFromContact(contacts: Contacts) {
        setContactPhotoJob?.cancel()
        setContactPhotoJob = launch {
            setPhotoDrawable(contact.runIfExist {
                it.photoBitmapDrawableWithContext(contacts)
            })
        }
    }
}