package contacts.core.entities.custom

/**
 * Defines up to how many entities of a certain kind is allowable **per RawContact**.
 */
enum class CustomDataCountRestriction {
    /**
     * A **RawContact** may have 0 or 1 one of this kind of entity. For example;
     *
     * - Name
     * - Nickname
     * - Note
     * - Organization
     * - Photo
     * - SipAddress
     */
    AT_MOST_ONE,

    /**
     * A **RawContact** may 0, 1, or more of this kind of entity. For example;
     *
     * - Address
     * - Email
     * - Event
     * - GroupMembership
     * - Im
     * - Phone
     * - Relation
     * - Website
     */
    NO_LIMIT
}