package contacts.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import contacts.core.entities.BlockedNumber
import contacts.ui.R
import java.util.*

/**
 * A (vertical) [LinearLayout] that displays a list of [BlockedNumber]s.
 *
 * This is not an actual ListView. It **does not implement any optimizations** like view recycling.
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of native Contacts app.
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
class BlockedNumbersView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    init {
        orientation = VERTICAL
    }

    /**
     * Removes all currently shown blocked numbers, if any. Then, shows the given [blockedNumbers].
     *
     * If the [blockedNumbers] is empty, "No blocked numbers" is shown. If it is null,
     * "Unable to show blocked numbers".
     */
    fun set(blockedNumbers: List<BlockedNumber>?) {
        removeAllViews()

        val headerText = if (blockedNumbers == null) {
            R.string.contacts_ui_blocked_numbers_unavailable
        } else if (blockedNumbers.isEmpty()) {
            R.string.contacts_ui_blocked_numbers_empty
        } else {

            R.string.contacts_ui_blocked_numbers
        }

        addView(TextView(context).apply { setText(headerText) })

        blockedNumbers?.forEach {
            addView(BlockedNumberView(context, it))
        }
    }
}

@SuppressLint("ViewConstructor")
private class BlockedNumberView(context: Context, blockedNumber: BlockedNumber) :
    RelativeLayout(context) {

    init {
        inflate(context, R.layout.view_blocked_number, this)

        findViewById<TextView>(R.id.number).text =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                blockedNumber.number
            } else {
                PhoneNumberUtils.formatNumber(
                    blockedNumber.number,
                    blockedNumber.normalizedNumber,
                    Locale.getDefault().country
                )
            }

        findViewById<ImageView>(R.id.delete).setOnClickListener {
            // TODO
        }
    }
}