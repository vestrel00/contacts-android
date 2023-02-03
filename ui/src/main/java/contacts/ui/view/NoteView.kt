package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import contacts.core.entities.MutableNoteEntity
import contacts.core.entities.NewNote
import contacts.core.entities.NoteEntity
import contacts.ui.R

/**
 * Am [EditText] that displays a [NoteEntity] and handles the modifications to the given [data].
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
class NoteView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : EditText(context, attributeSet, defStyleAttr) {

    /**
     * The note that is shown in this view. Setting this will automatically update the views. Any
     * modifications in the views will also be made to the this (only if it is mutable).
     */
    var data: NoteEntity = NewNote()
        set(value) {
            field = value

            setText(value.note)
        }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setHint(R.string.contacts_ui_note_hint)
    }

    override fun onTextChanged(
        text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        data.applyIfMutable {
            note = text?.toString()
        }
    }
}

private fun NoteEntity.applyIfMutable(block: MutableNoteEntity.() -> Unit) {
    if (this is MutableNoteEntity) {
        block(this)
    }
}