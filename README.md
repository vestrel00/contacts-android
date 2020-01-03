# Contacts

An easy way to insert, query, update, delete contacts in idiomatic Kotlin (and Java).

## Usage

TODO

## Best Practices

#### Do not use `copy` function of `Entity` classes.

All `Entity` classes such as `Name` and `Email` are `data class`es whose constructor are `internal`.
The constructors are internal in order to prevent consumers from setting internal, private, or
read-only properties, which lessens the risks of unwanted side effects when inserting, updating, or
deleting entities. However, Kotlin data classes have a `copy` function that allows consumers to set
any of the properties that are meant to be hidden even if the constructor or the properties are 
private.

Until Kotlin allows for hiding or disabling the `copy` function, the only thing this library can do
is document this and hope that consumers follow this practice. We are "consenting adults" =)
