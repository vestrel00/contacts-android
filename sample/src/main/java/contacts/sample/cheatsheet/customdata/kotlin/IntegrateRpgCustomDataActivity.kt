package contacts.sample.cheatsheet.customdata.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.data.*
import contacts.core.entities.*
import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.rpg.*
import contacts.entities.custom.rpg.profession.*
import contacts.entities.custom.rpg.stats.*

class IntegrateRpgCustomDataActivity : Activity() {

    val contacts = Contacts(this, false, CustomDataRegistry().register(RpgRegistration()))

    fun getContactsWithRpgCustomData(): List<Contact> = contacts
        .query()
        .where {
            RpgFields.Profession.Title.isNotNull() or RpgFields.Stats.Level.isNotNull()
        }
        .find()

    fun insertRawContactWithRpgCustomData(): Insert.Result = contacts
        .insert()
        .rawContact {
            setRpgProfession(contacts) {
                title = "Berserker"
            }
            setRpgStats(contacts) {
                level = 78
                speed = 500
                strength = 789
                intelligence = 123
                luck = 369
            }
        }
        .commit()

    fun updateRawContactRpgCustomData(rawContact: RawContact): Update.Result = contacts
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                rpgProfession(contacts)?.title = "Mage"
                rpgStats(contacts)?.apply {
                    speed = 250
                    strength = 69
                    intelligence = 863
                }
            }
        )
        .commit()

    fun deleteRpgCustomDataFromRawContact(rawContact: RawContact): Update.Result =
        contacts
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    setRpgProfession(contacts, null)
                    setRpgStats(contacts, null)
                }
            )
            .commit()

    fun getAllRpgProfessions(): List<RpgProfession> = contacts.data().query().rpgProfession().find()

    fun getAllRpgStats(): List<RpgStats> = contacts.data().query().rpgStats().find()

    fun updateRpgProfessionAndStats(
        profession: RpgProfession, rpgStats: RpgStats
    ): DataUpdate.Result = contacts.data().update().data(profession, rpgStats).commit()

    fun deleteFileAsAndUserDefined(
        profession: RpgProfession, rpgStats: RpgStats
    ): DataDelete.Result = contacts.data().delete().data(profession, rpgStats).commit()
}