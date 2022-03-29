# Installation guide

> This library is a multi-module project published with JitPack
> [![JitPack](https://jitpack.io/v/vestrel00/contacts-android.svg)](https://jitpack.io/#vestrel00/contacts-android)

First, include JitPack in the repositories list,

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

To install individual modules,

```groovy
dependencies {
    implementation 'com.github.vestrel00.contacts-android:core:<version>'
    
    implementation 'com.github.vestrel00.contacts-android:async:<version>'
    implementation 'com.github.vestrel00.contacts-android:customdata-gender:<version>'
    implementation 'com.github.vestrel00.contacts-android:customdata-googlecontacts:<version>'
    implementation 'com.github.vestrel00.contacts-android:customdata-handlename:<version>'
    implementation 'com.github.vestrel00.contacts-android:customdata-pokemon:<version>'
    implementation 'com.github.vestrel00.contacts-android:customdata-rpg:<version>'
    implementation 'com.github.vestrel00.contacts-android:debug:<version>'
    implementation 'com.github.vestrel00.contacts-android:permissions:<version>'
    implementation 'com.github.vestrel00.contacts-android:test:<version>'
    implementation 'com.github.vestrel00.contacts-android:ui:<version>'
    // Notice that when installing individual modules, the first ":" comes after "contacts-android".
}
```

**The `core` module is really all you need. All other modules are optional.**

It is recommended that you install individual modules to make sure that unused code is not included
in your application, which will increase your app's APK size.

If you still want to install all modules in a single line, read the **Installing all modules in one line** section below.

## Modules

Here is a brief description of the individual modules you can install.

- `core`: All of the contacts management APIs the library has to offer.
  _This is the only required module. All other modules are optional_.
- `async`: Extension functions for executing core API functions asynchronously using 
  [Kotlin Coroutines][coroutines].
- `permissions`: Extension functions for executing core API functions with permissions
  granted using [Kotlin Coroutines][coroutines].
- `test`: APIs for mocking core APIs during tests or at production runtime.
- `debug`: Extension functions for logging internal database tables into the Logcat and
  other debugging related stuff. _This is only meant for development use_.
- `ui`: Rudimentary UI views and functions that are already integrated with the core APIs.
  _You may use these for rapid prototyping or just for reference_.
- `customdata-gender`: Custom data for [gender](https://en.wikipedia.org/wiki/Gender).
- `customdata-googlecontacts`: Custom data managed by the [Google Contacts app](https://play.google.com/store/apps/details?id=com.google.android.contacts).
- `customdata-handlename`: Custom data for [handle](https://techterms.com/definition/handle).
- `customdata-pokemon`: Custom data for [pokemon](https://en.wikipedia.org/wiki/Pokémon).
- `customdata-rpg`: Custom data for [role playing games (RPG)](https://en.wikipedia.org/wiki/Role-playing_game).

## Installing all modules in one line

To install all modules in a single line,

```groovy
dependencies {
    implementation 'com.github.vestrel00:contacts-android:<version>'
    // Notice that when installing all modules, the first ":" comes after "vestrel00".
}
```

Starting with version `0.2.0`, installing all modules in a single line is only
supported when using the [`dependencyResolutionManagement` in `settings.gradle`](https://developer.android.com/studio/build/dependencies#remote-repositories).

In your `settings.gradle`,

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

For versions `0.1.10` and below, you can still install all modules in a single line using the old
common method of dependency resolution. 

In your root `build.gradle`,

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

[coroutines]: https://kotlinlang.org/docs/coroutines-overview.html