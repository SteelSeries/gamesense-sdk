# JSON tactile handlers

Note: This document describes the specific handler format for tactile notification handlers.  See [Writing Handlers in JSON][json-handlers] for a general overview of how to bind handlers.

Tactile handlers are used to control tactile notifications on supported devices with motors for vibration.

## Full schema definition ##

Each portion is described in detail in later sections.

Top-level schema

    `device-type`: <device type>                                         mandatory
    `zone`: <fixed zone value>                                           mandatory
    `mode`: "vibrate"                                                    mandatory
    `pattern`: <static-pattern-definition> |  <range-pattern-definition> mandatory
    `rate`: <rate-definition>                                            optional

_static-pattern-definition_

    [ <predefined-pattern-entry> | <custom-pattern-entry> ... ] The list is mandatory, but it may be empty

_range-pattern-definition_

    `low`: <event value, low end of range (inclusive)>                  mandatory
    `high`: <event value, high end of range (inclusive)>                mandatory
    `pattern`: <static-pattern-definition> | <range-pattern-definition> mandatory

_predefined-pattern-entry_

    `type`: <predefined pattern type value>                mandatory
    `delay-ms`: <delay value in ms, ignored on last entry> optional

_custom-pattern-entry_

    `type`: "custom" mandatory
    `length-ms`: <length value in ms>                      mandatory
    `delay-ms`: <delay value in ms, ignored on last entry> optional

_rate-definition_

    `frequency`: <static frequency value> | <range-frequency-definition>          mandatory
    `repeat_limit`: <static repeat limit value> | <range-repeat-limit-definition> optional

_range-frequency-definition_

    `low`: <event value, low end of range (inclusive)>                    mandatory
    `high`: <event value, high end of range (inclusive)>                  mandatory
    `frequency`:  <static frequency value> | <range-frequency-definition> mandatory

_range-repeat-limit-definition_

    `low`: <event value, low end of range (inclusive)>   mandatory
    `high`: <event value, high end of range (inclusive)> mandatory
    `repeat_limit`:  <static repeat limit value>         mandatory


## Specifying a device type ##

The `device-type` key is mandatory.  Currently the only value for this key that is guaranteed to match [only] tactile devices is `tactile`.  For a full list of device types and zones, see [Zones by device type][zones-types].

## Specifying a zone ##

Specifying a zone to which to apply the effect is mandatory for devices with multiple valid zones, and optional for devices with only a single zone.  For guaranteed future compatibility, it is recommended to use the value of `one` for tactile handlers with the device-type `tactile`.

## Describing the tactile notification ##

The `pattern` key is mandatory, and describes the vibrations that the motor will produce on the device when values are received from the event.  The form of the JSON object used as a value for this key depends on whether you are specifying a static pattern or a pattern calculated from a range of values.

### Static pattern ###

The pattern is specified statically.  The same vibration pattern will be sent to the motor on the device for each new value sent to the event.  Additional effects may also be applied which cause the pattern to be repeated at a certain frequency, and limit it to repeating a certain number of times.  See the later section `Specifying repeated effects` for details.

Each pattern consists of a list of objects, each of which describes either a value that is predefined in the firmware library, or describes a vibration that happens for a custom length of time.  The maximum length of the supported list may vary by device.  For the Rival 700, the maximum possible length is 140, with each custom event counting as 2.

#### Predefined library values ####

For predefined effects, the `type` key is mandatory, and corresponds to the name of one of the predefined effects.  For a list of the possible predefined values, see `Reference Sections - TI predefined vibrations` below.

The `delay-ms` key is optional.  If specified on a value that is not the last value in the list, it will control the delay in milliseconds between the vibration and the next vibration.  If specified on a value that is the last or only value in the list, it will be ignored.  The maximum possible value of this key is 2560, and any larger value will be ignored.

For a single strong click effect:

    "pattern": [
      {
      	"type": "ti_predefined_strongclick_100"
      }
    ]

For an effect that consists of a strong click followed after 150ms by another strong click:

    "pattern": [
      {
      	"type": "ti_predefined_strongclick_100",
      	"delay-ms": 150
      },
      {
      	"type": "ti_predefined_strongclick_100"
      }
    ]

#### Custom vibration lengths ####

Patterns can also be specified with non-predefined custom vibration lengths.

The `type` key is mandatory and must be set to the value `custom`.

The `length-ms` key is mandatory, and controls the number of milliseconds that the vibration motor will be enabled before turning off again.  The maximum value is 2560.

The `delay-ms` key is optional.  It works the same way for custom events as for predefined events, see above.

For an effect with a custom short buzz followed after 150 ms by a custom long buzz?:

    "pattern": [
      {
        "type": "custom",
        "length-ms": 100,
        "delay-ms": 150
      },
      {
        "type": "custom",
        "length-ms": 250
      }
    ]


### Vibration based on ranges ###

The full range of the event value is divided into discrete sub-ranges, with the low and high bounds of each sub-range being inclusive (if the value is >= the low bound and <= the high bound, then it considered to be in the range).  Each sub-range has an associated patterns specification as described above for a static pattern.

The contents of the `"pattern"` key should be an array, with each object in the array containing `"low"` and `"high"` keys specifying the range, and a `"pattern"` key containing a pattern specification.

For a vibration pattern that does not vibrate at high health, that clicks a single time per new value at medium health, and that uses the above custom pattern for every new value at low health:

    "pattern": [
      {
        "low": 61,
        "high": 100,
        "pattern": []
      },
      {
        "low": 26,
        "high": 60,
        "pattern": [
          "type": "ti_predefined_sharpclick_60"
        ]
      },
      {
        "low": 1,
        "high": 25,
        "pattern": [
          {
            "type": "custom",
            "length-ms": 100,
            "delay-ms": 150
          },
          {
            "type": "custom",
            "length-ms": 250
          }
        ]
      }
    ]

## Specifying repeating vibration effects ##

You can optionally use the `"rate"` key to enable repetition of the vibration effect.  Rates are specified by frequency, which is how many times the effect will be repeated a second.  Repetition frequency can be specified either statically or through a range of values.

Note: Unlike color effects, vibration effects take time to complete.  Please take care to set the frequency low enough that vibration effects are not constantly queued up.

### Static frequency

A static frequency specifies that the vibration will always be repeated at the given frequency.  If that frequency is zero, then they never repeat (are only played once).  A static frequency of zero is the default if the `"rate"` key is omitted.

To repeat twice a second:

    "rate": {
      "frequency": 2
    }

### Frequency ranges ###

As with colors or with vibration patterns, you can divide the full range of the event value into discrete sub-ranges by passing an array to this key.  Low and high bounds of each sub-range being inclusive (if the value is >= the low bound and <= the high bound, then it considered to be in the range).  Each sub-range has an associated frequency specification, which can be a static frequency or another range definition.  Any sub-range that is not defined is assumed to default to a frequency of 0 (not flashing).

For an effect that repeats 3 times a second at values 0-10, once a second from 11-20, and does not for any higher value:

    "rate": {
      "frequency": [
        {
          "low": 0,
          "high": 10,
          "frequency": 3
        },
        {
          "low": 11,
          "high": 20,
          "frequency": 1
        }
      ]
    }


### Vibration effect repeat limit ###

If you want the vibration to happen only a certain number of times rather than flashing continually, you can specify the `"repeat_limit"` key within the `"rate"` value.  When an event value is received that triggers a rate definition with a repeat limit, the vibration effect will only happen the specified number of times and then stop.  Repeat limits can also be specified either with a static value or with a range.

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

For a vibration effect that repeats more the lower the value is:

    {
      ...
      "rate": {
        "frequency": 5,
        "repeat_limit": [
          {
            "low": 0,
            "high": 10,
            "repeat_limit": 3
          },
          {
            "low": 11,
            "high": 20,
            "repeat_limit": 2
          },
          {
            "low": 21,
            "high": 100,
            "repeat_limit": 1
          }
        ]
      }
    }

## Reference Sections - TI predefined vibrations ##

Note: Most of these effects have "softer" versions that feel softer.  Any effect suffixed `_100` is the full power version of that effect, while a different number suffix indicates relative strength on a percentage basis.  Similar effects are grouped in each bullet point.

* `ti_predefined_strongclick_100`
`ti_predefined_strongclick_60`
`ti_predefined_strongclick_30`
* `ti_predefined_sharpclick_100`
`ti_predefined_sharpclick_60`
`ti_predefined_sharpclick_30`
* `ti_predefined_softbump_100`
`ti_predefined_softbump_60`
`ti_predefined_softbump_30`
* `ti_predefined_doubleclick_100`
`ti_predefined_doubleclick_60`
* `ti_predefined_tripleclick_100`
* `ti_predefined_softfuzz_60`
* `ti_predefined_strongbuzz_100`
* `ti_predefined_buzzalert750ms`
`ti_predefined_buzzalert1000ms`
* `ti_predefined_strongclick1_100`
`ti_predefined_strongclick2_80`
`ti_predefined_strongclick3_60`
`ti_predefined_strongclick4_30`
* `ti_predefined_mediumclick1_100`
`ti_predefined_mediumclick2_80`
`ti_predefined_mediumclick3_60`
* `ti_predefined_sharptick1_100`
`ti_predefined_sharptick2_80`
`ti_predefined_sharptick3_60`
* `ti_predefined_shortdoubleclickstrong1_100`
`ti_predefined_shortdoubleclickstrong2_80`
`ti_predefined_shortdoubleclickstrong3_60`
`ti_predefined_shortdoubleclickstrong4_30`
* `ti_predefined_shortdoubleclickmedium1_100`
`ti_predefined_shortdoubleclickmedium2_80`
`ti_predefined_shortdoubleclickmedium3_60`
* `ti_predefined_shortdoublesharptick1_100`
`ti_predefined_shortdoublesharptick2_80`
`ti_predefined_shortdoublesharptick3_60`
* `ti_predefined_longdoublesharpclickstrong1_100`
`ti_predefined_longdoublesharpclickstrong2_80`
`ti_predefined_longdoublesharpclickstrong3_60`
`ti_predefined_longdoublesharpclickstrong4_30`
* `ti_predefined_longdoublesharpclickmedium1_100`
`ti_predefined_longdoublesharpclickmedium2_80`
`ti_predefined_longdoublesharpclickmedium3_60`
* `ti_predefined_longdoublesharptick1_100`
`ti_predefined_longdoublesharptick2_80`
`ti_predefined_longdoublesharptick3_60`
* `ti_predefined_buzz1_100`
`ti_predefined_buzz2_80`
`ti_predefined_buzz3_60`
`ti_predefined_buzz4_40`
`ti_predefined_buzz5_20`
* `ti_predefined_pulsingstrong1_100`
`ti_predefined_pulsingstrong2_60`
* `ti_predefined_pulsingmedium1_100`
`ti_predefined_pulsingmedium2_60`
* `ti_predefined_pulsingsharp1_100`
`ti_predefined_pulsingsharp2_60`
* `ti_predefined_transitionclick1_100`
`ti_predefined_transitionclick2_80`
`ti_predefined_transitionclick3_60`
`ti_predefined_transitionclick4_40`
`ti_predefined_transitionclick5_20`
`ti_predefined_transitionclick6_10`
* `ti_predefined_transitionhum1_100`
`ti_predefined_transitionhum2_80`
`ti_predefined_transitionhum3_60`
`ti_predefined_transitionhum4_40`
`ti_predefined_transitionhum5_20`
`ti_predefined_transitionhum6_10`
* `ti_predefined_transitionrampdownlongsmooth1_100to0`
`ti_predefined_transitionrampdownlongsmooth2_100to0`
* `ti_predefined_transitionrampdownmediumsmooth1_100to0`
`ti_predefined_transitionrampdownmediumsmooth2_100to0`
* `ti_predefined_transitionrampdownshortsmooth1_100to0`
`ti_predefined_transitionrampdownshortsmooth2_100to0`
* `ti_predefined_transitionrampdownlongsharp1_100to0`
`ti_predefined_transitionrampdownlongsharp2_100to0`
* `ti_predefined_transitionrampdownmediumsharp1_100to0`
`ti_predefined_transitionrampdownmediumsharp2_100to0`
* `ti_predefined_transitionrampdownshortsharp1_100to0`
`ti_predefined_transitionrampdownshortsharp2_100to0`
* `ti_predefined_transitionrampuplongsmooth1_0to100`
`ti_predefined_transitionrampuplongsmooth2_0to100`
* `ti_predefined_transitionrampupmediumsmooth1_0to100`
`ti_predefined_transitionrampupmediumsmooth2_0to100`
* `ti_predefined_transitionrampupshortsmooth1_0to100`
`ti_predefined_transitionrampupshortsmooth2_0to100`
* `ti_predefined_transitionrampuplongsharp1_0to100`
`ti_predefined_transitionrampuplongsharp2_0to100`
* `ti_predefined_transitionrampupmediumsharp1_0to100`
`ti_predefined_transitionrampupmediumsharp2_0to100`
* `ti_predefined_transitionrampupshortsharp1_0to100`
`ti_predefined_transitionrampupshortsharp2_0to100`
* `ti_predefined_transitionrampdownlongsmooth1_50to0`
`ti_predefined_transitionrampdownlongsmooth2_50to0`
* `ti_predefined_transitionrampdownmediumsmooth1_50to0`
`ti_predefined_transitionrampdownmediumsmooth2_50to0`
* `ti_predefined_transitionrampdownshortsmooth1_50to0`
`ti_predefined_transitionrampdownshortsmooth2_50to0`
* `ti_predefined_transitionrampdownlongsharp1_50to0`
`ti_predefined_transitionrampdownlongsharp2_50to0`
* `ti_predefined_transitionrampdownmediumsharp1_50to0`
`ti_predefined_transitionrampdownmediumsharp2_50to0`
* `ti_predefined_transitionrampdownshortsharp1_50to0`
`ti_predefined_transitionrampdownshortsharp2_50to0`
* `ti_predefined_transitionrampuplongsmooth1_0to50`
`ti_predefined_transitionrampuplongsmooth2_0to50`
* `ti_predefined_transitionrampupmediumsmooth1_0to50`
`ti_predefined_transitionrampupmediumsmooth2_0to50`
* `ti_predefined_transitionrampupshortsmooth1_0to50`
`ti_predefined_transitionrampupshortsmooth2_0to50`
* `ti_predefined_transitionrampuplongsharp1_0to50`
`ti_predefined_transitionrampuplongsharp2_0to50`
* `ti_predefined_transitionrampupmediumsharp1_0to50`
`ti_predefined_transitionrampupmediumsharp2_0to50`
* `ti_predefined_transitionrampupshortsharp1_0to50`
`ti_predefined_transitionrampupshortsharp2_0to50`
* `ti_predefined_longbuzzforprogrammaticstopping_100`
* `ti_predefined_smoothhum1nokickorbrakepulse_50`
`ti_predefined_smoothhum2nokickorbrakepulse_40`
`ti_predefined_smoothhum3nokickorbrakepulse_30`
`ti_predefined_smoothhum4nokickorbrakepulse_20`
`ti_predefined_smoothhum5nokickorbrakepulse_10`

[json-handlers]: /doc/api/writing-handlers-in-json.md "Writing Handlers in JSON"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
