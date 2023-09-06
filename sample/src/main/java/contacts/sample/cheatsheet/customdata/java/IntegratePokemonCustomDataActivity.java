package contacts.sample.cheatsheet.customdata.java;

import static contacts.core.WhereKt.*;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.data.*;
import contacts.core.entities.*;
import contacts.core.entities.custom.CustomDataRegistry;
import contacts.entities.custom.pokemon.*;

public class IntegratePokemonCustomDataActivity extends Activity {

    Contacts contacts = ContactsFactory.create(
            this, false, new CustomDataRegistry().register(new PokemonRegistration())
    );

    List<Contact> getContactsWithPokemonCustomData() {
        return contacts
                .query()
                .where(or(isNotNull(PokemonFields.Name), isNotNull(PokemonFields.PokeApiId)))
                .find();
    }

    Insert.Result insertRawContactWithPokemonCustomData() {
        NewPokemon newPokemon = new NewPokemon();
        newPokemon.setName("ditto");
        newPokemon.setNickname("copy-cat");
        newPokemon.setLevel(24);
        newPokemon.setPokeApiId(132);

        NewRawContact newRawContact = new NewRawContact();
        RawContactPokemonKt.addPokemon(newRawContact, contacts, newPokemon);

        return contacts
                .insert()
                .rawContacts(newRawContact)
                .commit();
    }

    Update.Result updateRawContactPokemonCustomData(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        MutablePokemonEntity mutablePokemon =
                RawContactPokemonKt.pokemonList(mutableRawContact, contacts).get(0);
        if (mutablePokemon != null) {
            mutablePokemon.setNickname("OP");
            mutablePokemon.setLevel(99);
        }

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result deletePokemonCustomDataFromRawContact(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactPokemonKt.removeAllPokemons(mutableRawContact, contacts);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    List<Pokemon> getAllPokemons() {
        return PokemonDataQueryKt.pokemons(contacts.data().query()).find();
    }

    DataUpdate.Result updatePokemon(MutablePokemon pokemon) {
        return contacts.data().update().data(pokemon).commit();
    }

    DataDelete.Result deletePokemon(Pokemon pokemon) {
        return contacts.data().delete().data(pokemon).commit();
    }
}