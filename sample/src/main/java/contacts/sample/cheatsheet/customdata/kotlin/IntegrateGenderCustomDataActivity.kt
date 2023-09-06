package contacts.sample.cheatsheet.customdata.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.data.*
import contacts.core.entities.*
import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.gender.*

class IntegrateGenderCustomDataActivity : Activity() {

    val contacts = Contacts(this, false, CustomDataRegistry().register(GenderRegistration()))

    fun getContactsWithGenderCustomData(): List<Contact> = contacts
        .query()
        .where { GenderFields.Type.isNotNull() }
        .find()

    fun insertRawContactWithGenderCustomData(): Insert.Result = contacts
        .insert()
        .rawContact {
            setGender(contacts) {
                type = GenderEntity.Type.MALE
            }
        }
        .commit()

    fun updateRawContactGenderCustomData(rawContact: RawContact): Update.Result = contacts
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                gender(contacts)?.type = GenderEntity.Type.FEMALE
            }
        )
        .commit()

    fun deleteGenderCustomDataFromRawContact(rawContact: RawContact): Update.Result =
        contacts
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    setGender(contacts, null)
                }
            )
            .commit()

    fun getAllGender(): List<Gender> = contacts.data().query().genders().find()

    fun updateGender(gender: MutableGender): DataUpdate.Result =
        contacts.data().update().data(gender).commit()

    fun deleteGender(gender: Gender): DataDelete.Result =
        contacts.data().delete().data(gender).commit()
}