package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import contacts.async.accounts.findWithContext
import contacts.core.Contacts
import contacts.core.entities.*
import contacts.core.util.setName
import contacts.core.util.setNickname
import contacts.core.util.setOrganization
import contacts.core.util.setSipAddress
import contacts.permissions.accounts.queryWithPermission
import contacts.sample.R
import contacts.sample.util.runIfExist
import contacts.ui.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A (vertical) [LinearLayout] that displays a [RawContactEntity] and handles the modifications to
 * the given [rawContact].
 *
 * Setting the [rawContact] will automatically update the views. Any modifications in the views will
 * also be made to the [rawContact] (only if it is mutable).
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
// TODO make this extendable for custom views after moving this to ui-async module
class RawContactView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    /**
     * The RawContact that is shown in this view. Setting this will automatically update the views.
     * Any modifications in the views will also be made to the this (only if it is mutable).
     */
    var rawContact: RawContactEntity = NewRawContact()
        private set

    /**
     * Sets the RawContact shown and managed by this view to the given [rawContact] and uses the
     * given [contacts] API to perform operations on it.
     */
    fun setRawContact(rawContact: RawContactEntity, contacts: Contacts) {
        this.rawContact = rawContact
        setRawContactView(contacts)
    }

    // Not using any view binding libraries or plugins just for this.
    private val accountView: AccountView
    private val photoThumbnailView: RawContactPhotoThumbnailView
    private val nameView: NameView
    private val nicknameView: NicknameView
    private val organizationView: OrganizationView
    private val phonesView: PhonesView
    private val sipAddressView: SipAddressView
    private val emailsView: EmailsView
    private val addressesView: AddressesView
    private val imsView: ImsView
    private val websiteView: WebsitesView
    private val eventsView: EventsView
    private val relationsView: RelationsView
    private val groupMembershipsView: GroupMembershipsView
    // TODO note

    private val accountRequiredViews: Set<View>

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_raw_contact, this)

        accountView = findViewById(R.id.account)
        photoThumbnailView = findViewById(R.id.photoThumbnail)
        nameView = findViewById(R.id.name)
        nicknameView = findViewById(R.id.nickname)
        organizationView = findViewById(R.id.organization)
        phonesView = findViewById(R.id.phones)
        sipAddressView = findViewById(R.id.sipAddress)
        emailsView = findViewById(R.id.emails)
        addressesView = findViewById(R.id.addresses)
        imsView = findViewById(R.id.ims)
        websiteView = findViewById(R.id.websites)
        eventsView = findViewById(R.id.events)
        relationsView = findViewById(R.id.relations)
        groupMembershipsView = findViewById(R.id.groupMemberships)

        accountRequiredViews = setOf(
            eventsView, relationsView, groupMembershipsView,
            findViewById(R.id.eventsLabel),
            findViewById(R.id.relationsLabel),
            findViewById(R.id.groupMembershipsLabel)
        )

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        photoThumbnailView.onActivityResult(requestCode, resultCode, data)
    }

    suspend fun savePhoto(contacts: Contacts): Boolean = photoThumbnailView.savePhoto(contacts)

    fun setAccountRequiredViews(contacts: Contacts) {
        launch {
            val account = rawContact.runIfExist {
                contacts.accounts(it.isProfile)
                    .queryWithPermission()
                    .associatedWith(it)
                    .findWithContext()
                    .firstOrNull()
            }

            // The native Contacts app hides these from the UI for local raw contacts. Let's follow
            // in the footsteps of the native Contacts app...
            if (account != null) {
                eventsView.dataList = rawContact.events.asMutableList()
                relationsView.dataList = rawContact.relations.asMutableList()
                groupMembershipsView.setMemberships(
                    rawContact.groupMemberships.asMutableList(),
                    contacts
                )
            } else {
                accountRequiredViews.forEach {
                    it.visibility = GONE
                }
            }
        }
    }

    private fun setRawContactView(contacts: Contacts) {
        val rawContact = rawContact

        photoThumbnailView.setRawContact(rawContact, contacts)
        accountView.setRawContact(rawContact, contacts)
        nameView.data = rawContact.name ?: NewName().also { newName ->
            when (rawContact) {
                is MutableRawContact -> rawContact.setName(newName)
                is NewRawContact -> rawContact.setName(newName)
                else -> {
                    // do nothing
                }
            }
        }
        nicknameView.data = rawContact.nickname ?: NewNickname().also { newNickname ->
            when (rawContact) {
                is MutableRawContact -> rawContact.setNickname(newNickname)
                is NewRawContact -> rawContact.setNickname(newNickname)
                else -> {
                    // do nothing
                }
            }
        }
        organizationView.data =
            rawContact.organization ?: NewOrganization().also { newOrganization ->
                when (rawContact) {
                    is MutableRawContact -> rawContact.setOrganization(newOrganization)
                    is NewRawContact -> rawContact.setOrganization(newOrganization)
                    else -> {
                        // do nothing
                    }
                }
            }
        phonesView.dataList = rawContact.phones.asMutableList()
        sipAddressView.data =
            rawContact.sipAddress ?: NewSipAddress().also { newSipAddress ->
                when (rawContact) {
                    is MutableRawContact -> rawContact.setSipAddress(newSipAddress)
                    is NewRawContact -> rawContact.setSipAddress(newSipAddress)
                    else -> {
                        // do nothing
                    }
                }
            }
        emailsView.dataList = rawContact.emails.asMutableList()
        addressesView.dataList = rawContact.addresses.asMutableList()
        imsView.dataList = rawContact.ims.asMutableList()
        websiteView.dataList = rawContact.websites.asMutableList()

        setAccountRequiredViews(contacts)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}