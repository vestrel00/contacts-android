package contacts.sample.cheatsheet.basics.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.Update
import contacts.core.entities.*
import contacts.core.util.*

class UpdateContactsActivity : Activity() {

    fun addEmail(contact: Contact): Update.Result =
        Contacts(this)
            .update()
            .contacts(contact.mutableCopy {
                addEmail {
                    address = "321@xyz.com"
                    type = EmailEntity.Type.CUSTOM
                    label = "Personal"
                }
            })
            .commit()

    fun addEmail(rawContact: RawContact): Update.Result =
        Contacts(this)
            .update()
            .rawContacts(rawContact.mutableCopy {
                addEmail {
                    address = "321@xyz.com"
                    type = EmailEntity.Type.CUSTOM
                    label = "Personal"
                }
            })
            .commit()

    fun addAnniversary(contact: Contact): Update.Result =
        Contacts(this)
            .update()
            .contacts(contact.mutableCopy {
                addEvent {
                    date = EventDate.from(2016, 6, 14)
                    type = EventEntity.Type.ANNIVERSARY
                }
            })
            .commit()

    fun setFullName(rawContact: RawContact): Update.Result =
        Contacts(this)
            .update()
            .rawContacts(rawContact.mutableCopy {
                setName {
                    prefix = "Mr."
                    givenName = "Small"
                    middleName = "Bald"
                    familyName = "Eagle"
                    suffix = "Sr"
                }
            })
            .commit()

    fun setGivenName(rawContact: RawContact): Update.Result =
        Contacts(this)
            .update()
            .rawContacts(rawContact.mutableCopy {
                name = (name ?: NewName()).also { it.givenName = "Greg" }
            })
            .commit()

    fun removeGmailEmails(contact: Contact): Update.Result =
        Contacts(this)
            .update()
            .contacts(contact.mutableCopy {
                emails()
                    .filter { it.address?.endsWith("@gmail.com", ignoreCase = true) == true }
                    .forEach { removeEmail(it) }
            })
            .commit()

    fun removeEmailsAndPhones(contact: Contact): Update.Result =
        Contacts(this)
            .update()
            .contacts(contact.mutableCopy {
                removeAllEmails()
                removeAllPhones()
            })
            .commit()
}