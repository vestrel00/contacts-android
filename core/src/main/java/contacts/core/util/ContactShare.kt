package contacts.core.util

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.isNotNullOrBlank

/**
 * Returns an [Intent] that allows you to share this existing contact.
 *
 * If [includePhoto] is set to false, photo data will NOT be included. This is useful in saving
 * space. This option is only available since API 23. Lower API levels will always include photo
 * data.
 *
 * This requires the [ExistingContactEntity.lookupKey] to be available. Otherwise, a null intent
 * will be returned.
 *
 * Typical usage of this is,
 *
 * ```kotlin
 * val shareIntent = contact.shareVCardIntent()
 * if (shareIntent != null) {
 *     activity.startActivity(Intent.createChooser(shareIntent, null))
 * }
 * ```
 *
 * The above code will open up a share sheet that will allow you to send the _.VCF_ file (a vCard)
 * containing the contact data. Opening this file in any OS (iOS, OSX, Windows) typically prompts
 * the addition of the contact contained in the vCard.
 *
 * The contact data is taken from the database and NOT from this instance. If you've made changes
 * to this instance, you should first perform an update operation on this in order to save it to
 * the database so that all data can be included in the share intent.
 *
 * **Custom data** are not supported and will not be included in the output vCard!
 */
@JvmOverloads
fun ExistingContactEntity.shareVCardIntent(includePhoto: Boolean = true): Intent? =
    vCardUri(includePhoto)?.shareVCardIntent()

/**
 * Returns an [Intent] that allows you to share all existing contacts in this collection.
 *
 * **Custom data** are not supported and will not be included in the output vCard!
 * If [includePhoto] is set to false, photo data will NOT be included. This is useful in saving
 * space. This option is only available since API 23. Lower API levels will always include photo
 * data. Note that setting this to false seems to not have any effect. Photo data is still included
 * in the output vCard. The [shareVCardIntent] does not have this issue.
 *
 * Contacts in this collection without an available [ExistingContactEntity.lookupKey] will be
 * excluded. If no available lookup key is found on any contacts in this collection, then this will
 * return null.
 *
 * This is only available for API 21 and above. This will return null for lower API levels.
 *
 * Typical usage of this is,
 *
 * ```kotlin
 * val shareIntent = contacts.shareMultiVCardIntent()
 * if (shareIntent != null) {
 *     activity.startActivity(Intent.createChooser(shareIntent, null))
 * }
 * ```
 *
 * The above code will open up a share sheet that will allow you to send the _.VCF_ file (a vCard)
 * containing all contacts' data. Opening this file in any OS (iOS, OSX, Windows) typically prompts
 * the addition of all contact(s) contained in the vCard.
 *
 * The contact data is taken from the database and NOT from this instance. If you've made changes
 * to this instance, you should first perform an update operation on this in order to save it to
 * the database so that all data can be included in the share intent.
 *
 * **Custom data** are not supported and will not be included in the output vCard!
 */
// [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Collection<ExistingContactEntity>.shareMultiVCardIntent(includePhoto: Boolean = true): Intent? =
    asSequence().shareMultiVCardIntent(includePhoto)

/**
 * See [shareMultiVCardIntent].
 */
fun Sequence<ExistingContactEntity>.shareMultiVCardIntent(includePhoto: Boolean = true): Intent? =
    multiVCardUri(includePhoto)?.shareVCardIntent()

private fun Uri.shareVCardIntent() = Intent(Intent.ACTION_SEND).also {
    it.type = ContactsContract.Contacts.CONTENT_VCARD_TYPE
    it.putExtra(Intent.EXTRA_STREAM, this)
}

private fun ExistingContactEntity.vCardUri(includePhoto: Boolean): Uri? {
    val lookupKey = lookupKey
    return if (lookupKey.isNullOrBlank()) {
        null
    } else {
        ContactsContract.Contacts.CONTENT_VCARD_URI
            .buildUpon()
            .appendPath(lookupKey)
            .withNoPhotoQueryParameter(includePhoto)
            .build()
    }
}

private fun Sequence<ExistingContactEntity>.multiVCardUri(includePhoto: Boolean): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        return null
    }

    val lookupKeys = map { it.lookupKey }.filter { it.isNotNullOrBlank() }.filterNotNull()
    return if (lookupKeys.isEmpty()) {
        null
    } else {
        ContactsContract.Contacts.CONTENT_MULTI_VCARD_URI
            .buildUpon()
            .appendPath(lookupKeys.joinToString(":"))
            .withNoPhotoQueryParameter(includePhoto)
            .build()
    }
}

private fun Uri.Builder.withNoPhotoQueryParameter(includePhoto: Boolean): Uri.Builder =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !includePhoto) {
        appendQueryParameter(
            ContactsContract.Contacts.QUERY_PARAMETER_VCARD_NO_PHOTO,
            "true"
        )
    } else {
        this
    }