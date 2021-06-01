package contacts.sample

import android.content.Context
import contacts.Contacts
import contacts.async.commitInOneTransactionWithContext
import contacts.async.commitWithContext
import contacts.entities.*
import contacts.util.*
import java.util.*

// TODO Delete this file before releasing code to public!

suspend fun Context.insertContact() {
    Contacts(this).insert()
        .rawContact {
            addAddress {
                type = Address.Type.HOME
                street = "123 abc street"
                poBox = "987"
                neighborhood = "Triangle"
                city = "Austin"
                region = "Travis"
                postcode = "78758"
                country = "US"
            }
            addAddress {
                type = Address.Type.CUSTOM
                label = "Hangout"
                formattedAddress = "xyz hangout place"
            }

            addEmail {
                type = Email.Type.HOME
                address = "abc@123.xyz"
            }
            addEmail {
                type = Email.Type.CUSTOM
                label = "Emergency"
                address = "abc@123.emergency"
            }

            addEvent {
                type = Event.Type.BIRTHDAY
                date = Date()
            }
            addEvent {
                type = Event.Type.CUSTOM
                label = "Graduation"
                date = Date()
            }

            addIm {
                protocol = Im.Protocol.AIM
                data = "lol@aim.com"
            }
            addIm {
                protocol = Im.Protocol.CUSTOM
                customProtocol = "HOALW"
                data = "hoalw@king.com"
            }

            setName {
                givenName = "John"
                middleName = "Anonymous"
                familyName = "Doe"
                prefix = "Mr."
                suffix = "Jr."
            }

            setNickname {
                name = "Unnoticed"
            }

            setNote {
                note = "This dude is invisible."
            }

            setOrganization {
                company = "Abc"
                title = "Grunt"
                department = "Human resources"
                jobDescription = "Talk to people"
                officeLocation = "Downtown"
            }

            addPhone {
                type = Phone.Type.HOME
                number = "(555) 555-5555"
            }
            addPhone {
                type = Phone.Type.CUSTOM
                label = "Emergency"
                number = "(999) 999-9999"
            }

            addRelation {
                type = Relation.Type.FRIEND
                name = "Imaginary Joe"
            }
            addRelation {
                type = Relation.Type.CUSTOM
                label = "Underling"
                name = "Him self"
            }

            setSipAddress {
                sipAddress = "123 Sip address"
            }

            addWebsite {
                url = "www.example.com"
            }
            addWebsite {
                url = "www.example2.com"
            }
        }
        .commitWithContext()
}

suspend fun Context.deleteAllContacts(contacts: List<Contact>) {
    Contacts(this).delete()
        .contacts(contacts)
        .commitInOneTransactionWithContext()
}