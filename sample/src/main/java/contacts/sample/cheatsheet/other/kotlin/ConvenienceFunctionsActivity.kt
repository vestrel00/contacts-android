package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.entities.*
import contacts.core.util.*

class ConvenienceFunctionsActivity : Activity() {

    fun getSetRawContactDataThroughContact(contact: Contact) {
        contact.mutableCopy().addEmail(NewEmail().apply {
            address = "test@email.com"
            type = EmailEntity.Type.WORK
        })
    }

    fun setDataUsingExtensions(mutableRawContact: MutableRawContact) {
        mutableRawContact.addEmail {
            address = "test@email.com"
            type = EmailEntity.Type.WORK
        }
    }

    fun getParentContactOfRawContact(rawContact: RawContact): Contact? =
        rawContact.contact(Contacts(this))

    fun getParentContactOfData(data: ExistingDataEntity): Contact? = data.contact(Contacts(this))

    fun getParentRawContactOfData(data: ExistingDataEntity): RawContact? =
        data.rawContact(Contacts(this))

    fun refreshContact(contact: Contact): Contact? = contact.refresh(Contacts(this))

    fun refreshRawContact(rawContact: RawContact): RawContact? = rawContact.refresh(Contacts(this))

    fun refreshData(data: ExistingDataEntity): ExistingDataEntity? = data.refresh(Contacts(this))

    fun isDataReadOnly(data: ExistingDataEntity): Boolean = data.isReadOnly(Contacts(this))

    fun isDataReadOnlyMap(data: Collection<ExistingDataEntity>): Map<Long, Boolean> =
        data.isReadOnlyMap(Contacts(this))

    fun sortContactsUsingDataTableFields(contacts: List<Contact>) =
        contacts.sortedWith(
            setOf(
                Fields.Contact.Options.Starred.desc(),
                Fields.Email.Type.asc()
            ).contactsComparator()
        )

    fun getGroupOfGroupMemberships(groupMemberships: List<GroupMembership>): List<Group> =
        groupMemberships.groups(Contacts(this))
}