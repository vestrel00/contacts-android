package contacts.core

import android.content.ContentResolver
import contacts.core.accounts.AccountsPermissions
import contacts.core.entities.custom.CustomDataRegistry

// ## Developer notes
//
// This should actually be an internal interface. It is public because public APIs implement this.
// Anyways, this enforces some level of uniformity for all CRUD APIs. However, we should try to
// keep the contract loose to allow for the most flexibility. Who knows? Consumers might find some
// use for this. There is no howto page for this until we can find an official use for it.

/**
 * All CRUD (Insert*, Query*, Update*, Delete*) APIs, accessible via a [Contacts], must implement
 * this interface.
 */
interface CrudApi : Redactable {

    /**
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts

    /**
     * The API core function result.
     */
    interface Result : Redactable

    /**
     * The API core function result.
     */
    interface QueryResultWithLimit : Result {
        /**
         * This is true if the number of entities found is exceeds the specified limit. This occurs
         * when the device does not support limits in queries. In such cases, pagination is  not
         * supported because all table rows have been returned.
         *
         * For a list of devices found not to support limits, visit this discussion page;
         * https://github.com/vestrel00/contacts-android/discussions/242
         */
        val isLimitBreached: Boolean
    }

    /**
     * Get notified about events within [CrudApi] instances.
     */
    interface Listener {
        /**
         * Invoked by the [api] before executing its core function (e.g. "find" or "commit").
         *
         * ## Thread Safety
         *
         * This is invoked on the same thread as the thread the core function is invoked, which is
         * determined by the consumer.
         */
        fun onPreExecute(api: CrudApi)

        /**
         * Invoked by the API after executing its core function (e.g. "find" or "commit") right
         * before returning the [result] to the caller.
         *
         * ## Thread Safety
         *
         * This is invoked on the same thread as the thread the core function is invoked, which is
         * determined by the consumer.
         */
        fun onPostExecute(api: CrudApi, result: Result)
    }
}

/**
 * Registry for all [CrudApi.Listener]s.
 *
 * This single instance of [CrudApi.Listener] per [Contacts] instance that will be used by all
 * [CrudApi] instances.
 */
class CrudApiListenerRegistry {

    private val listeners = mutableSetOf<CrudApi.Listener>()

    /**
     * Register a [listener] that will be notified about events on all CRUD APIs accessible via a
     * [Contacts] instance.
     *
     * ## A short lesson about memory leaks <3
     *
     * Make sure to [unregister] the [listener] to prevent leaks! You only need to do this if the
     * [listener] lifecycle is less than the lifecycle of the [Contacts] instance.
     *
     * For example, if your application is holding one instance of [Contacts] throughout the entire
     * lifecycle of your [android.app.Application] (a singleton) and the [listener] passed here is
     * (or contains a reference to) an `Activity`, `Fragment`, or `View`, then you must [unregister]
     * the  [listener] before your Activity, Fragment, or View is destroyed. Otherwise, the
     * reference to the Activity, Fragment, or View will remain in memory for as long as your
     * `Application` is alive because there is a reference to it via this registry!
     *
     * However, if the [listener] does not have a reference to a non-Application `Context` or it
     * is meant to have the same lifecycle as your [android.app.Application], then there is no need
     * to [unregister].
     */
    fun register(listener: CrudApi.Listener): CrudApiListenerRegistry = apply {
        listeners.add(listener)
    }

    /**
     * Removes the [listener] from the registry.
     *
     * This is important for preventing memory leaks! Read more about it in the [register] function!
     */
    fun unregister(listener: CrudApi.Listener): CrudApiListenerRegistry = apply {
        listeners.remove(listener)
    }

    internal fun onPreExecute(api: CrudApi) {
        listeners.forEach { it.onPreExecute(api) }
    }

    internal fun onPostExecute(api: CrudApi, result: CrudApi.Result) {
        listeners.forEach { it.onPostExecute(api, result) }
    }
}

// region Shortcuts

internal fun CrudApi.onPreExecute() {
    contactsApi.apiListenerRegistry.onPreExecute(this)
}

internal fun CrudApi.onPostExecute(contactsApi: Contacts, result: CrudApi.Result) {
    contactsApi.apiListenerRegistry.onPostExecute(this, result)
}

internal val CrudApi.permissions: ContactsPermissions
    get() = contactsApi.permissions

internal val CrudApi.accountsPermissions: AccountsPermissions
    get() = contactsApi.accountsPermissions

internal val CrudApi.contentResolver: ContentResolver
    get() = contactsApi.contentResolver

internal val CrudApi.customDataRegistry: CustomDataRegistry
    get() = contactsApi.customDataRegistry

// endregion