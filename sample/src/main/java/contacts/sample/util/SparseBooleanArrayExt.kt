package contacts.sample.util

import android.util.SparseBooleanArray

val SparseBooleanArray.trueKeys: List<Int>
    get() = mutableListOf<Int>().apply {
        for (i in 0 until size()) {
            val key = keyAt(i)
            val isKeyValueTrue = valueAt(i)

            if (isKeyValueTrue) {
                add(key)
            }
        }
    }