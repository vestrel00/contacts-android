package contacts.core.log

import android.util.Log

interface Logger {
  val redactMessages: Boolean

  fun log(message: String)
}

class EmptyLogger : Logger {
  override val redactMessages: Boolean = false

  override fun log(message: String) {}
}

class AndroidLogger(
  private val tag: String = TAG,
  override val redactMessages: Boolean = true,
) : Logger {
  override fun log(message: String) {
    Log.d(tag, message)
  }

  companion object {
    private const val TAG = "ContactsDebug"
  }
}