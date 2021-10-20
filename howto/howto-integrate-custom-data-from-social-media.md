# How do I integrate custom data from social media?

Support for social media custom data is in the post v1.0.0 roadmap, though the community may 
implement it sooner if they contribute :D 

Anyways, this library plans to support custom data from;

- WhatsApp 
- Facebook
- Twitter
- (and more?)

I will need help from the community to implement these because I don't participate in social media. 
Use the `debug` module to look into the data table and look for social media mimetypes. Then, 
create a new custom data module using the existing `customdata-x` modules as reference. A separate 
module should be created for each social media. 

E.G. customdata-whatsapp, customdata-fb, customdata-twitter

For more info, read [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

## Note

This howto page is just a placeholder. It will be rewritten when custom data from social media has
been implemented.