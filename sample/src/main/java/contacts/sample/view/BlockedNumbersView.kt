package contacts.sample.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.InputType
import android.util.AttributeSet
import android.widget.*
import contacts.async.blockednumbers.findWithContext
import contacts.core.BlockedNumbersFields
import contacts.core.Contacts
import contacts.core.desc
import contacts.core.entities.BlockedNumber
import contacts.sample.R
import contacts.ui.text.AbstractTextWatcher
import java.util.*

/**
 * A (vertical) [LinearLayout] that displays a list of [BlockedNumber]s. This also handles
 * additions and deletions.
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
 *
 * This is in the sample and not in the contacts-ui module because it requires concurrency. We
 * should not add coroutines and contacts-async as dependencies to contacts-ui just for this.
 * Consumers may copy and paste this into their projects or if the community really wants it, we may
 * move this to a separate module (contacts-ui-async).
 */
class BlockedNumbersView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    private var eventListener: EventListener? = null

    init {
        orientation = VERTICAL
    }

    fun setEventListener(eventListener: EventListener?) {
        this.eventListener = eventListener
    }

    suspend fun loadBlockedNumbers(contacts: Contacts) {
        val blockedNumbers = if (contacts.blockedNumbers().privileges.canReadAndWrite()) {
            contacts.blockedNumbers()
                .query()
                // The builtin native Blocked numbers activity shows the most recently added first
                .orderBy(BlockedNumbersFields.Id.desc())
                .findWithContext()
        } else {
            null
        }
        setViews(blockedNumbers)
    }

    /**
     * Removes all currently shown blocked numbers, if any. Then, shows the given [blockedNumbers].
     *
     * If the [blockedNumbers] is empty, "No blocked numbers" is shown. If it is null,
     * "Unable to show blocked numbers".
     *
     * ## Developer notes
     *
     * No view recycling here. No ListView, RecyclerView, or LazyColumn in use. This is just for
     * demo purposes. Chill out.
     */
    private fun setViews(blockedNumbers: List<BlockedNumber>?) {
        removeAllViews()

        setupHeaderView(blockedNumbers)
        addBlockedNumberViews(blockedNumbers)
        if (blockedNumbers != null) {
            setupAddBlockedNumberViews()
        }
    }

    private fun setupHeaderView(blockedNumbers: List<BlockedNumber>?) {
        val headerText = if (blockedNumbers == null) {
            R.string.blocked_numbers_unavailable
        } else if (blockedNumbers.isEmpty()) {
            R.string.blocked_numbers_empty
        } else {
            R.string.blocked_numbers
        }

        addView(TextView(context).apply { setText(headerText) })

    }

    private fun addBlockedNumberViews(blockedNumbers: List<BlockedNumber>?) {
        blockedNumbers?.forEach {
            BlockedNumberView(context, it) { blockedNumber ->
                eventListener?.onUnblockNumber(blockedNumber)
            }.also(::addView)
        }
    }

    private fun setupAddBlockedNumberViews() {
        val addNewBlockedNumberField = EditText(context).apply {
            addView(this)
            setHint(R.string.blocked_numbers_add_hint)
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val addNewBlockedNumberButton = Button(context).apply {
            addView(this)
            setOnClickListener {
                addNewBlockedNumberField.text?.toString()?.let { numberToBlock ->
                    eventListener?.onBlockNumber(numberToBlock)
                }
            }
            setText(R.string.blocked_numbers_add)
            isEnabled = false
        }

        addNewBlockedNumberField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                addNewBlockedNumberButton.isEnabled = !s.isNullOrBlank()
            }
        })
    }

    interface EventListener {
        fun onBlockNumber(numberToBlock: String)
        fun onUnblockNumber(blockedNumberToUnblock: BlockedNumber)
    }
}

@SuppressLint("ViewConstructor")
private class BlockedNumberView(
    context: Context, blockedNumber: BlockedNumber,
    onDeleteButtonClicked: (BlockedNumber) -> Unit
) : RelativeLayout(context) {

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
            onDeleteButtonClicked(blockedNumber)
        }
    }
}