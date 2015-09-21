# Writing Handlers in Json #

This document assumes that you have read the [Sending Events to the SteelSeries GameSense™ API][api doc]

Using SteelSeries GoLisp to write handlers allows the most detailed level of handler creation, but introduces the external dependency of requiring the file with the handler definitions to be manually placed in the `hax0rBindings` directory.  It also makes it impossible for an end-user of the file to customize event bindings through SteelSeries Engine.  As an alternative, specifically formatted json can be used to create events and bind handlers that use a defined subset of functionality.  Handlers defined this way will also allow end users to override your default handler bindings through SteelSeries Engine to create the experience most suited to them.

For each event that you intend to support, you need to register it or bind handlers to it.  Registering an event will merely add it to the system and allow user customization of behavior, while binding handlers will also add default behavior.

# Registering a game #

Your game is automatically registered with the system when you register or bind any events (see below).  However, if you want users to see a user-friendly game name and/or a custom-colored icon in the SteelSeries Engine interface, you will need to POST that metadata to the URL `http://127.0.0.1:<port>/game_metadata`.  Making this call is optional, and each parameter except `game` is optional when making the call.  For a list of the available colors of the default icon, see `Reference Sections - Default icon colors` below.

If you send your game events with the game `"TEST_GAME"`, but you want to indicate to SteelSeries Engine that it should be displayed with a light blue icon and the user-friendly name `My testing game`, you would POST the following JSON to `game_metadata` on startup.

    {
      "game": "TEST_GAME",
      "game_display_name": "My testing game",
      "icon_color_id": 5
    }

# Registering an event #

Note: It is not necessary to both bind and register an event.  The difference is that event registration does not specify default (pre user customization) behavior for an event, whereas event binding does.

You can register an event via sending POST data to the URL `http://127.0.0.1:<port>/register_game_event`.  The payload requires you to specify the game and event names, and can optionally contain minimum and maximum numeric values for the event, as well as an ID specifying what icon is displayed next to the event in the SteelSeries Engine UI.

If the adventure game wanted to indicate to SteelSeries Engine that you will be sending a health event with values between 0-100, and associate it with a health icon, it would POST the following JSON to `register_game_event` on startup.

    {
      "game": "ADVENTURE",
      "event": "HEALTH",
      "min_value": 0
      "max_value": 100,
      "icon_id": 1
    }

Only the "game" and "event" keys are required.  The other keys will be filled in with the following default values if omitted:
* Min value: 0
* Max value: 100
* Icon ID: 0 (No icon is displayed)

Game and event names are limited to the following characters: Uppercase A-Z, the digits 0-9, hyphen, and underscore.

For a list of available icons, see `Reference Sections - Event Icons` below.

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

The following sub-sections describe the format of the handler JSON.

## Specifying a device type ##

The `device-type` key is mandatory, and takes a string describing the device type to which the handler applies.  For a list of valid device type strings, see `Reference Sections - Device types` below.  

## Specifying a zone ##

Specifying a zone to which to apply the effect is mandatory.  The zone can be either a fixed or dynamic zone, both are described below.

### Fixed zones ###
GameSense enabled devices support different named zone specifiers depending on their device type.  As a general rule, all devices with a fixed number of zones will support named identifiers of `one`, `two`, etc. up to the number of zones supported.  All mice support `wheel` for their mousewheel LED and `logo` for their lower logo LED (if applicable).  All keyboards will support certain named zones, e.g. `function-keys`.  

You can specify a fixed zone via the `"zone"` key.  The value the name of the zone to use as a string. For a full list of fixed zone identifiers, see [Zones by device type][zones-types].

    {
      "device-type": "rgb-2-zone"
      "zone": "two"
      ...
    }

    {
      "device-type": "keyboard"
      "zone": "function-keys"
      ...
    }

### Dynamic zones ###

For devices with support for lighting control on a per-key basis (at launch this is limited to the APEX M800), you have the ability to create custom zones.  Note that this is mutually exclusive with specifying a fixed zone.

To specify a dynamic zone, you use the `"custom-zone-keys"` key.  The value is an array of zone numbers.  For the APEX M800, this is an array of the HID codes of the keys in the zone, in the order in which the effect should be applied.  [HID key code reference][HID reference].  For example, to have a handler display using the WASD keys, you can specify:

    {
      "device-type": "rgb-per-key-zones",
      "custom-zone-keys": [26,4,22,7]
    }  

You need to be careful to ensure that the zones you use for a given device don't overlap; the result will likely be confusing for the user.

## Describing the color computation ##

The `"color"` key is mandatory, and controls the way the color of the zone is computed when values are received from the event.  The form of the JSON object used as a value for this key depends on whether you are specifying a static color, a color calculated along a linear gradient between two colors, or a color calculated from a range of values.

### Static color ###

The color is specified statically.  The keys in this zone will always be this color for any non-zero event value, or black for a zero value.  Additional effects may also be applied which cause the zone to flash between this color and black, or cause the color to be applied to only a subset of keys in the zone.  See the later sections `Specifying the visualization mode` and `Specifying flash effects` for details.

For a purple color:

    "color": {
      "red": 255,
      "green": 0,
      "blue": 255
    }

### Color from a linear gradient ###

For this mode we specify a linear gradient between one color representing 0%, and one representing 100%. The event value, assumed to be 0-100, selects a color at the corresponding point along the gradient between the two endpoint colors.

For a gradient between red (0) and green (100):

    "color": {
      "gradient": {
        "zero": {
          "red": 255,
          "green": 0,
          "blue": 0
        },
        "hundred": {
          "red": 0,
          "green": 255,
          "blue": 0
        }
      }
    }

### Color based on ranges ###

The full range of the event value is divided into discrete sub-ranges, with the low and high bounds of each sub-range being inclusive (if the value is >= the low bound and <= the high bound, then it considered to be in the range).  Each sub-range has an associated color specification, which can be a static color, a gradient, or another range definition.

The contents of the `"color"` key should be an array, with each object in the array containing `"low"` and `"high"` keys specifying the range, and a `"color"` key containing a color specification.

For a color that is static red at values 0-10 and a red to green gradient at values 11-100:

    "color": [
      {
        "low": 0,
        "high": 10,
        "color": {
          "red": 255,
          "green": 0,
          "blue": 0
        }
      },
      {
        "low": 11,
        "high": 100,
        "color": {
          "gradient": {
            "zero": {
              "red": 255,
              "green": 0,
              "blue": 0
            },
            "hundred": {
              "red": 0,
              "green": 255,
              "blue": 0
            }
          }
        }
      }
    ]

## Specifying the visualization mode ##

The `"mode"` key is mandatory.  It controls the way that the computed color is applied to the LEDs in the specified zone:

`color`
: All LEDs in the zone are set to the computed color.

`percent`
: The LEDs are used to create a visualization of the control value (as a percentage) by illuminating them proportionally and leaving the rest black/off. This requires that the control value be in the range of 0-100, inclusive. Keep in mind that the order of the LEDs in the zone determine increasing percentage values. The M800 `function-keys` zone is ordered `F1`-`F12`, and so increasing percentage runs from left to right. The most significant lit LED has it's brightness dimmed to reflect how much of it is lit.  E.g. if you have a 10 LED zone, and the percentage is 55, the first 5 will be at full brightness and the sixth will be at half brightness.

_*Note_*: The proportional illumination is only enabled for per-key-illuminated devices (e.g. the Apex M800).  On other devices, the computed color will be applied to all LEDs, behaving like the `color` mode.

`count`
: As above, but the number of LEDs illuminated directly correspond to the control value. I.e. if the value is 2, 2 LEDs will be lit. The control value should be between 0 and the size of the zone.  As above, increasing count is determined by the order of the LEDs in the zone used. Since the value directly indicates how many LEDs to light, there is no dimming effect on the most significant one.

_*Note_*: The count visualization is only enabled for per-key-illuminated devices (e.g. the Apex M800).  On other devices, the computed color will be applied to all LEDs, behaving like the `color` mode.

The visualization mode is set using the `"mode"` key. For example:

    {
      "mode": "percent"
      ...
    }

## Specifying flash effects ##

You can optionally use the `"rate"` key to enable flashing effects on the LEDs in the zone.  Rates are specified by frequency, which is the number of times the LEDs will turn on and off per second.  The amount of time an LED spends on and off when flashing equal.  Flashing frequency can be specified either statically or through a range of values.

### Static frequency

A static frequency specifies that the LEDs will always flash at the given frequency.  If that frequency is zero, then they never flash (are always on).  A static frequency of zero is the default if the `"rate"` key is omitted.

To flash four times a second:

    "rate": {
      "frequency": 4
    }

### Frequency ranges ###

As with colors, you can divide the full range of the event value into discrete sub-ranges by passing an array to this key.  Low and high bounds of each sub-range being inclusive (if the value is >= the low bound and <= the high bound, then it considered to be in the range).  Each sub-range has an associated frequency specification, which can be a static frequency or another range definition.  Any sub-range that is not defined is assumed to default to a frequency of 0 (not flashing).

For an effect that flashes 10 times a second at values 0-10, 5 times a second from 11-20, and does not for any higher value:

    "rate": {
      "frequency": [
        {
          "low": 0,
          "high": 20,
          "frequency": 10
        },
        {
          "low": 11,
          "high": 20,
          "frequency": 5
        }
      ]
    }


### Flash repeat limit ###

If you want the LEDs to flash only a certain number of times rather than flashing continually, you can specify the `"repeat_limit"` key within the `"rate"` value.  When an event value is received that triggers a rate definition with a repeat limit, the flashes will only occur the specified number of times and then stop flashing (remaining on).  Repeat limits can also be specified either with a static value or with a range.

If you do not want to use repeat limits, simply omit this key from the `"rate"` definition.

#### Static repeat limit example

For a static repeat limit of 5:

    {
      ...
      "rate": {
        "frequency": 1,
        "repeat_limit": 5
      }
    }

#### Ranged repeat limit example ####

For a flashing effect that always flashes for one second, but flashes faster at lower values:

    {
      ...
      "rate": {
        "frequency": [
          {
            "low": 0,
            "high": 10,
            "frequency": 10
          },
          {
            "low": 11,
            "high": 20,
            "frequency": 5
          },
          {
            "low": 21,
            "high": 100,
            "frequency": 2
          }
        ],
        "repeat_limit": [
          {
            "low": 0,
            "high": 10,
            "repeat_limit": 10
          },
          {
            "low": 11,
            "high": 20,
            "repeat_limit": 5
          },
          {
            "low": 21,
            "high": 100,
            "repeat_limit": 2
          }
        ]
      }
    }



## Examples ##

Use the row of 12 function keys on the Apex M800 to display a percentage bar graph, selecting the color from a gradient (red at 0, green at 100). Flash at 5 Hz when the value is between 11% and 20%, inclusive, and at 10Hz when it is at or below 10%:

    {
      "device-type": "rgb-per-key-zones",
      "zone": "function-keys",
      "mode": "percent",
      "color": {
        "gradient": {
          "zero": {"red": 255, "green": 0, "blue": 0},
          "hundred": {"red": 0, "green": 255, "blue": 0}}},
      "rate": {"frequency":[{"low": 1, "high": 10, "frequency": 10},
                            {"low": 11, "high": 20, "frequency": 5}]}}

To do something similar on any supported headset:

    {
      "device-type": "headset",
      "zone": "earcups",
      "mode": "color",
      "color": {
        "gradient": {
          "zero": {"red": 255, "green": 0, "blue": 0},
          "hundred": {"red": 0, "green": 255, "blue": 0}}},
      "rate": {"frequency": [{"low": 1, "high": 10, "frequency": 10},
                             {"low": 11, "high": 20, "frequency": 5}]}
    }


Show a count on the M800's 1-5 macro keys. Use a solid white color, and don't flash:

    {
      "device-type": "rgb-per-key-zones",
      "zone": "macro-keys",
      "mode": "count",
      "color": { "red": 255, "green": 255, "blue": 255 }
    }

Flash the M800's esc key 5 times in red (250mS flashes):

    {
      "device-type": "rgb-per-key-zones",
      "zone": "esc",
      "mode": "color",
      "color": { "red": 255, "green": 0, "blue": 0 },
      "rate": {
        "frequency": 2,
        "repeat_limit": 5
      }
    }

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

## Default icon colors ##

The following IDs can be specified in the `"icon_color_id"` key when supplying game metadata, to display the following colored versions of the default game icon next to your game when it is displayed in SteelSeries Engine.

0:  ![Orange](/images/defaulticons/orange.png) Orange  
1:  ![Gold](/images/defaulticons/gold.png) Gold  
2:  ![Yellow](/images/defaulticons/yellow.png) Yellow  
3:  ![Green](/images/defaulticons/green.png) Green  
4:  ![Teal](/images/defaulticons/teal.png) Teal  
5:  ![Light blue](/images/defaulticons/bright-blue.png) Light blue  
6:  ![Blue](/images/defaulticons/blue.png) Blue  
7:  ![Purple](/images/defaulticons/purple.png) Purple  
8:  ![Fuschia](/images/defaulticons/fuschia.png) Fuschia  
9:  ![Pink](/images/defaulticons/hot-pink.png) Pink  
10: ![Red](/images/defaulticons/red.png) Red  
11: ![Silver](/images/defaulticons/silver.png) Silver  

## Event icons ##

The following IDs can be specified in the `"icon_id"` key when registering or binding events to associate the icons pictured to the event when it is displayed in SteelSeries Engine.

0:  Default (Blank display, no icon)  
1:  ![Health](/images/eventicons/health.png) Health  
2:  ![Armor](/images/eventicons/armor.png) Armor  
3:  ![Ammo](/images/eventicons/ammo.png) Ammo/Ammunition  
4:  ![Money](/images/eventicons/money.png) Money  
5:  ![Flashbang](/images/eventicons/flash.png) Flash/Flashbang/Explosion  
6:  ![Kills](/images/eventicons/kills.png) Kills  
7:  ![Headshot](/images/eventicons/headshot.png) Headshot  
8:  ![Helmet](/images/eventicons/helmet.png) Helmet  
10: ![Hunger](/images/eventicons/hunger.png) Hunger  
11: ![Air](/images/eventicons/air.png) Air/Breath  
12: ![Compass](/images/eventicons/compass.png) Compass  
13: ![Tool](/images/eventicons/pick.png) Tool/Pickaxe  
14: ![Mana](/images/eventicons/potion.png) Mana/Potion  
15: ![Clock](/images/eventicons/clock.png) Clock  
16: ![Lightning](/images/eventicons/lightning.png) Lightning  
17: ![Item](/images/eventicons/backpack.png) Item/Backpack  

[golisp handlers]: /doc/api/writing-handlers-in-golisp.md "Writing Handlers in GoLisp"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
