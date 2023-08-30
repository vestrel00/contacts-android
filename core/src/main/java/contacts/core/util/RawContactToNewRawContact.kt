package contacts.core.util

import contacts.core.entities.Address
import contacts.core.entities.Email
import contacts.core.entities.Event
import contacts.core.entities.GroupMembership
import contacts.core.entities.Im
import contacts.core.entities.Name
import contacts.core.entities.NewAddress
import contacts.core.entities.NewEmail
import contacts.core.entities.NewEvent
import contacts.core.entities.NewGroupMembership
import contacts.core.entities.NewIm
import contacts.core.entities.NewName
import contacts.core.entities.NewNickname
import contacts.core.entities.NewNote
import contacts.core.entities.NewOptions
import contacts.core.entities.NewOrganization
import contacts.core.entities.NewPhone
import contacts.core.entities.NewRawContact
import contacts.core.entities.NewRelation
import contacts.core.entities.NewSipAddress
import contacts.core.entities.NewWebsite
import contacts.core.entities.Nickname
import contacts.core.entities.Note
import contacts.core.entities.Options
import contacts.core.entities.Organization
import contacts.core.entities.Phone
import contacts.core.entities.RawContact
import contacts.core.entities.Relation
import contacts.core.entities.SipAddress
import contacts.core.entities.Website

// TODO? Add newCopy function to Entity interfaces and expose it to library users?
// It would allow library users to easily insert copies of entities into the database.

internal fun RawContact.newCopy(block: NewRawContact.() -> Unit) = newCopy().apply(block)

private fun RawContact.newCopy() = NewRawContact(
    account = account,
    // Intentionally not copying the source ID because that should only be set by the Account's
    // sync adapter.

    addresses = addresses.asSequence().map { it.newCopy() }.toMutableList(),
    emails = emails.asSequence().map { it.newCopy() }.toMutableList(),
    events = events.asSequence().map { it.newCopy() }.toMutableList(),
    groupMemberships = groupMemberships.asSequence().map { it.newCopy() }.toMutableList(),
    ims = ims.asSequence().map { it.newCopy() }.toMutableList(),
    name = name?.newCopy(),
    nickname = nickname?.newCopy(),
    note = note?.newCopy(),
    options = options?.newCopy(),
    organization = organization?.newCopy(),
    phones = phones.asSequence().map { it.newCopy() }.toMutableList(),
    photo = photo,
    relations = relations.asSequence().map { it.newCopy() }.toMutableList(),
    sipAddress = sipAddress?.newCopy(),
    websites = websites.asSequence().map { it.newCopy() }.toMutableList(),

    customDataEntities = customDataEntities
        .mapValues { it.value.mutableCopy() }
        .toMutableMap(),

    isRedacted = isRedacted
)

private fun Website.newCopy() = NewWebsite(
    url = url,

    isRedacted = isRedacted
)

private fun SipAddress.newCopy() = NewSipAddress(
    sipAddress = sipAddress,

    isRedacted = isRedacted
)

private fun Relation.newCopy() = NewRelation(
    type = type,
    label = label,

    name = name,

    isRedacted = isRedacted
)

private fun Phone.newCopy() = NewPhone(
    type = type,
    label = label,

    number = number,
    normalizedNumber = normalizedNumber,

    isRedacted = isRedacted
)

private fun Organization.newCopy() = NewOrganization(
    company = company,
    title = title,
    department = department,
    jobDescription = jobDescription,
    officeLocation = officeLocation,
    symbol = symbol,
    phoneticName = phoneticName,

    isRedacted = isRedacted
)

private fun Options.newCopy() = NewOptions(
    starred = starred,
    customRingtone = customRingtone,
    sendToVoicemail = sendToVoicemail,

    isRedacted = isRedacted
)

private fun Note.newCopy() = NewNote(
    note = note,

    isRedacted = isRedacted
)

private fun Nickname.newCopy() = NewNickname(
    name = name,

    isRedacted = isRedacted
)

private fun Name.newCopy() = NewName(
    displayName = displayName,

    givenName = givenName,
    middleName = middleName,
    familyName = familyName,

    prefix = prefix,
    suffix = suffix,

    phoneticGivenName = phoneticGivenName,
    phoneticMiddleName = phoneticMiddleName,
    phoneticFamilyName = phoneticFamilyName,

    isRedacted = isRedacted
)

private fun Im.newCopy() = NewIm(
    protocol = protocol,
    customProtocol = customProtocol,

    data = data,

    isRedacted = isRedacted
)

private fun GroupMembership.newCopy() = NewGroupMembership(
    groupId = groupId,

    isRedacted = isRedacted
)

private fun Event.newCopy() = NewEvent(
    type = type,
    label = label,

    date = date,

    isRedacted = isRedacted
)

private fun Email.newCopy() = NewEmail(
    type = type,
    label = label,

    address = address,

    isRedacted = isRedacted
)

private fun Address.newCopy() = NewAddress(
    type = type,
    label = label,

    formattedAddress = formattedAddress,
    street = street,
    poBox = poBox,
    neighborhood = neighborhood,
    city = city,
    region = region,
    postcode = postcode,
    country = country,

    isRedacted = isRedacted
)