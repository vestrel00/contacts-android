# Integrate custom data from other apps

If you are looking to create and integrate your own custom data, read [Integrate custom data](/howto/integrate-custom-data.md).

If you are looking to integrate custom data from other apps, you are in the right place!

There are a lot of other apps out there that provide their own custom data, such as 
[Google Contacts][google-contacts] and [WhatsApp][whatsapp]. 

There are two parts to "integrating custom data";

1. Providing create (insert), read (query), update, and delete (CRUD) APIs for custom data
   associated with RawContacts.
2. Providing sync adapters to sync custom data across devices.

This library only handles the first part. Other (third party) apps typically provide sync adapters 
to sync their custom data across devices. This library does not interfere with any syncing 
functionality of custom data from other apps. What this library does is allow you and others to 
easily read and write custom data from other apps in your own apps.

## Research what custom data the third party app's are adding, if any

The hardest part will be researching what custom data a particular third party app provides, what 
they are used for, and how they behave. Here are some things you can do to find the answers.

1. Install and log into the app you are interested in researching. Then, use the `debug` module 
   functions in your app to log the Data table via `Context.logDataTable()`. Look for any mime types
   that look like like they belong to the app. Figure out how where in the app's UI the data is
   shown and/or how the app uses it in general.
2. Deconstruct the APK and look for `res/xml/contacts.xml` and other places in code where custom 
   data may reside. A good place to look will be in [sync adapter related classes][syncadapter].
3. Search the internet for any official documentation on the custom data added by the app. There is
   a high chance that this does not exist.
4. Search the internet for other people's research on the app's custom data, if any.

The first strategy is the most effective strategy to take because you are able to experience 
first-hand and play around with the custom data and document everything about it. Nothing beats 
first-hand research!

The second strategy is a bit more hacky and advanced and time consuming but it could pay off.

The third strategy is optimistic but could end up being the most useful if you are able to locate
official documentation from the app developers themselves.

The fourth strategy could be unreliable as it depends on other people's knowledge, which could be
inaccurate. 

## Integrate the third party app custom data with this library

Once you have figured out all of the details of all of the custom data (mimetypes) that the third
party app adds, you may proceed to write the code that will allow you and others to perform read
and write operations on it using the CRUD APIs provided in this library.

To proceed, read [Integrate custom data](/howto/integrate-custom-data.md).

## Example, [Google Contacts app][google-contacts] custom data

Issue [#165: Google Contacts app custom data](https://github.com/vestrel00/contacts-android/issues/165)
integrates custom data from the Google Contacts app into this library. You may use it as an example
on how to get started with the research and also what code to write after the research has been 
completed.

## Consider adding your integration of third party apps' custom data to this library

Let's say that you have written the code that integrates custom data from a third party app into 
your app using this library. That's great and all but your app will be the only app that will be 
able to use it! In the spirit of open source, please feel free to add your third party app custom 
data integration into this library so that other people using this library can optionally integrate 
it into their own apps! Please create a GitHub issue and file a pull request!

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts
[whatsapp]: https://play.google.com/store/apps/details?id=com.whatsapp
[syncadapter]: https://developer.android.com/training/sync-adapters/creating-sync-adapter