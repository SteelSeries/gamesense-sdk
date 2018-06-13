# JSON screen handlers

Note: This document describes the specific handler format for screen notification handlers.  See [Writing Handlers in JSON][json-handlers] for a general overview of how to bind handlers.

Screen handlers are used to display images and/or textual notifications on supported devices with one or more embedded OLED/LCD screens.

## Full schema definition ##

Each portion is described in detail in later sections.

Top-level schema

    `device-type`: <device type>                                               mandatory
    `zone`: <fixed zone value>                                                 mandatory
    `mode`: "screen"                                                           mandatory
    `datas`: <static-screen-data-definition> |  <range-screen-data-definition> mandatory

_static-screen-data-definition_

    [ <screen-frame-data> ... ] The list is mandatory and must contain one or more entries

_range-screen-data-definition_

    `low`: <event value, low end of range (inclusive)>   mandatory
    `high`: <event value, high end of range (inclusive)> mandatory
    `datas`: <static-screen-data-definition>             mandatory

_screen-frame-data_

    `length-millis`: <integer>    optional. default 0
    `has-text`: <boolean>         mandatory
    `prefix`: <text>              optional. default ""
    `suffix`: <text>              optional. default ""
    `arg`: <data accessor string> optional. default "(value: self)"
    `icon-id`: <icon id>          optional. default 0
    `repeats`: <boolean> | <integer> optional. default false
    `image-data`: <image-data>    optional.  default []

## Specifying a device type ##

The `device-type` key is mandatory.  The value for this key that is guaranteed to match [only] devices with screens is `screened`.  If you must match a specific screen resolution size (for example, you are showing a raw bitmap) use the type `screened-WIDTHxHEIGHT` where `WIDTH` and `HEIGHT` are the resolution of the screen in pixels.  For a full list of device types and zones, see [Zones by device type][zones-types].

## Specifying a zone ##

Specifying a zone to which to apply the effect is mandatory for devices with multiple valid zones, and optional for devices with only a single zone.  For guaranteed future compatibility, it is recommended to use the value of `one` for screen handlers with the device-type `screened`.

## Describing the screen notification ##

The `datas`key is mandatory, and describes a series of frames that will be displayed on the screen on the device as well as the timing with which they will be displayed.  The form of the JSON object used as a value for this key depends on whether you are specifying static frame data or frame data calculated from a range of values.

### Static frame data ###

The frame data is specified statically.  The same frame data will be sent to the screen on the device for each new value sent to the event. Optional effects can be applied which will affect the amount of time each frame is displayed, the formatting of text sent to the screen, adding an icon to the left of the displayed text, and whether and how many times the frames are looped.

Each set of data consists of a list of objects, each of which describes a single on-screen frame.

The only mandatory key is `has-text`.  Setting this value to `true` causes the frame to display the value of the event as text on the screen.  A number of additional options controlling the display of the text are available and described below.  A value of `false` causes the frame to require raw frame data to be supplied in the `image-data` key.  

    "datas": [
      {
      	"has-text": true
      }
    ]

* With an event data value of 15, the text "15" will be displayed on the screen.

#### Text formatting options ####

The `prefix` and `suffix` keys can be used to specify text that will be prepended and appended respectively to the event value in the displayed text.

    "datas": [
      {
      	"has-text": true,
        "prefix": "Got ",
        "suffix": " kills"
      }
    ]

 * With an event data value of 15, the text "Got 15 kills" will be displayed on the screen

 The `arg` key can be used to specify a data transformation to be applied to the event data to get the value substituted into the final text, or be used to obtain the value to display from a source other than the event data.  This key is largely useful when using the local Golisp API rather than the JSON api, but is provided here for flexibility.

 The format of this key is a string that is evaluated by Golisp at runtime to obtain the value.  The default value of this key if not specified is "(value: self)", where `self` refers to the Golisp data frame for the event.

     "datas": [
       {
         "has-text": true,
         "suffix": "k kills",
         "arg": (/ 1000 (value: self))
       }
     ]

* With an event data value of 15000, the text "15k kills" will be displayed on the screen

#### Showing icons alongside text #####

The `icon-id` key can be used to specify an icon that will be displayed to the left of the text.  The icon will be displayed in the 32 leftmost pixels of the display, and the text will be displayed in the remaining space to the right.  For a list of available icon IDs, see [Event icons][event-icons].

	"datas": [
	  {
	  	"has-text": true,
	  	"suffix": "stuff",
	  	"icon-id": 16
	  }
	]

* With an event data value of 15, a lightning bolt icon will be displayed to the left, followed by the text "15"

#### Showing raw bitmaps ####

You can specify a raw image for a frame instead of text with the `image-data` key.  If you use `image-data` for a screen the `has-text` key must be false and the `prefix`, `suffix`, `arg`, and `icon-id` keys are ignored.

The format for `image-data` is an array of byte values where each pixel is a single bit.  A bit of `1` is a white pixel and a bit of `0` is a black pixel. The pixels are packed per row with the origin in the upper-left corner of the bitmap with a most significant bit first bit order.  (Ex. `0b11110000`, or `240`, is four white pixels followed by four black pixels in a row going from left to right.)  The number of bytes in the array must match the size of the device's screen. This can be calculated with `⌈ width * height / 8 ⌉`.

Because you must match the dimensions of the device's screen exactly you should not use the `screened` device type when specifying handlers with raw bitmaps.  Instead, use the `screened-WIDTHxHEIGHT` device type to match devices with the resolution you specify.


    "device-type": "screened-128x36",
    "datas": [
      {
        "has-text": false,
        "image-data": [
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,0,12,0,0,
            0,0,28,0,0,28,0,0,0,112,0,0,0,12,0,0,0,4,28,0,0,28,0,0,0,112,0,0,0,0,0,0,0,14,28,0,0,28,0,0,0,112,0,0,0,0,
            0,0,0,4,127,0,120,28,7,0,112,112,224,14,0,0,28,1,128,1,255,193,254,127,31,193,252,115,248,63,140,236,127,
            15,224,1,193,195,207,127,63,227,254,119,28,113,207,236,227,156,112,3,128,227,135,28,112,119,6,118,12,96,
            207,12,193,152,48,3,28,99,192,28,112,119,7,119,0,192,108,13,128,216,0,31,54,113,248,28,112,119,7,115,192,
            192,108,13,128,223,0,63,34,112,254,28,127,247,255,113,248,255,236,13,255,199,224,31,54,112,31,28,127,247,
            255,112,28,192,12,13,128,0,112,3,28,96,7,28,112,7,0,112,14,192,12,13,128,0,56,3,128,227,131,28,112,119,7,
            118,6,224,108,13,192,216,24,3,193,227,199,31,63,227,254,119,14,112,204,12,225,156,48,1,247,193,254,31,31,
            193,252,115,252,63,140,12,127,15,240,0,255,128,124,31,7,0,112,112,240,14,12,12,28,3,128,0,28,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        ]
      }
    ]

#### Controlling frame timing and repeating data ####

The `length-millis` key can be to control the length of time each frame is displayed.  If not specified, or if the value specified is 0, the frame will remain on the screen until another event writes a frame.  If an integer value is provided, the frame will remain on the screen for the specified number of milliseconds.  

After the frame is displayed, depending on the values of the `repeats` key and the number of screen data frames specified, the screen will either display the next frame in the list, return to the first frame in the list (if the current frame is the last and it is set to repeat), or return to the background image (if the current frame is the last and it is not set to repeat).

Examples

    "datas": [
      {
      	"has-text": true,
      	"suffix": " kills",
      	"length-millis": 250
      }
    ]

* With an event data value of 15, the text "15 kills" will be displayed for a quarter second before the screen returns to the background image.

```
"datas": [
  {
  	"has-text": true,
  	"suffix": "Headshot!",
  	"length-millis": 250,
  	"arg": "",
  	"icon-id": 7
  },
  {
  	"has-text": true,
  	"suffix": " kills",
  	"icon-id": 6
  }
]
```

* With an event data value of 15, the text "Headshot!" will be displayed with a headshot icon for a quarter second.  Next, it will display the text "15 kills" with a kills icon until a new event value is received.

The `repeats` key can be used to specify whether, and how many times, to loop through the specified frames after the first time.  The value for this key can be either a boolean or an integer.  If a boolean, the value `true` means to repeat forever (until a new event is received that writes to the screen), and the value `false` means to display them only once.  If an integer, the value specified is the number of times to display the frames, with 0 being a special case meaning to repeat forever (equivalent to `true`).  The default value is `false`.

The `repeats` key needs to be specified on the final frame in the list.  The value on any other frame will be ignored.

	"datas": [
	  {
	    "has-text": true,
	    "suffix": " frame 1",
	    "length-millis": 200
	  },
	  {
	  	"has-text": true,
	  	"suffix": " frame 2",
	  	"length-millis": 200,
	  	"repeats": true
	  }
	]

* With an event data value of 15, the text values "15 frame 1" and "15 frame 2" will alternate every 200ms until a new event is received that writes to the screen.

## Example: A handler to display custom text

The following example shows you how to bind an event handler that shows custom text that is sent as context with the event data.

	{
		"device-type": "screened",
		"mode": "screen",
		"zone": "one",
		"datas": [{
			"has-text": true,
			"arg": "(custom-text: (context-frame: self))}"
		}
	}

When sending data to this handler, it is ideal to treat the `value` key of the data as an integer that monotonically increases each time data is sent.  This ensures that value caching is bypassed and that the new custom text is displayed each time it is sent.  

To ensure that this handler works properly, it is necessary to send the `frame` key within the data for the event with a subkey `custom-text` that has a string value.  See the "Context Data" section of the [Sending Events][api doc] document for details.

[json-handlers]: /doc/api/writing-handlers-in-json.md "Writing Handlers in JSON"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
[event-icons]: /doc/api/event-icons.md
