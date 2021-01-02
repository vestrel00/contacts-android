package contacts.entities.custom

/**
 * Defines up to how many entities of a certain type is allowable **per RawContact**.
 */
enum class CustomDataCountRestriction {
    /**
     * A **RawContact** may have 0 or 1 one of this type of entity. For example;
     *
     * - Name
     * - Nickname
     * - Note
     * - Organization
     * - SipAddress
     */
    AT_MOST_ONE,

    /**
     * A **RawContact** may 0, 1, or more of this type of entity. For example;
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