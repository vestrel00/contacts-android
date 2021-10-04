package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.EditText
import contacts.entities.MutableOrganization
import contacts.ui.R
import contacts.ui.text.AbstractTextWatcher

/**
 * A [CommonDataEntityView] for a [MutableOrganization].
 */
class OrganizationView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityView<MutableOrganization>(
    context, attributeSet, defStyleAttr,
    layoutRes = R.layout.view_organization,
    dataFieldInputType = InputType.TYPE_CLASS_TEXT,
    dataFieldHintResId = R.string.contacts_ui_organization_company_hint,
) {

    private val secondaryDataField: EditText

    init {
        secondaryDataField = findViewById(R.id.secondaryDataField)

        secondaryDataField.apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setHint(R.string.contacts_ui_organization_title_hint)
            addTextChangedListener(SecondaryDataFieldTextChangeListener())
        }
    }

    override fun onDataSet() {
        super.onDataSet()
        setSecondaryDataField()
    }

    private fun setSecondaryDataField() {
        secondaryDataField.setText(data?.title)
    }

    private inner class SecondaryDataFieldTextChangeListener : AbstractTextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            data?.title = s?.toString()
        }
    }
}