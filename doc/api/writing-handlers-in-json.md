# Writing Handlers in Json #

This document assumes that you have read the [Sending Events to the SteelSeries GameSense™ API][api doc]

The JSON API is designed to be a flexible and accessible form of the API.  Specifically formatted JSON can be used to create events and bind handlers that use a large subset of the total available functionality.  Handlers defined this way will also allow end users to override default handler bindings inside SteelSeries Engine to create the experience most suited to them.

For each event that you intend to support, you need to register it as described in the previous document, or bind handlers to it.  Registering an event will merely add it to the system and allow user customization of behavior, while binding handlers will also add default behavior.

# Binding an event #

Note: It is not necessary to both bind and register an event.  The difference is that event registration does not specify default (pre user customization) behavior for an event, whereas event binding does.

You can bind JSON handlers for an event via sending POST data to the URL `http://127.0.0.1:<port>/bind_game_event`.  The payload includes all of the same mandatory and optional keys as event registration, as well as one additional key `handlers`.  The `handlers` value is an array of handlers.  Each handler should be either a JSON object or the stringified representation of a JSON object that describes the handler and what device type it should be applied to.  There should be one handler for each device type to which you wish to apply default behavior.

For each handler, your JSON data describes:

* The device type to which you wish to apply the effect
* The zone effected
* The details for how the illumination, OLED screen, or tactile effect should behave

So if the adventure game wanted to provide its health event handler, it would POST the following JSON to `bind_game_event`.

```json
{
  "game": "ADVENTURE",
  "event": "HEALTH",
  "min_value": 0,
  "max_value": 100,
  "icon_id": 1,
  "handlers": [
    {
      "device-type": "keyboard",
      "zone": "function-keys",
      "color": {"gradient": {"zero": {"red": 255, "green": 0, "blue": 0},
                             "hundred": {"red": 0, "green": 255, "blue": 0}}},
      "mode": "percent"
    }
  ]
}
```

This handler will display the health (whose value is 0-100, inclusive) as a percentage bar graph (assuming the connected keyboard supports it, or just using the color otherwise) on the function keys, varying from green at full health down to red at 0 health.

As of SteelSeries Engine 3.7.0, there are three different types of handlers you can create, each of which is described in its own document:

* [Color handlers][color-handlers] for interacting with illumination of devices with RGB LED lighting
* [Tactile handlers][tactile-handlers] for providing tactile notifications that users can feel on supported devices
* [Screen handlers][screen-handlers] for providing text and image notifications on OLED/LCD screens on supported devices

# Removing an event #

As of SteelSeries Engine 3.5.0, you can remove an event you have registered via sending POST data to the URL `http://127.0.0.1:<port>/remove_game_event`.  The payload requires you to specify the game and event names.  To remove the event MY_EVENT from the game MY_GAME, POST the following data:

```json
{
  "game": "MY_GAME",
  "event": "MY_EVENT"
}
```

Removing an event also removes all bindings for the event.  However, if you have bound handlers via placing a GoLisp file on the client system and that file still exists, the event will be automatically re-registered and re-bound when the file is loaded.

Events that are built-in to SteelSeries Engine 3 by default cannot be removed.

# Removing a game #

As of SteelSeries Engine 3.5.0, you can remove a game you have registered via sending POST data to the URL `http://127.0.0.1:<port>/remove_game`.  The payload requires you to specify the game name.  To remove the game MY_GAME, POST the following data:

```json
{
  "game": "MY_GAME"
}
```

Removing a game also removes all events registered for the game, as well as all bindings for the events.However, if you have bound handlers via golisp and the file still exists, the game and events in the file will be automatically re-registered and re-bound when the file is loaded.

Games that are built-in to SteelSeries Engine 3 by default cannot be removed.

# Reference sections #

## Error handling ##

All successful requests to the JSON API will return an HTTP status code of 200.  As of SteelSeries Engine 3.5.0, unsuccessful requests will return an error code along with the error message describing the problem.

A request that has any problems with the parameters will return a 400 status code and one of the following errors:

0: "Game or event string not specified".  Most of the requests require specifying both the game and event in question.  This error is returned if one is missing.  This error will also be returned if the JSON sent to the endpoint is malformed and cannot be parsed.

1: "Game string not specified".  Same as the above, but for requests that require only the game name.

2: "Game or event string contains disallowed characters.  Allowed are upper-case A-Z, 0-9, hyphen, and underscore".  Game and event strings are limited to the characters described.  

3: "Game string contains disallowed characters.  Allowed are upper-case A-Z, 0-9, hyphen, and underscore".  Same as above, but for requests which only take the game name as a parameter.

4: "GameEvent data member is empty".  The `game_event` request requires a `data` member describing the data the event should use when calculating the effects to apply.

5: "Events for too many games have been registered recently, please try again later".  There are limited anti-spam measures implemented into the API to prevent malicious use.  This message will show up if one of them has been triggered.

6: "One or more handlers must be specified for binding".  This message is returned if the `bind_game_event` request is sent without the `handlers` key or if the array in the key is empty.

7: "That event for that game is reserved".  Some operations cannot be performed on events which are built-in to SteelSeries Engine 3.  This includes binding and removing the events.

8: "That game is reserved".  Same as above, but for requests which only take the game name.  This includes removing a game.

9: "That event is not registered".  This is returned when attempting to remove an event which does not exist.

10: "That game is not registered".  This is returned when attempting to remove a game which does not exist.

A request that returns a 500 status code indicates an error internal to SteelSeries Engine.  If you wish to submit information about requests which caused these errors, you can file a support ticket on our website.

## Error logging ##

If an error is encountered within a handler while an event is being processed, the stack trace for the error will be logged by SteelSeries Engine.  The file can be found in one of these locations, depending on OS:

**OSX**     | `/Library/Application Support/SteelSeries Engine 3/Logs/golisp-log.txt`

**Windows** | `%PROGRAMDATA%/SteelSeries/SteelSeries Engine 3/Logs/golisp-log.txt`

[golisp handlers]: /doc/api/writing-handlers-in-golisp.md "Writing Handlers in GoLisp"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
[color-handlers]: /doc/api/json-handlers-color.md
[screen-handlers]: /doc/api/json-handlers-screen.md
[tactile-handlers]: /doc/api/json-handlers-tactile.md
[event-icons]: /doc/api/event-icons.md
