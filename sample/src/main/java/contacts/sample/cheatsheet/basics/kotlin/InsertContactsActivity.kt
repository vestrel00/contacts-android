package contacts.sample.cheatsheet.basics.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts
import contacts.core.Insert
import contacts.core.entities.*
import contacts.core.util.*

class InsertContactsActivity : Activity() {

    fun insertContact(account: Account?, groupMembership: NewGroupMembership?): Insert.Result =
        Contacts(this)
            .insert()
            .forAccount(account)
            .rawContact {
                addAddress {
                    street = "123 Abc street"
                    city = "Brooklyn"
                    region = "New York"
                    postcode = "11207"
                    country = "US"
                    type = AddressEntity.Type.WORK
                }
                addEmail {
                    address = "123@abc.com"
                    type = EmailEntity.Type.WORK
                }
                addEvent {
                    date = EventDate.from(1990, 0, 1)
                    type = EventEntity.Type.BIRTHDAY
                }
                if (groupMembership != null) {
                    addGroupMembership(groupMembership)
                }
                addIm {
                    data = "im@aol.com"
                    protocol = ImEntity.Protocol.CUSTOM
                    customProtocol = "AOL"
                }
                setName {
                    prefix = "Mr."
                    givenName = "Big"
                    middleName = "Bad"
                    familyName = "Fox"
                    suffix = "Jr"
                }
                setNickname {
                    name = "BIG BAD FOX"
                }
                setNote {
                    note = "This is one big bad fox!"
                }
                setOrganization {
                    company = "Bad company"
                    title = "Boss"
                    department = "The bad one"
                    jobDescription = "Be a big bad boss"
                    officeLocation = "It's a secret"
                }
                addPhone {
                    number = "(888) 123-4567"
                    type = PhoneEntity.Type.WORK
                }
                addRelation {
                    name = "Bro"
                    type = RelationEntity.Type.BROTHER
                }
                setSipAddress {
                    sipAddress = "sip:user@domain:port"
                }
                addWebsite {
                    url = "www.bigbadfox.com"
                }
            }
            .commit()
}