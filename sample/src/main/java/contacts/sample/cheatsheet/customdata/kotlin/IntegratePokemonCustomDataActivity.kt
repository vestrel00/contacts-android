package contacts.sample.cheatsheet.customdata.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.data.*
import contacts.core.entities.*
import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.pokemon.*

class IntegratePokemonCustomDataActivity : Activity() {

    val contacts = Contacts(this, false, CustomDataRegistry().register(PokemonRegistration()))

    fun getContactsWithPokemonCustomData(): List<Contact> = contacts
        .query()
        .where { PokemonFields.Name.isNotNull() or PokemonFields.PokeApiId.isNotNull() }
        .find()

    fun insertRawContactWithPokemonCustomData(): Insert.Result = contacts
        .insert()
        .rawContact {
            addPokemon(contacts) {
                name = "ditto"
                nickname = "copy-cat"
                level = 24
                pokeApiId = 132
            }
        }
        .commit()

    fun updateRawContactPokemonCustomData(rawContact: RawContact): Update.Result = contacts
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                pokemons(contacts).firstOrNull()?.apply {
                    nickname = "OP"
                    level = 99
                }
            }
        )
        .commit()

    fun deletePokemonCustomDataFromRawContact(rawContact: RawContact): Update.Result =
        contacts
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    removeAllPokemons(contacts)
                }
            )
            .commit()

    fun getAllPokemon(): List<Pokemon> = contacts.data().query().pokemons().find()

    fun updatePokemon(pokemon: MutablePokemon): DataUpdate.Result =
        contacts.data().update().data(pokemon).commit()

    fun deletePokemon(pokemon: Pokemon): DataDelete.Result =
        contacts.data().delete().data(pokemon).commit()
}