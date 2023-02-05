package contacts.ui.util

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.view.ViewGroup
import android.widget.EditText
import contacts.ui.R
import contacts.ui.text.AbstractTextWatcher

/**
 * Shows an [AlertDialog] with an [EditText] and a cancel and ok button. The ok button is enabled
 * only when the user input is not null or empty. Clicking the ok button invokes the onTextEntered
 * function parameter callback with the non-null and non-empty string.
 *
 * ## Note
 *
 * This does not support state retention (e.g. device rotation). The community may contribute to
 * this by implementing it.
 */
class UserInputDialog(private val context: Context) {

    private var alertDialog: AlertDialog? = null

    /**
     * Shows the user input dialog with the given [titleRes].
     *
     * The ok button is enabled only when the user input is not null or empty. Clicking the ok
     * button invokes the [onTextEntered] function parameter callback with the non-null and
     * non-empty string.
     */
    // [ANDROID X] @StringRes (not using annotation to avoid dependency on androidx.annotation)
    fun show(
        titleRes: Int,
        initialText: String? = null,
        onCancelled: () -> Unit = {},
        onTextEntered: (text: String) -> Unit
    ) {
        show(context.getString(titleRes), initialText, onCancelled, onTextEntered)
    }

    /**
     * See [show].
     */
    @Suppress("InflateParams")
    fun show(
        title: String,
        initialText: String? = null,
        onCancelled: () -> Unit = {},
        onTextEntered: (text: String) -> Unit
    ) {
        alertDialog?.dismiss()

        val alertDialogBuilder = AlertDialog.Builder(context)
        // Use the alert dialog's themed context as per documentation.
        val userInputEditText = EditText(alertDialogBuilder.context).apply {
            addTextChangedListener(object : AbstractTextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    setPositiveButtonEnabled(!s.isNullOrBlank())
                }
            })
        }

        if (!initialText.isNullOrBlank()) {
            userInputEditText.setText(initialText)
        }

        alertDialog = alertDialogBuilder
            .setTitle(title)
            .setView(userInputEditText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onTextEntered(userInputEditText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancelled()
            }
            .show()

        // Need to set margins after it has been added to the alert dialog. Any existing layout
        // params (in code or in xml) get dropped when setting the view in the alert dialog.
        (userInputEditText.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val margin = context.resources
                .getDimensionPixelSize(R.dimen.user_input_dialog_margin_horizontal)
            leftMargin = margin
            rightMargin = margin
        }

        setPositiveButtonEnabled(!initialText.isNullOrBlank())
    }

    private fun setPositiveButtonEnabled(enabled: Boolean) {
        alertDialog?.apply {
            val okButton = getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.isEnabled = enabled
        }
    }
}