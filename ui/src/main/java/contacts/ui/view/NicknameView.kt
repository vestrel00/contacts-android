package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.MutableNickname
import contacts.ui.R

/**
 * A [CommonDataEntityView] for a [MutableNickname].
 */
class NicknameView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityView<MutableNickname>(
    context, attributeSet, defStyleAttr,
    dataFieldInputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
    dataFieldHintResId = R.string.contacts_ui_nickname_hint,
    dataDeleteButtonIsVisible = false
)