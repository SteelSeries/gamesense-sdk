# JSON color handlers

Note: This document describes the specific handler format for color handlers.  See [Writing Handlers in JSON][json-handlers] for a general overview of how to bind handlers.  See [JSON Handlers for Full-Keyboard Lighting Effects][json-handlers-full-keyboard] for details writing an effect handler that writes to each individual key on a keyboard.

Color handlers are used to control LED lighting across a wide variety of supported products.

## Full schema definition ##

Each portion is described in detail in later sections.

Top-level schema

```
`device-type`: <device type>                            mandatory
`zone`: <fixed zone value>                              mandatory for either the `zone` or `custom-zone-keys` to be specified
`custom-zone-keys`: <dynamic-zone-definition>           mandatory for either the `zone` or `custom-zone-keys` to be specified
`mode`: `count` | `percent` | `color` | `context-color` mandatory
`color`: <static-color-definition> | <gradient-color-definition> | <range-color-definition> mandatory except for `context-color` mode
`rate`: <rate-definition>                               optional
`context-frame-key`: <string>                           mandatory for `context-color` mode, unused otherwise
```

_static-color-definition_

```
`red`: Red value  (0-255)    mandatory
`green`: Green value (0-255) mandatory
`blue`: Blue value (0-255)   mandatory
```

_gradient-color-definition_

```
`zero`: <static-color-definition>    mandatory
`hundred`: <static-color-definition> mandatory
```

_range-color-definition_

```
`low`: <event value, low end of range (inclusive)>               mandatory
`high`: <event value, high end of range (inclusive)>             mandatory
`color`: <static-color-definition> | <gradient-color-definition> mandatory
```

_rate-definition_

```
`frequency`: <static frequency value> | <range-frequency-definition>          mandatory
`repeat_limit`: <static repeat limit value> | <range-repeat-limit-definition> optional
```

_range-frequency-definition_

```
`low`: <event value, low end of range (inclusive)>                    mandatory
`high`: <event value, high end of range (inclusive)>                  mandatory
`frequency`:  <static frequency value> | <range-frequency-definition> mandatory
```

_range-repeat-limit-definition_

```
`low`: <event value, low end of range (inclusive)>                             mandatory
`high`: <event value, high end of range (inclusive)>                           mandatory
`repeat_limit`:  <static repeat limit value> | <range-repeat-limit-definition> mandatory
```

## Specifying a device type ##

The `device-type` key is mandatory, and takes a string describing the device type to which the handler applies.  For a list of valid device type strings, see [Zones by device type][zones-types].  

## Specifying a zone ##

Specifying a zone to which to apply the effect is mandatory.  The zone can be either a fixed or dynamic zone, both are described below.

### Fixed zones ###
GameSense enabled devices support different named zone specifiers depending on their device type.  As a general rule, all devices with a fixed number of zones will support named identifiers of `one`, `two`, etc. up to the number of zones supported.  All mice support `wheel` for their mousewheel LED and `logo` for their lower logo LED (if applicable).  All keyboards will support certain named zones, e.g. `function-keys`.  

You can specify a fixed zone via the `"zone"` key.  The value the name of the zone to use as a string. For a full list of fixed zone identifiers, see [Zones by device type][zones-types].

```json
{
  "device-type": "rgb-2-zone",
  "zone": "two"
}
```

```json
{
  "device-type": "keyboard",
  "zone": "function-keys"
}
```

### Dynamic zones ###

For devices with support for lighting control on a per-key basis, you have the ability to create custom zones.  Note that this is mutually exclusive with specifying a fixed zone for a handler.

To specify a dynamic zone, you use the `"custom-zone-keys"` key.  The value is an array of zone numbers.  For all existing keyboards, this is an array of the HID codes of the keys in the zone, in the order in which the effect should be applied.  [HID key code reference][HID reference].  For example, to have a handler display using the WASD keys, you can specify:

```json
{
  "device-type": "rgb-per-key-zones",
  "custom-zone-keys": [26,4,22,7]
}
```

You should be careful in most cases to ensure that the zones you use for a given device don't overlap.  If they do, the result will likely be confusing for the user.

## Describing the color computation ##

The `"color"` key is mandatory, and controls the way the color of the zone is computed when values are received from the event.  The form of the JSON object used as a value for this key depends on whether you are specifying a static color, a color calculated along a linear gradient between two colors, or a color calculated from a range of values.

### Static color ###

The color is specified statically.  The keys in this zone will always be this color for any non-zero event value, or black for a zero value.  Additional effects may also be applied which cause the zone to flash between this color and black, or cause the color to be applied to only a subset of keys in the zone.  See the later sections `Specifying the visualization mode` and `Specifying flash effects` for details.

For a purple color:

```json
"color": {
  "red": 255,
  "green": 0,
  "blue": 255
}
```

### Color from a linear gradient ###

For this mode we specify a linear gradient between one color representing 0%, and one representing 100%. The event value, assumed to be 0-100, selects a color at the corresponding point along the gradient between the two endpoint colors.

For a gradient between red (0) and green (100):

```json
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
```

### Color based on ranges ###

The full range of the event value is divided into discrete sub-ranges, with the low and high bounds of each sub-range being inclusive (if the value is >= the low bound and <= the high bound, then it considered to be in the range).  Each sub-range has an associated color specification, which can be a static color, a gradient, or another range definition.

The contents of the `"color"` key should be an array, with each object in the array containing `"low"` and `"high"` keys specifying the range, and a `"color"` key containing a color specification.

For a color that is static red at values 0-10 and a red to green gradient at values 11-100:

```json
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
```

## Specifying the visualization mode ##

The `"mode"` key is mandatory.  It controls the way that the computed color is applied to the LEDs in the specified zone:

`color`
: All LEDs in the zone are set to the computed color.

`percent`
: The LEDs are used to create a visualization of the control value (as a percentage) by illuminating them proportionally and leaving the rest black/off. This requires that the control value be in the range of 0-100, inclusive. Keep in mind that the order of the LEDs in the zone determine increasing percentage values. The M800 `function-keys` zone is ordered `F1`-`F12`, and so increasing percentage runs from left to right. The most significant lit LED has it's brightness dimmed to reflect how much of it is lit.  E.g. if you have a 10 LED zone, and the percentage is 55, the first 5 will be at full brightness and the sixth will be at half brightness.

_*Note_*: The proportional illumination is only enabled for per-key-illuminated devices (e.g. the Apex M800).  On other devices, the computed color will be applied to all LEDs, behaving like the `color` mode.

`count`
: As above, but the number of LEDs illuminated directly correspond to the control value. I.e. if the value is 2, 2 LEDs will be lit. The control value should be between 0 and the size of the zone.  As above, increasing count is determined by the order of the LEDs in the zone used. Since the value directly indicates how many LEDs to light, there is no dimming effect on the most significant one.

`context-color`
: This mode pulls the color data for the zone from the context data frame sent with the event at runtime.  See the section [Dynamic color via context data](#dynamic-color-via-context-data) below for full details.

_*Note_*: Introduced in SteelSeries Engine 3.18.0.


_*Note_*: The count visualization is only enabled for per-key-illuminated devices (e.g. the Apex M800).  On other devices, the computed color will be applied to all LEDs, behaving like the `color` mode.

The visualization mode is set using the `"mode"` key. For example:

```json
{
  "mode": "percent"
}
```

For details on the `bitmap` visualization mode, see the page [JSON handlers for full-keyboard lighting effects][json-handlers-full-keyboard].

## Dynamic color via context data ##

The handler mode `context-color` can be used to entirely avoid pre-calculation and pull the color to display directly from the data sent with the event.  Using this handler type requires you to specify a string value in the `context-frame-key` key for the handler.  The string is treated as a key in the object sent in the `frame` key in the event payload, if specified.  For this handler type, the value of the key in the event data must be a color value specified according to the _static-color-definition_ schema above.  For instructions on sending contex data with events, see [Event context data][event-context-data].

For example:

The following handler definition specifies zone one on an `rgb-3-zone` device.  The color for this zone will be located in the `zone-one-color` key in the context data.

```json
{
  "mode": "context-color",
  "device-type": "rgb-3-zone",
  "zone": "one",
  "context-frame-key": "zone-one-color"
}
```

Sending the following event data will cause green to be displayed on the zone:

```json
{
  "game": "WHATEVER-YOUR-GAME-IS",
  "event": "YOUR-EVENT",
  "data": {
    "frame": {
      "zone-one-color": {
        "red": 0,
        "green": 255,
        "blue": 0
      }
    }
  }
}
```

## Specifying flash effects ##

You can optionally use the `"rate"` key to enable flashing effects on the LEDs in the zone.  Rates are specified by frequency, which is the number of times the LEDs will turn on and off per second.  The amount of time an LED spends on and off when flashing equal.  Flashing frequency can be specified either statically or through a range of values.

### Static frequency

A static frequency specifies that the LEDs will always flash at the given frequency.  If that frequency is zero, then they never flash (are always on).  A static frequency of zero is the default if the `"rate"` key is omitted.

To flash four times a second:

```json
"rate": {
  "frequency": 4
}
```

### Frequency ranges ###

As with colors, you can divide the full range of the event value into discrete sub-ranges by passing an array to this key.  Low and high bounds of each sub-range being inclusive (if the value is >= the low bound and <= the high bound, then it considered to be in the range).  Each sub-range has an associated frequency specification, which can be a static frequency or another range definition.  Any sub-range that is not defined is assumed to default to a frequency of 0 (not flashing).

For an effect that flashes 10 times a second at values 0-10, 5 times a second from 11-20, and does not for any higher value:

```json
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
    }
  ]
}
```

### Flash repeat limit ###

If you want the LEDs to flash only a certain number of times rather than flashing continually, you can specify the `"repeat_limit"` key within the `"rate"` value.  When an event value is received that triggers a rate definition with a repeat limit, the flashes will only occur the specified number of times and then stop flashing (remaining on).  Repeat limits can also be specified either with a static value or with a range.

If you do not want to use repeat limits, simply omit this key from the `"rate"` definition.

#### Static repeat limit example

For a static repeat limit of 5:

```json
{
  "rate": {
    "frequency": 1,
    "repeat_limit": 5
  }
}
```

#### Ranged repeat limit example ####

For a flashing effect that always flashes for one second, but flashes faster at lower values:

```json
{
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
```

## Examples ##

Use the row of 12 function keys on a per-key keyboard to display a percentage bar graph, selecting the color from a gradient (red at 0, green at 100). Flash at 5 Hz when the value is between 11% and 20%, inclusive, and at 10Hz when it is at or below 10%:

```json
{
  "device-type": "rgb-per-key-zones",
  "zone": "function-keys",
  "mode": "percent",
  "color": {
    "gradient": {
      "zero": {"red": 255, "green": 0, "blue": 0},
      "hundred": {"red": 0, "green": 255, "blue": 0}}},
  "rate": {"frequency":[{"low": 1, "high": 10, "frequency": 10},
                        {"low": 11, "high": 20, "frequency": 5}]}
}
```

To do something similar on any supported headset:

```
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
```

Show a count on a per-key keyboard's 1-5 macro keys (if it is a keyboard with macro keys). Use a solid white color, and don't flash:

```json
{
  "device-type": "rgb-per-key-zones",
  "zone": "macro-keys",
  "mode": "count",
  "color": { "red": 255, "green": 255, "blue": 255 }
}
```

Flash a per-key keyboard's Esc key 5 times in red (250mS flashes):

```
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
```

[json-handlers]: /doc/api/writing-handlers-in-json.md "Writing Handlers in JSON"
[json-handlers-full-keyboard]: /doc/api/json-handlers-full-keyboard-lighting.md "JSON Handlers for Full-Keyboard Lighting Effects"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: https://www.usb.org/document-library/hid-usage-tables-112
[event-context-data]: /doc/api/sending-game-events.md#event-context-data