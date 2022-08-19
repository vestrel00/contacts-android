# Using limit and offset in queries

Most query APIs in this library provide `limit` and `offset` functions to allow you to retrieve only
some entities instead of everything.

To limit the amount of entities returned and/or offset (skip) a specified number of entities,

```kotlin
.limit(limit)
.offset(offset)
```

For example, to only get a maximum 20 entities, skipping the first 20,

```kotlin
.limit(20)
.offset(20)
```

This is useful for pagination =)

> ℹ️ It is recommended to limit the number of entities requested when querying to increase 
> performance and decrease memory cost.

## Some devices do not support limit and/or offset in queries

If the `limit` and `offset` functions are not supported by the device's database query operation, 
all entities will be returned. In such cases, the `Result.isLimitBreached` will be true if the 
number of entities returned exceed the `limit`.

> ℹ️ The `Result.isLimitBreached` is available since [version 0.2.3](https://github.com/vestrel00/contacts-android/releases/tag/0.2.3).

Setting `forceOffsetAndLimit(true)` will ensure that the `offset` and `limit will be applied after 
performing the internal database query, before returning the result to the caller (you).

> ℹ️ The `forceOffsetAndLimit` is available since [version 0.2.4](https://github.com/vestrel00/contacts-android/discussions/248).

This defaults to true in order to seamlessly support pagination. However, it is recommended to set 
this to false and handle such cases yourself to prevent performing more than one query for devices 
that do not support pagination.

For the full set of devices that do not support pagination, visit this discussion;
https://github.com/vestrel00/contacts-android/discussions/242#discussioncomment-3337613

### Limitation

If the number of entities found do not exceed the `limit` but an `offset` is provided, this is 
unable to detect/handle events where the `offset` is not supported. Sorry :P