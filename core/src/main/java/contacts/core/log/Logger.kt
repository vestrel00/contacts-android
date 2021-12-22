package contacts.core.log

interface Logger {
  fun log(message: String)
}

class EmptyLogger : Logger {
  override fun log(message: String) {}
}