package contacts.core.log

import contacts.core.CrudApi
import contacts.core.Redactable
import contacts.core.redactedCopyOrThis

class LoggerRegistry(logger: Logger) {

    internal val apiListener: CrudApi.Listener = Listener(logger)

    // Prevent consumers from invoking the listener functions by not having the registry implement
    // it directly.
    private class Listener(private val logger: Logger, private val apiExecutionStartTimeMillis: MutableMap<CrudApi, Long> = mutableMapOf()) : CrudApi.Listener {

        override fun onPreExecute(api: CrudApi) {
            apiExecutionStartTimeMillis[api] = System.currentTimeMillis()
            logger.log(api.redactedCopyOrThis(logger.redactMessages).toString())
        }

        override fun onPostExecute(api: CrudApi, result: CrudApi.Result) {
            val executionTime = apiExecutionStartTimeMillis.remove(api)

            logger.log(
                """
                   ${api.redactedCopyOrThis(logger.redactMessages)}
                   execution time: $executionTime
                """.trimIndent()
            )
        }
    }
}