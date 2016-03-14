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

The `device-type` key is mandatory.  Currently the only value for this key that is guaranteed to match [only] devices with screens is `screened`.  For a full list of device types and zones, see [Zones by device type][zones-types].

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

[json-handlers]: /doc/api/writing-handlers-in-json.md "Writing Handlers in JSON"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
[event-icons]: /doc/api/event-icons.md