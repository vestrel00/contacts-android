package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import contacts.accounts.Accounts
import contacts.async.accounts.accountForWithContext
import contacts.entities.*
import contacts.permissions.accounts.queryWithPermission
import contacts.sample.R
import contacts.ui.view.*
import contacts.util.setName
import contacts.util.setNickname
import contacts.util.setOrganization
import contacts.util.setSipAddress
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A (vertical) [LinearLayout] that displays a [MutableRawContact] and handles the modifications to
 * the given [rawContact].
 *
 * Setting the [rawContact] will automatically update the views. Any modifications in the views will
 * also be made to the [rawContact].
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
     * Any modifications in the views will also be made to the this.
     */
    var rawContact: MutableRawContact = MutableRawContact()
        set(value) {
            field = value

            setRawContactView()
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

    suspend fun savePhoto(): Boolean = photoThumbnailView.savePhoto()

    private fun setRawContactView() {
        photoThumbnailView.rawContact = rawContact
        accountView.rawContact = rawContact
        nameView.data = rawContact.name ?: MutableName().apply(rawContact::setName)
        nicknameView.data = rawContact.nickname ?: MutableNickname().apply(rawContact::setNickname)
        organizationView.data =
            rawContact.organization ?: MutableOrganization().apply(rawContact::setOrganization)
        phonesView.dataList = rawContact.phones
        sipAddressView.data =
            rawContact.sipAddress ?: MutableSipAddress().apply(rawContact::setSipAddress)
        emailsView.dataList = rawContact.emails
        addressesView.dataList = rawContact.addresses
        imsView.dataList = rawContact.ims
        websiteView.dataList = rawContact.websites

        launch {
            val account = Accounts(context, rawContact.isProfile)
                .queryWithPermission()
                .accountForWithContext(rawContact)

            // The native Contacts app hides these from the UI for local raw contacts. Let's follow
            // in the footsteps of the native Contacts app...
            if (account != null) {
                eventsView.dataList = rawContact.events
                relationsView.dataList = rawContact.relations
                groupMembershipsView.memberships = rawContact.groupMemberships
            } else {
                accountRequiredViews.forEach {
                    it.visibility = GONE
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}