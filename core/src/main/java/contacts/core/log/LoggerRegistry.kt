package contacts.core.log

import contacts.core.CrudApi
import contacts.core.Redactable
import contacts.core.redactedCopyOrThis

class LoggerRegistry(logger: Logger) {

    internal val apiListener: CrudApi.Listener = Listener(logger)

    // Prevent consumers from invoking the listener functions by not having the registry implement
    // it directly.
    private class Listener(private val logger: Logger) : CrudApi.Listener {

        override fun onPreExecute(api: CrudApi) {
            logRedactable(api)
        }

        override fun onPostExecute(result: CrudApi.Result) {
            logRedactable(result)
        }

        private fun logRedactable(redactable: Redactable) {
            logger.log(redactable.redactedCopyOrThis(logger.redactMessages).toString())
        }
    }
}