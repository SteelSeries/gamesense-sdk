# SteelSeries Game Event Support #

Most modern SteelSeries gaming devices have multiple zones of RGB illumination.  Several of our own and partnered keyboards offer individual control of illumination on each key.  A number of devices have OLED screens which can be used to display text or even arbitrary images.  And a few select devices offer the possibility of tactile feedback.

We've built an event framework that offers a common approach for indicating game state to the user by using any of these capabilites.  The event framework will work with games written in any language. All you need is to be able to create a JSON formatted string and POST it to a local URL. We've used it so far with languages as varied as C++, Java, Swift, Go, and Javascript.

## Engine plugins ##

Simultaneously with the release of SteelSeries Engine 3.7.0, we have released a plugin for the Unity Engine and editor that is available [here on Github][unity-plugin-repo] or on the Unity Web Store.  For documentation on binding and sending events using the plugin, refer to the documentation in its repository.

An Unreal Engine 4 plugin is also planned for future release.

One enthusiast also created an open source node-compatible implementation which is available on npm as `gamesense-client`, or [on Github][gamesense-client-repo].  This is an unofficial project not created by SteelSeries and is now inactive.  All credit goes to Christian Schuller.

## Server discovery ##

Interaction with the GameSense™ SDK is controlled through HTTP POST events to SteelSeries Engine.  Before you can use it, your game will need to find out the server address to which to send handlers and events.  To do this it will need to read the `coreProps.json` file that SteelSeries Engine creates when it starts. This file contains a small JSON object. You are interested in the `address` top level key. The corresponding value is the host and port that Engine is listening on. This is a string in `"host:port"` format. E.g.

```json
{
  "address": "127.0.0.1:51248"
}
```

This address value can then be used to create the URL used to post game handler specifications and events, by appending `"/game_event"`.

This file can be found in one of these locations, depending on OS:

**OSX**     | `/Library/Application Support/SteelSeries Engine 3/coreProps.json`

**Windows** | `%PROGRAMDATA%/SteelSeries/SteelSeries Engine 3/coreProps.json`

If this file is not present, then SteelSeries Engine 3 is not running and you should not attempt to send events.  Please note that if you are creating an app that starts on login, you may need to poll the location of this file in case your app starts up prior to SteelSeries Engine.

## Game Events ##

Games communicate with SteelSeries Engine 3 by posting a specifically formatted JSON object to Engine's endpoint.  The properties of this object specify the game it is coming from, the event it corresponds to, and a data payload including a `"value"` property with an arbitrary value that is used by the handler. For example:

```json
{
  "game": "MY_GAME",
  "event": "HEALTH",
  "data": {
      "value": 75
  }
}
```

Notes about the data:
* The values for `game` and `event` are limited to uppercase A-Z, 0-9, hyphen, and underscore characters.
* The same value for the `game` key should be used for all events and handlers within a single game.
* All three of the keys `game`, `event`, and `data` are mandatory for the event to be processed.
* The value for `data` can be either a JSON object or a string containing the stringified form of a JSON object.
* Inside `data`, the `value` key can be arbitrary data.  However, for both simplicity and greatest compatibility with user configurability in SteelSeries Engine, it is recommended that it be a numerical value.

The events must be sent as a POST request to the address `<SSE3 url>/game_event`, with a content type of `application/json`.

It is generally recommended to encapsulate the creation and POST of the JSON within a function that takes the event name and value to send, so that it can be re-used with each event type sent.

### Event context data ###

Some more complicated event handling may require access to more data than a simple numerical value.  This is supported in both the JSON and GoLisp APIs by adding an additional optional data object to the event payload.  The below example shows how this fits into the payload shown above:

```json
{
  "game": "MY_GAME",
  "event": "HEALTH",
  "data": {
      "value": 75,
      "frame": {
        "<arbitrary key>": "value"
      }
  }
}
```

* Inside `data`, the `frame` key is optional.  If present, it should be a JSON object containing key-value pairs with arbitrary data.  This data need not be a string as shown above, it can be of any type supported in JSON including basic types and arrays.  This contextual data can be accessed by any GoLisp handler or JSON screen handler.  See the appropriate documents for information on accessing this contextual data within handlers.

## Heartbeat/Keepalive Events ##

GameSense™ is initialized on devices when the first event for a game is recieved.  It is deactivated when no events have been received within its timeout period, which defaults to 15 seconds.  This means that your game should send at least one event every 15 seconds if you want the game state to continue to be fully represented on the user's devices.

An additional endpoint, `game_heartbeat`, is available to simplify this process.  The data payload sent to this endpoint only needs to include the name of the game:

```json
{
  "game": "MY_GAME"
}
```

This endpoint does not affect any state on the user devices, but resets the GameSense™ deactivation timer.  Use of this endpoint is completely optional, as you can also send real event data to keep GameSense™ alive.

# Registering a game #

Your game is automatically registered with SteelSeries Engine the system when you register (see below) or bind (see the handler documents) any events.  However, you can use another call to set various pieces of metadata.  Most notably, if you want users to see a user-friendly game name or developer name, you will need to POST that metadata to the URL `http://127.0.0.1:<port>/game_metadata`.  Making this call is optional, and each parameter except `game` is optional when making the call.

The optional parameters are as follows:
| JSON key                       | Value type           | Description       |
|--------------------------------|----------------------|-------------------|
| `game_display_name`            | string               | User-friendly name displayed in SSE.  If this is not set, your game will show up as the `game` string sent with your data |
| `developer`                    | string               | Developer name displayed underneath the game name in SSE.  This line is omitted in SSE if the metadata field is not set. |
| `deinitialize_timer_length_ms` | integer (1000-60000) | By default, SSE will return to default behavior when the `stop_game` call is made or when no events have been received for 15 seconds.  This can be used to customize that length of time between 1 and 60 seconds. |

If you send your game events with the game `"TEST_GAME"`, but you want to indicate to SteelSeries Engine that it should be displayed with the user-friendly name `My testing game` and a developer name of `My Game Studios`, you would POST the following JSON to `game_metadata` on startup.

```json
{
  "game": "TEST_GAME",
  "game_display_name": "My testing game",
  "developer": "My Game Studios"
}
```

# Registering an event #

Note: It is not necessary to both bind and register an event.  The difference is that event registration does not specify default (pre user customization) behavior for an event, whereas event binding does.  Event binding is described in detail in the handler documents.

You can register an event via sending POST data to the URL `http://127.0.0.1:<port>/register_game_event`.  The payload requires you to specify the game and event names, and can optionally contain minimum and maximum numeric values for the event, as well as an ID specifying what icon is displayed next to the event in the SteelSeries Engine UI.

If the adventure game wanted to indicate to SteelSeries Engine that you will be sending a health event with values between 0-100, and associate it with a health icon, it would POST the following JSON to `register_game_event` on startup.

```json
{
  "game": "ADVENTURE",
  "event": "HEALTH",
  "min_value": 0,
  "max_value": 100,
  "icon_id": 1,
  "value_optional": false
}
```

Only the "game" and "event" keys are required.  The other keys will be filled in with the following default values if omitted:
* Min value: 0
* Max value: 100
* Icon ID: 0 (No icon is displayed)
* Value optional: false

Game and event names are limited to the following characters: Uppercase A-Z, the digits 0-9, hyphen, and underscore.

For a list of available icons, see [Event icons][event-icons].

If the `value_optional` key is set to true for an event, the handlers for the event will be processed each time it is updated, even if a value key is not specified in the data or if the value key matches the previously cached value.  This is mainly useful for events that use context data rather than the event value to determine what to display, such as some OLED screen events or for `bitmap` type lighting events.

[gamesense-client-repo]: https://github.com/cschuller/gamesense-client
[unity-plugin-repo]: https://github.com/SteelSeries/unity-gamesense-client
