# How do I integrate custom data from other apps?

This library plans to support custom data from;

- Google Contacts
- WhatsApp 
- Facebook
- Twitter
- (and more?)

Use the `debug` module to look into the data table and look for mimetypes from other apps. Then, 
create a new custom data module using the existing `customdata-x` modules as reference. A separate 
module should be created for each custom data set from other apps. 

E.G. `customdata-whatsapp`, `customdata-fb`, `customdata-twitter`

For more info, read [How do I integrate custom data?](/contacts-android/howto/howto-integrate-custom-data.html)

## Note

This howto page is just a placeholder. It will be rewritten when custom data from other apps has
been implemented.
