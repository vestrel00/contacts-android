package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import contacts.entities.CommonDataEntity
import contacts.entities.MutableCommonDataEntity
import contacts.ui.R
import contacts.ui.dialog.CustomLabelInputDialog
import contacts.ui.text.AbstractTextWatcher
import contacts.ui.util.CommonDataEntityType

/**
 * A [RelativeLayout] that displays a [MutableCommonDataEntity] [K] that has a
 * [CommonDataEntityType] [V] (which wraps a [CommonDataEntity.Type] [T]) and handles the
 * modifications to it.
 *
 * Setting the [data] will automatically update the views and vice versa.
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
abstract class MutableCommonDataEntityWithTypeView
<K : MutableCommonDataEntity, T : CommonDataEntity.Type, V : CommonDataEntityType<T>>
@JvmOverloads constructor(
    context: Context,
    initialData: K,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr) {

    /**
     * The data entity that is shown in this view. Changing this will automatically update the views
     * and vice versa.
     */
    var data: K = initialData
        set(value) {
            field = value

            setDataField()
            setDataTypeField()
            setDataDeleteButton()
        }

    /**
     * Invoked when the delete button is clicked.
     */
    var onDataDeleteButtonClicked: (() -> Unit)? = null

    /**
     * Invoked when the data field is cleared.
     */
    var onDataCleared: (() -> Unit)? = null

    /**
     * Invoked when the a piece of the data field is entered from a blank state.
     */
    var onDataBegin: (() -> Unit)? = null

    private var selectedType: V? = null
        set(value) {
            field = value

            value?.let {
                setDataTypeAndTypeLabelValue(it.type, it.typeLabel)
            }
        }

    protected abstract val dataField: EditText
    protected abstract val dataTypeField: Spinner
    protected abstract val dataDeleteButton: View

    protected abstract val dataValue: String?
    protected abstract val dataType: V
    protected abstract val systemDataTypes: List<V>

    protected abstract fun setDataValue(value: String?)
    protected abstract fun setDataTypeAndTypeLabelValue(type: T, typeLabel: String?)
    protected abstract fun createUserCustomDataTypeWithLabel(typeLabel: String): V

    private val dataTypesAdapter: ArrayAdapter<V> =
        ArrayAdapter(context, android.R.layout.simple_list_item_1)

    // Must be called by subclass after immediately after creation to complete initialization.
    protected fun initViews() {
        dataField.apply {
            setOnFocusChangeListener { _, _ ->
                setDataTypeFieldVisibility()
            }
            addTextChangedListener(DataFieldTextChangeListener())
        }

        dataTypeField.apply {
            adapter = dataTypesAdapter
            onItemSelectedListener = OnDataTypeSelectedListener()
        }

        dataDeleteButton.setOnClickListener {
            onDataDeleteButtonClicked?.invoke()
        }
    }

    private fun setDataField() {
        dataField.setText(dataValue)
    }

    private fun setDataTypeField() {
        dataTypesAdapter.apply {
            setNotifyOnChange(false)
            clear()

            // If this field's data type is a user custom type, add it as first entry.
            if (dataType.userCustomType) {
                add(dataType)
            }

            // Add all system data types.
            addAll(systemDataTypes)

            notifyDataSetChanged()
        }

        // Set the selected data type to this field.
        dataTypeField.setSelection(dataTypesAdapter.getPosition(dataType))

        setDataTypeFieldVisibility()
    }

    // Hide the dataTypeField if the dataField is empty and not focused.
    private fun setDataTypeFieldVisibility() {
        dataTypeField.visibility =
            if (dataField.text.isEmpty() && !dataField.hasFocus()) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun setDataDeleteButton() {
        setDataDeleteButtonVisibility()
    }

    private fun setDataDeleteButtonVisibility() {
        dataDeleteButton.visibility = if (dataField.text.isNullOrEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

    }

    private fun showCreateUserCustomTypeInputPrompt() {
        CustomLabelInputDialog(context)
            .show(R.string.contacts_ui_custom_label_input_dialog_title,
                onLabelEntered = { label ->
                    replaceUserCustomType(createUserCustomDataTypeWithLabel(label))
                }, onCancelled = {
                    // Revert the selection to the current data type.
                    dataTypeField.setSelection(dataTypesAdapter.getPosition(selectedType))
                })
    }

    private fun replaceUserCustomType(userCustomType: V) {
        dataTypesAdapter.apply {
            setNotifyOnChange(false)

            selectedType?.let {
                if (it.userCustomType) {
                    remove(it)
                }
            }

            insert(userCustomType, USER_CUSTOM_DATA_TYPE_INDEX)
            notifyDataSetChanged()
        }

        // Set the selected data type to this new custom data type.
        dataTypeField.setSelection(USER_CUSTOM_DATA_TYPE_INDEX)
    }

    private inner class DataFieldTextChangeListener : AbstractTextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setDataValue(s?.toString())

            if (s.isNullOrEmpty()) {
                onDataCleared?.invoke()
            }

            if (start == 0 && count > 0) {
                onDataBegin?.invoke()
            }

            setDataDeleteButtonVisibility()
        }
    }

    private inner class OnDataTypeSelectedListener : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val dataType = dataTypesAdapter.getItem(position) ?: return
            if (dataType.type.isCustomType) {
                // If generic custom type is selected, show an input prompt for creating a new
                // custom type label.
                if (!dataType.userCustomType) {
                    showCreateUserCustomTypeInputPrompt()
                } else {
                    // Else existing user custom type is selected.
                    selectedType = dataType
                }
            } else {
                // A generic, non custom type is selected. Set the data type to this and remove
                // user custom type, if exist.
                dataTypesAdapter.getItem(USER_CUSTOM_DATA_TYPE_INDEX)?.let { maybeUserCustomType ->
                    if (maybeUserCustomType.userCustomType) {
                        dataTypesAdapter.remove(maybeUserCustomType)
                    }
                }

                selectedType = dataType
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}

private const val USER_CUSTOM_DATA_TYPE_INDEX = 0