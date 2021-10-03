package contacts.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout
import contacts.entities.MutableNickname
import contacts.ui.R
import contacts.ui.text.AbstractTextWatcher

/**
 * A [FrameLayout] that displays a [MutableNickname] and handles the modifications to it.
 *
 * Setting the [data] will automatically update the views. Any modifications in the views will also
 * be made to the [data].
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
 */
class NicknameView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAttr) {

    /**
     * The name that is shown in this view. Setting this will automatically update the views. Any
     * modifications in the views will also be made to the this.
     */
    var data = MutableNickname()
        set(value) {
            field = value

            nicknameField.setText(field.name)
        }

    // This is the only view in this layout. I'm aware that this custom view can just inherit from
    // EditText directly and it would be better performance-wise. That was the initial
    // implementation. However, for some reason the styling, focus handling, and soft keyboard were
    // not working correctly. Probably has something to do with styles/themes not getting passed
    // down properly. So, out of laziness, I decided not to waste my time trying to subclass
    // EditText ;D
    private val nicknameField: EditText

    init {
        inflate(context, R.layout.view_nickname, this)

        nicknameField = findViewById(R.id.nicknameField)
        nicknameField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data.name = s?.toString()
            }
        })
    }
}