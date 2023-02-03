package contacts.ui.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import contacts.core.entities.MutableOptionsEntity
import contacts.core.entities.NewOptions
import contacts.core.entities.OptionsEntity
import contacts.ui.R
import contacts.ui.util.onRingtoneSelected
import contacts.ui.util.selectRingtone

/**
 * A (horizontal) [LinearLayout] that displays a [OptionsEntity] and handles the modifications to
 * the given [data].
 *
 * Setting the [data] will automatically update the views. Any modifications in the views will also
 * be made to the [data].
 *
 * ## Note
 *
 * This is a very simple view that is not styled or made to look good. Consumers of the library may
 * choose to use this as is or simply as a reference on how to implement this part of AOSP
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
 */
class OptionsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    /**
     * The options that is shown in this view. Setting this will automatically update the views. Any
     * modifications in the views will also be made to the this (only if it is mutable).
     */
    var data: OptionsEntity = NewOptions()
        set(value) {
            field = value

            setOptionFields()
        }

    // Not using any view binding libraries or plugins just for this.
    private val starredView: ImageView
    private val sendToVoicemailView: CheckBox
    private val customRingtoneView: ImageView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        inflate(context, R.layout.view_options, this)

        starredView = findViewById(R.id.starred)
        sendToVoicemailView = findViewById(R.id.sendToVoicemail)
        customRingtoneView = findViewById(R.id.customRingtone)

        setOptionsFieldsListeners()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onRingtoneSelected(requestCode, resultCode, data) { ringtoneUri ->
            setCustomRingtone(ringtoneUri)
        }
    }

    private fun setOptionsFieldsListeners() {
        starredView.setOnClickListener {
            toggleStarred()
        }
        sendToVoicemailView.setOnCheckedChangeListener { _, isChecked ->
            sendToVoicemail(isChecked)
        }
        customRingtoneView.setOnClickListener {
            selectCustomRingtone()
        }
    }

    private fun setOptionFields() {
        setStarredView(data.starred == true)
        setSendToVoicemailView(data.sendToVoicemail == true)
    }

    private fun setStarredView(starred: Boolean) {
        // Image decoding done in UI thread but this should be fine as long as this is not displayed
        // in a list.
        starredView.setImageResource(
            if (starred) {
                android.R.drawable.star_big_on
            } else {
                android.R.drawable.star_big_off
            }
        )
    }

    private fun setSendToVoicemailView(sendToVoicemail: Boolean) {
        sendToVoicemailView.isChecked = sendToVoicemail
    }

    private fun toggleStarred() {
        val currentlyStarred = data.starred == true
        data.applyIfMutable {
            starred = !currentlyStarred
        }
        setStarredView(!currentlyStarred)
    }

    private fun sendToVoicemail(sendToVoicemail: Boolean) {
        data.applyIfMutable {
            this.sendToVoicemail = sendToVoicemail
        }
    }

    private fun selectCustomRingtone() {
        activity?.selectRingtone(data.customRingtone)
    }

    private fun setCustomRingtone(customRingtone: Uri?) {
        data.applyIfMutable {
            this.customRingtone = customRingtone
        }
    }
}

private fun OptionsEntity.applyIfMutable(block: MutableOptionsEntity.() -> Unit) {
    if (this is MutableOptionsEntity) {
        block(this)
    }
}