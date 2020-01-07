package com.vestrel00.contacts.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.view.ViewGroup
import android.widget.EditText
import com.vestrel00.contacts.ui.R
import com.vestrel00.contacts.ui.text.AbstractTextWatcher

class CustomLabelInputDialog(private val context: Context) {

    private var alertDialog: AlertDialog? = null

    fun show(titleRes: Int, onLabelEntered: (label: String) -> Unit, onCancelled: () -> Unit) {
        show(context.getString(titleRes), onLabelEntered, onCancelled)
    }

    @Suppress("InflateParams")
    fun show(title: String, onLabelEntered: (label: String) -> Unit, onCancelled: () -> Unit) {
        alertDialog?.dismiss()

        val alertDialogBuilder = AlertDialog.Builder(context)
        // Use the alert dialog's themed context as per documentation.
        val customTypeEditText = EditText(alertDialogBuilder.context).apply {
            addTextChangedListener(object : AbstractTextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    setPositiveButtonEnabled(!s.isNullOrBlank())
                }
            })
        }

        alertDialog = alertDialogBuilder
            .setTitle(title)
            .setView(customTypeEditText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onLabelEntered(customTypeEditText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancelled()
            }
            .show()

        // Need to set margins after it has been added to the alert dialog. Any existing layout
        // params (in code or in xml) get dropped when setting the view in the alert dialog.
        (customTypeEditText.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val margin = context.resources
                .getDimensionPixelSize(R.dimen.custom_label_input_dialog_margin_horizontal)
            leftMargin = margin
            rightMargin = margin
        }

        setPositiveButtonEnabled(false)
    }

    private fun setPositiveButtonEnabled(enabled: Boolean) {
        alertDialog?.apply {
            val okButton = getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.isEnabled = enabled
        }
    }
}