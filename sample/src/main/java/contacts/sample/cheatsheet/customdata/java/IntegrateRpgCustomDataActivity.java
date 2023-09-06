package contacts.sample.cheatsheet.customdata.java;

import static contacts.core.WhereKt.*;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.data.*;
import contacts.core.entities.*;
import contacts.core.entities.custom.CustomDataRegistry;
import contacts.entities.custom.rpg.*;
import contacts.entities.custom.rpg.profession.*;
import contacts.entities.custom.rpg.stats.*;

public class IntegrateRpgCustomDataActivity extends Activity {

    Contacts contacts = ContactsFactory.create(
            this, false, new CustomDataRegistry().register(new RpgRegistration())
    );

    List<Contact> getContactsWithRpgCustomData() {
        return contacts
                .query()
                .where(or(isNotNull(RpgFields.Profession.Title), isNotNull(RpgFields.Stats.Level)))
                .find();
    }

    Insert.Result insertRawContactWithRpgCustomData() {
        NewRpgProfession newRpgProfession = new NewRpgProfession("Berserker");
        NewRpgStats newRpgStats = new NewRpgStats();
        newRpgStats.setLevel(78);
        newRpgStats.setSpeed(500);
        newRpgStats.setStrength(789);
        newRpgStats.setIntelligence(123);
        newRpgStats.setLuck(369);

        NewRawContact newRawContact = new NewRawContact();
        RawContactRpgProfessionKt.setRpgProfession(newRawContact, contacts, newRpgProfession);
        RawContactRpgStatsKt.setRpgStats(newRawContact, contacts, newRpgStats);

        return contacts
                .insert()
                .rawContacts(newRawContact)
                .commit();
    }

    Update.Result updateRawContactRpgCustomData(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        MutableRpgProfessionEntity mutableRpgProfession =
                RawContactRpgProfessionKt.rpgProfession(mutableRawContact, contacts);
        MutableRpgStatsEntity mutableRpgStats =
                RawContactRpgStatsKt.rpgStats(mutableRawContact, contacts);

        if (mutableRpgProfession != null) {
            mutableRpgProfession.setTitle("Mage");
        }
        if (mutableRpgStats != null) {
            mutableRpgStats.setSpeed(250);
            mutableRpgStats.setStrength(69);
            mutableRpgStats.setIntelligence(863);
        }

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result deleteRpgCustomDataFromRawContact(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactRpgProfessionKt.setRpgProfession(mutableRawContact, contacts, (MutableRpgProfession) null);
        RawContactRpgStatsKt.setRpgStats(mutableRawContact, contacts, (MutableRpgStats) null);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    List<RpgProfession> getAllRpgProfessions() {
        return RpgProfessionDataQueryKt.rpgProfession(contacts.data().query()).find();
    }

    List<RpgStats> getAllRpgStats() {
        return RpgStatsDataQueryKt.rpgStats(contacts.data().query()).find();
    }

    DataUpdate.Result updateRpgProfessionAndRpgStats(
            MutableRpgProfession rpgProfession, MutableRpgStats rpgStats
    ) {
        return contacts.data().update().data(rpgProfession, rpgStats).commit();
    }

    DataDelete.Result deleteRpgProfessionAndRpgStats(
            RpgProfession rpgProfession, RpgStats rpgStats
    ) {
        return contacts.data().delete().data(rpgProfession, rpgStats).commit();
    }
}