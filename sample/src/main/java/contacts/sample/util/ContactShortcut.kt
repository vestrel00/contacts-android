package contacts.sample.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.os.Build
import android.util.TypedValue
import android.widget.Toast
import contacts.core.entities.ExistingContactEntity
import contacts.sample.ContactDetailsActivity
import contacts.sample.ContactsActivity

/**
 * Creates a pinned shortcut to this [ExistingContactEntity], which results in a new icon in the
 * launcher (home screen).
 *
 * This will only work if all of the following are true;
 *
 * - API version is 26 or higher
 * - The shortcut does not yet exist
 * - The [ExistingContactEntity.lookupKey] is not null
 * - The [ExistingContactEntity.displayNamePrimary] is not null
 *
 * A [Toast] will be shown if any of the above requirements are not met.
 *
 * Tapping the pinned shortcut will open the [ContactDetailsActivity] to show full contact details.
 */
fun ExistingContactEntity.createPinnedShortcut(context: Context) {
    val lookupKey = lookupKey
    val displayName = displayNamePrimary

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Toast.makeText(
            context,
            "Creating pinned Contact shortcuts require API ${Build.VERSION_CODES.O}",
            Toast.LENGTH_SHORT
        ).show()
    } else if (lookupKey == null) {
        Toast.makeText(
            context,
            "Creating pinned Contact shortcuts require the Contact.lookupKey",
            Toast.LENGTH_SHORT
        ).show()
    } else if (displayName == null) {
        Toast.makeText(
            context,
            "Creating pinned Contact shortcuts require the Contact.displayNamePrimary",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        // Requires API 25+
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)

        // Requires API 26+. Could be reduced to 25+ if using ShortcutManagerCompat
        if (!shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(
                context,
                "Launcher does not support pinned shortcuts",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (shortcutManager.pinnedShortcuts.find { it.id == lookupKey } != null) {
            Toast.makeText(
                context,
                "Pinned Contact shortcut already exists",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val pinShortcutInfo = ShortcutInfo.Builder(context, lookupKey)
            .setShortLabel(displayName)
            .setIcon(Icon.createWithBitmap(createBitmapForCharacter(displayName.first())))
            .setIntents(
                arrayOf(
                    Intent(context, ContactsActivity::class.java)
                        .setAction(Intent.ACTION_VIEW),
                    ContactDetailsActivity.viewContactDetailsIntent(context, lookupKey)
                        .setAction(Intent.ACTION_VIEW)
                )
            )
            .build()

        shortcutManager.requestPinShortcut(pinShortcutInfo, null)
    }
}

private fun createBitmapForCharacter(char: Char): Bitmap {
    val size = 48.toPx()

    val paint = Paint().apply {
        textSize = size
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#0000FF")
    }

    return Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawText(char.toString(), it.width / 2f, it.height * 0.9f, paint)
    }
}

private fun Int.toPx(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    toFloat(),
    Resources.getSystem().displayMetrics
)