# Contributing

There are only a few loose guidelines to follow. Read the **Setup** and **Guidelines** sections.

## Setup

To open, build, and run this project, you will need to use 
[Android Studio Meerkat Feature Drop | 2024.3.2][android-studio] and later versions.

If you run into any build issues, open up Android Studio preferences and make sure the following is 
set correctly...

- Build, Execution, Deployment -> Build Tools -> Gradle
   - Use Gradle from: 'gradlew-wrapper.properties' file

Restart Android Studio, clean, build, and invalidate caches & restart.

## Guidelines

1. Simple is better.
    - Over-engineering is not welcome here. Don't over complicate function implementations
      unnecessarily, especially the public API.
2. Less is more.
    - If you have not noticed yet, the dependency list of this project is almost non-existent. The
      core module only depends on Kotlin's standard library. Not even the support annotations are
      included (though this is questionable and may change quickly). All modules only have
      dependencies on essentials. Nice-to-haves are excluded.
    - Contacts have been here since API 1. In that spirit, we should not need to import tons of
      unnecessary dependencies to deliver the most basic Android API.
3. Java compatibility is a must.
    - Java is not dead even in Android, though it may seem like it. There are still probably a lot of
      people that have not migrated over to Kotlin. This is especially true for larger organizations
      with large code bases and unable to afford migrating to Kotlin. The API must be usable in Java,
      with exceptions to Kotlin-specific modules (e.g. async, permissions).
4. Be patient.
    - Early on, I (Vandolf) will be the only one to approve incoming code. It may take a few days for
      me to review code and decline/approve. I have a full time job after all =) As time passes, I'm
      hoping to give the power of approvals to others in the community.
5. Uphold the spirit of Contacts, Reborn!
    - Don't deviate from the existing API design. New code should follow existing API design to
      promote uniformity. It'll be easier to maintain and cross-pollinate.
      
[android-studio]: https://developer.android.com/studio