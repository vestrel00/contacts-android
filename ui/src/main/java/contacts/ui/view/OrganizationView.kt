package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.EditText
import contacts.core.entities.MutableOrganizationEntity
import contacts.core.entities.OrganizationEntity
import contacts.ui.R
import contacts.ui.text.AbstractTextWatcher

/**
 * A [DataEntityView] for an [OrganizationEntity].
 */
class OrganizationView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityView<OrganizationEntity>(
    context, attributeSet, defStyleAttr,
    layoutRes = R.layout.view_organization,
    dataFieldInputType = InputType.TYPE_CLASS_TEXT,
    dataFieldHintResId = R.string.contacts_ui_organization_company_hint,
) {

    private val secondaryDataField: EditText =
        findViewById<EditText>(R.id.secondaryDataField).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setHint(R.string.contacts_ui_organization_title_hint)
            addTextChangedListener(SecondaryDataFieldTextChangeListener())
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
            val data = data // reassignment required for null-check and casting successfully.
            if (data != null && data is MutableOrganizationEntity) {
                data.title = s?.toString()
            }
        }
    }
}