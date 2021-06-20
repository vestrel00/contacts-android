package contacts.sample

import android.content.Context
import contacts.Contacts
import contacts.async.commitInOneTransactionWithContext
import contacts.async.commitWithContext
import contacts.entities.*
import contacts.util.*
import java.util.*

// TODO Delete this file before releasing code to public!

suspend fun Context.insertContact(prefix: String = Random().nextLong().toString()) {
    Contacts(this).insert()
        .rawContact {
            addAddress {
                type = Address.Type.HOME
                street = "$prefix 123 abc street"
                poBox = "$prefix 987"
                neighborhood = "$prefix Triangle"
                city = "$prefix Austin"
                region = "$prefix Travis"
                postcode = "$prefix 78758"
                country = "$prefix US"
            }
            addAddress {
                type = Address.Type.CUSTOM
                label = "$prefix Hangout"
                formattedAddress = "$prefix xyz hangout place"
            }

            addEmail {
                type = Email.Type.HOME
                address = "${prefix}@123.xyz"
            }
            addEmail {
                type = Email.Type.CUSTOM
                label = "$prefix Emergency"
                address = "${prefix}@123.emergency"
            }

            addEvent {
                type = Event.Type.BIRTHDAY
                date = Date()
            }
            addEvent {
                type = Event.Type.CUSTOM
                label = "$prefix Graduation"
                date = Date()
            }

            addIm {
                protocol = Im.Protocol.AIM
                data = "$prefix lol@aim.com"
            }
            addIm {
                protocol = Im.Protocol.CUSTOM
                customProtocol = "$prefix HOALW"
                data = "$prefix hoalw@king.com"
            }

            setName {
                givenName = "$prefix John"
                middleName = "$prefix Anonymous"
                familyName = "$prefix Doe"
                this.prefix = "Mr."
                suffix = "Jr."
            }

            setNickname {
                name = "$prefix Unnoticed"
            }

            setNote {
                note = "$prefix This dude is invisible."
            }

            setOrganization {
                company = "$prefix Abc"
                title = "$prefix Grunt"
                department = "$prefix Human resources"
                jobDescription = "$prefix Talk to people"
                officeLocation = "$prefix Downtown"
            }

            addPhone {
                type = Phone.Type.HOME
                number = "(555) 555-5555"
            }
            addPhone {
                type = Phone.Type.CUSTOM
                label = "$prefix Emergency"
                number = "(999) 999-9999"
            }

            addRelation {
                type = Relation.Type.FRIEND
                name = "$prefix Imaginary Joe"
            }
            addRelation {
                type = Relation.Type.CUSTOM
                label = "$prefix Underling"
                name = "$prefix Him self"
            }

            setSipAddress {
                sipAddress = "$prefix 123 Sip address"
            }

            addWebsite {
                url = "www.${prefix}example.com"
            }
            addWebsite {
                url = "www.${prefix}example2.com"
            }
        }
        .commitWithContext()
}

suspend fun Context.deleteAllContacts(contacts: List<Contact>) {
    Contacts(this).delete()
        .contacts(contacts)
        .commitInOneTransactionWithContext()
}