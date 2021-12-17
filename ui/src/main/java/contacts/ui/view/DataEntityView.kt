package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import contacts.core.entities.DataEntity
import contacts.core.entities.MutableDataEntity
import contacts.ui.R
import contacts.ui.text.AbstractTextWatcher

/**
 * A [RelativeLayout] that displays a [DataEntity] and handles the modifications to it (if it is
 * mutable).
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
open class DataEntityView<E : DataEntity> @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    layoutRes: Int = R.layout.view_data_entity,
    dataFieldInputType: Int? = null,
    dataFieldHintResId: Int? = null,
    dataFieldIsFocusable: Boolean = true,
    private val dataDeleteButtonIsVisible: Boolean = true
) : RelativeLayout(context, attributeSet, defStyleAttr) {

    /**
     * The data entity that is shown in this view. Changing this will automatically update the views
     * and vice versa.
     *
     * This must be set after view creation.
     */
    var data: E? = null
        set(value) {
            field = value
            onDataSet()
        }

    private var eventListener: EventListener? = null

    private val dataField: EditText
    private val dataDeleteButton: View?

    init {
        inflate(context, layoutRes, this)

        dataField = findViewById(R.id.dataField)
        dataDeleteButton = findViewById(R.id.dataDeleteButton)

        dataField.apply {
            dataFieldInputType?.let(::setInputType)
            dataFieldHintResId?.let(::setHint)
            setOnClickListener { onDataFieldClicked() }
            addTextChangedListener(DataFieldTextChangeListener())
            isFocusable = dataFieldIsFocusable
        }

        dataDeleteButton?.setOnClickListener {
            eventListener?.onDataDeleteButtonClicked()
        }
    }

    fun setEventListener(eventListener: EventListener?) {
        this.eventListener = eventListener
    }

    protected open fun onDataFieldClicked() {
        // Optional override for subclasses
    }

    // [ANDROID X] @CallSuper (not using annotation to avoid dependency on androidx.annotation)
    protected open fun onDataSet() {
        setDataField()
        setDataDeleteButton()
    }

    protected fun setDataField() {
        dataField.setText(data?.primaryValue)
    }

    private fun setDataDeleteButton() {
        setDataDeleteButtonVisibility()
    }

    private fun setDataDeleteButtonVisibility() {
        dataDeleteButton?.visibility =
            if (!dataDeleteButtonIsVisible || dataField.text.isNullOrEmpty()) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
    }

    private inner class DataFieldTextChangeListener : AbstractTextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            val data = data // reassignment required for null-check and casting successfully.
            if (data != null && data is MutableDataEntity) {
                data.primaryValue = s?.toString()
            }

            if (s.isNullOrEmpty()) {
                eventListener?.onDataCleared()
            }

            if (start == 0 && before == 0 && count > 0) {
                eventListener?.onDataBegin()
            }

            setDataDeleteButtonVisibility()
        }
    }

    interface EventListener {
        /**
         * Invoked when the delete button is clicked.
         */
        fun onDataDeleteButtonClicked()

        /**
         * Invoked when the data field is cleared.
         */
        fun onDataCleared()

        /**
         * Invoked when the a piece of the data field is entered from a blank state.
         */
        fun onDataBegin()
    }

    interface Factory<E : DataEntity, V : DataEntityView<E>> {
        fun create(context: Context): V
    }
}