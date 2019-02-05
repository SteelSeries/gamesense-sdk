# Writing Handlers in Json #

This document assumes that you have read the [Sending Events to the SteelSeries GameSense™ API][api doc]

Using SteelSeries GoLisp to write handlers allows the most detailed level of handler creation, but introduces the external dependency of requiring the file with the handler definitions to be manually placed in the `hax0rBindings` directory.  It also makes it impossible for an end-user of the file to customize event bindings through SteelSeries Engine.  As an alternative, specifically formatted json can be used to create events and bind handlers that use a defined subset of functionality.  Handlers defined this way will also allow end users to override your default handler bindings through SteelSeries Engine to create the experience most suited to them.

For each event that you intend to support, you need to register it or bind handlers to it.  Registering an event will merely add it to the system and allow user customization of behavior, while binding handlers will also add default behavior.

# Registering a game #

Your game is automatically registered with the system when you register or bind any events (see below).  However, you can use another call to set various pieces of metadata.  Most notably, if you want users to see a user-friendly game name or developer name, you will need to POST that metadata to the URL `http://127.0.0.1:<port>/game_metadata`.  Making this call is optional, and each parameter except `game` is optional when making the call.

The optional parameters are as follows:
| JSON key                       | Value type           | Description       |
|--------------------------------|----------------------|-------------------|
| `game_display_name`            | string               | User-friendly name displayed in SSE.  If this is not set, your game will show up as the `game` string sent with your data |
| `developer`                    | string               | Developer name displayed underneath the game name in SSE.  This line is omitted in SSE if the metadata field is not set. |
| `deinitialize_timer_length_ms` | integer (1000-60000) | By default, SSE will return to default behavior when the `stop_game` call is made or when no events have been received for 15 seconds.  This can be used to customize that length of time between 1 and 60 seconds. |

If you send your game events with the game `"TEST_GAME"`, but you want to indicate to SteelSeries Engine that it should be displayed with the user-friendly name `My testing game` and a developer name of `My Game Studios`, you would POST the following JSON to `game_metadata` on startup.

    {
      "game": "TEST_GAME",
      "game_display_name": "My testing game"
      "developer": "My Game Studios"
    }

# Registering an event #

Note: It is not necessary to both bind and register an event.  The difference is that event registration does not specify default (pre user customization) behavior for an event, whereas event binding does.

You can register an event via sending POST data to the URL `http://127.0.0.1:<port>/register_game_event`.  The payload requires you to specify the game and event names, and can optionally contain minimum and maximum numeric values for the event, as well as an ID specifying what icon is displayed next to the event in the SteelSeries Engine UI.

If the adventure game wanted to indicate to SteelSeries Engine that you will be sending a health event with values between 0-100, and associate it with a health icon, it would POST the following JSON to `register_game_event` on startup.

    {
      "game": "ADVENTURE",
      "event": "HEALTH",
      "min_value": 0,
      "max_value": 100,
      "icon_id": 1
    }

Only the "game" and "event" keys are required.  The other keys will be filled in with the following default values if omitted:
* Min value: 0
* Max value: 100
* Icon ID: 0 (No icon is displayed)

Game and event names are limited to the following characters: Uppercase A-Z, the digits 0-9, hyphen, and underscore.

For a list of available icons, see [Event icons][event-icons].

# Binding an event #

Note: It is not necessary to both bind and register an event.  The difference is that event registration does not specify default (pre user customization) behavior for an event, whereas event binding does.

You can bind handlers for an event via sending POST data to the URL `http://127.0.0.1:<port>/bind_game_event`.  The payload includes all of the same mandatory and optional keys as event registration, as well as one additional key `handlers`.  The `handlers` value is an array of handlers.  Each handler should be either a JSON object or the stringified representation of a JSON object that describes the handler and what device type it should be applied to.  There should be one handler for each device type to which you wish to apply default behavior.

For each handler, your JSON data describes:

* The device type to which you wish to apply the effect
* The zone effected
* How the color should be calculated
* How the flash rate should be calculated
* In the case of zone-rich devices (e.g. the APEX M800 keyboard), whether to apply the color across all keys in the zone or to dynamically calculate how many keys to illuminate.

So if the adventure game wanted to provide its health event handler, it would POST the following JSON to `bind_game_event`.

    {
      "game": "ADVENTURE",
      "event": "HEALTH",
      "min_value": 0
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

This handler will display the health (whose value is 0-100, inclusive) as a percentage bar graph (assuming the connected keyboard supports it, or just using the color otherwise) on the function keys, varying from green at full health down to red at 0 health.

As of SteelSeries Engine 3.7.0, there are three different types of handlers you can create, each of which is described in its own document:

* [Color handlers][color-handlers] for interacting with illumination of devices with RGB LED lighting
* [Tactile handlers][tactile-handlers] for providing tactile notifications that users can feel on supported devices
* [Screen handlers][screen-handlers] for providing text and image notificaitons on OLED/LCD screens on supported devices

# Removing an event #

As of SteelSeries Engine 3.5.0, you can remove an event you have registered via sending POST data to the URL `http://127.0.0.1:<port>/remove_game_event`.  The payload requires you to specify the game and event names.  To remove the event MY_EVENT from the game MY_GAME, POST the following data:

    {
      "game": "MY_GAME",
      "event": "MY_EVENT"
    }

Removing an event also removes all bindings for the event.  However, if you have bound handlers via golisp and the file still exists, the event will be automatically re-registered and re-bound when the file is loaded.

Events that are built-in to SteelSeries Engine 3 by default cannot be removed.

# Removing a game #

As of SteelSeries Engine 3.5.0, you can remove a game you have registered via sending POST data to the URL `http://127.0.0.1:<port>/remove_game`.  The payload requires you to specify the game name.  To remove the game MY_GAME, POST the following data:

    {
      "game": "MY_GAME"
    }

Removing a game also removes all events registered for the game, as well as all bindings for the events.However, if you have bound handlers via golisp and the file still exists, the game and events in the file will be automatically re-registered and re-bound when the file is loaded.

Events that are built-in to SteelSeries Engine 3 by default cannot be removed.

# Reference sections #

## Error handling ##

All successful requests to the JSON API will return an HTTP status code of 200.  As of SteelSeries Engine 3.5.0, unsuccessful requests will return an error code along with the error message describing the problem.

A request that has any problems with the parameters will return a 400 status code and one of the following errors:

0: "Game or event string not specified".  Most of the requests require specifying both the game and event in question.  This error is returned if one is missing.

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

[golisp handlers]: /doc/api/writing-handlers-in-golisp.md "Writing Handlers in GoLisp"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
[color-handlers]: /doc/api/json-handlers-color.md
[screen-handlers]: /doc/api/json-handlers-screen.md
[tactile-handlers]: /doc/api/json-handlers-tactile.md
[event-icons]: /doc/api/event-icons.md
