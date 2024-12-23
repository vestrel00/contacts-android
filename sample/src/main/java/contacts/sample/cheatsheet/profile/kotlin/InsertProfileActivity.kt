package contacts.sample.cheatsheet.profile.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.*
import contacts.core.profile.ProfileInsert
import contacts.core.util.*

class InsertProfileActivity : Activity() {

    fun insertProfile(): ProfileInsert.Result = Contacts(this)
        .profile()
        .insert()
        .rawContact {
            addAddress {
                street = "321 Xyz street"
                city = "Brooklyn"
                region = "New York"
                postcode = "11207"
                country = "US"
                type = AddressEntity.Type.WORK
            }
            addEmail {
                address = "321@xyz.com"
                type = EmailEntity.Type.WORK
            }
            addEvent {
                date = EventDate.from(1990, 0, 1)
                type = EventEntity.Type.BIRTHDAY
            }
            @Suppress("Deprecation")
            addIm {
                data = "im@aol.com"
                protocol = ImEntity.Protocol.CUSTOM
                customProtocol = "AOL"
            }
            setName {
                prefix = "Mr."
                givenName = "Small"
                middleName = "Goody"
                familyName = "Dog"
                suffix = "Jr"
            }
            setNickname {
                name = "TINY DOMESTICATED ANIMAL"
            }
            setNote {
                note = "This is one furry friend!"
            }
            setOrganization {
                company = "Good company"
                title = "Teammate"
                department = "The good one"
                jobDescription = "Be a good citizen"
                officeLocation = "It's public"
            }
            addPhone {
                number = "(888) 321-7654"
                type = PhoneEntity.Type.WORK
            }
            addRelation {
                name = "Bro"
                type = RelationEntity.Type.BROTHER
            }
            @Suppress("Deprecation")
            setSipAddress {
                sipAddress = "sip:user@domain:port"
            }
            addWebsite {
                url = "www.smalltinycompany.com"
            }
        }
        .commit()
}