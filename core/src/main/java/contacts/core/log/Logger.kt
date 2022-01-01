package contacts.core.log

import android.util.Log

interface Logger {
  fun log(message: String)
}

class EmptyLogger : Logger {
  override fun log(message: String) {}
}

class AndroidLogger(private val tag: String = TAG) : Logger {
  override fun log(message: String) {
    Log.d(tag, message)
  }

  companion object {
    private const val TAG = "ContactsDebug"
  }
}