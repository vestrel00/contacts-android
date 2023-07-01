package contacts.sample.util

import android.content.Intent
import android.os.Build
import android.os.Parcelable

inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("Deprecation")
        getParcelableExtra(name)
    }

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(name: String): ArrayList<T>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(name, T::class.java)
    } else {
        @Suppress("Deprecation")
        getParcelableArrayListExtra(name)
    }