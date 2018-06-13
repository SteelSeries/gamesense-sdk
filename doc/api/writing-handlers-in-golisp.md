# Writing Handlers in GoLisp #

This document assumes that you have read the [Sending Events to the SteelSeries GameSense™ API][api doc]

Using the JSON API to create event handlers is recommended for programs and games that will be distributed to other users, because it allows user customization and requires no distribution dependencies.  See [this document][json api doc] for details.

However, for personal projects or to take fullest advantage of the capabilities of the SDK, you can create event handlers using SteelSeries Golisp.  Steelseries GoLisp is a version of Scheme we wrote to provide an extension/scripting language for Engine3.

The rest of this document assumes familiarity Lisp in general, and with SteelSeries Golisp in particular.  See [the GoLisp documentation][golisp documentation] for more information.

## Getting started ##

To start using GoLisp to create handlers, you have two choices. You can either use Steelseries Engine to register GoLisp handlers or use the haX0rBindings directory.

For using Steelseries Engine to register the handlers, you first must be using Steelseries Engine 3.9.0 or higher. You then need to send a JSON payload with both the game you're registering them for and the GoLisp handlers themselves. To do this you discover the Steelseries Engine port (you can do this using the guide in the [sending game event][server discovery] document) and sending it on the `/load_golisp_handlers` endpoint in this format:

```json
{
  "game": "<Game name>",
  "golisp": "<GoLisp handler code>"
}
```

For the haX0rBindings method need to create a lisp file that SteelSeries Engine will load.  First, locate the directory that Engine loads custom lisp from.  This location depends on your OS:

| OS          | Path                                                              |
|-------------|-------------------------------------------------------------------|
| **OSX**     | `/Library/Application Support/SteelSeries Engine 3/haX0rBindings` |
| **Windows** | `%PROGRAMDATA%/SteelSeries/SteelSeries Engine 3/haX0rBindings`    |

Create a file in this directory with the extension `.lsp`.  The filename of the file must match the string that will be passed in the `game` key when events are sent (although the match is case-insensitive).

So if you are on Windows, and your game is sending an event with the following JSON:

    {
      "game": "MY_GAME",
      "event": "HEALTH",
      "data": {
        "value": 50, 
        "frame": {
          "exact-health": "2413"
        }
      }
    }

then you would create the file `%PROGRAMDATA%/SteelSeries/SteelSeries Engine 3/haX0rBindings/my_game.lsp`.

_Note:_ Any handlers created a haX0rBindings file will now override all other handler values for the game in question, including those configured through SteelSeries Engine 3.  Those events will also not appear in SteelSeries Engine as available for customization unless the file is removed.

## Basic example and breakdown ##

Below is a simple example of how to display calculated colors on multiple devices when event data is received.  A breakdown of the GameSense™ primitive functions used in the example will follow.

    (handler "HEALTH"
      (lambda (data)
        (let* ((v (value: data))
               (c (color-between red-color green-color v))
               (t (exact-health: (context-frame: data))))
          (on-device 'rgb-per-key-zones show-percent-on-zone: c v function-keys:)
          (on-device 'rgb-2-zone show-on-zone: c two:)
          (on-device 'rgb-1-zone show-on-zone: c one:)
          (on-device 'screened show-text-on-zone: t one:))))
    
    (add-event-per-key-zone-use "HEALTH" "function-keys")

Line-by-line breakdown:

1. First, we use the `handler` primitive to define a lambda function that will execute when event data is received.  The handler takes two parameters: the name of the event for which to register, and a single-parameter lambda function.  The single parameter accepted by the lambda function is a frame containing the data sent by the event.

2. Next, we use a let statement to define some local variables.  `v` is defined as the contents of the slot in the data frame with the name `value`.  _Note_: Any events that are configurable via SteelSeries Engine and not handled purely through GoLisp must contain a slot named `value`.  

3. The preferred way to send additional context data to your handlers is to send the key `frame` in the event data.  Any JSON data contained in the value of this key is bound under `context-frame` in the data passed to the handler.  You can see `exact-health` being read from this context data in the example.

4. Then the `color-between` function is used to calculate a color between red and green.  The color is calculated along a red-to-green gradient, using the value of `v` as a percentage value to select an intermediate color on the gradient.  The resulting color will be pure red at the value 0, pure green at 100, or varying colors in between.

5. The following three lines apply this color to several different device types using the `on-device` function.  The first call applies to devices with per-key illumination (e.g. the Apex M800), and applies the color as a percentage bar across the zone containing the function keys.  The second call applies to devices with exactly 2 zones of RGB illumination, and applies the color to the second zone on each device of that type.  The final call applies to devices with exactly 1 zone of RGB illumination, and applies the color to the first (and only) zone on each device of that type.

6. The next line takes the exact health that was read from the context data frame passed into the handler, and sends this text to the first zone of all devices with OLED or LCD screens.

7. The last line of this example declares that the `HEALTH` event utilizes the `function-keys` zone on per-key-illuminated devices.  This is necessary for initialization of the proper keys on the Apex M800 (and other future per-key-illuminated devices).

Detailed explanations of all of the GameSense™-exclusive primitives and functions are available in the rest of this document.  Documentation for the rest of the primitives is in [the GoLisp documentation][golisp documentation].

## Support code ##

The primitives and functions made available with GameSense™ fall into four main categories.  General event and zone handling, device management (sending illumination), color specification/manipulation, and visual effects and flashing.  We will look at each.

### General event and zone handling ###

#### Handler registration and dispatch ####

**`handler`** `<event name>` `<1 parameter lambda function>`

The `handler` primitive registers a lambda function to an event.  After being registered, the lambda function will be called every time data for that event is received.  The parameter that will be passed to the lambda function is a frame containing key-value pairs corresponding to the data sent by the event.

The name of the event corresponds to the same rules governing the names that can be sent for events, namely that they are limited to the character set of uppercase A-Z, 0-9, hyphen, and underscore.

    (handler "HEALTH" (lambda (data)
      (let ((v (value: data)))
        ;; Now you can use the value to do stuff
        )))


**`handler-with-post-event`** `<event name>` `<1 parameter lambda function>`

The `handler-with-post-event` primitive takes the same parameters and behaves nearly identically to the `handler` primitive.  The difference between the two is that after handlers finish running for the registered event, another event is automatically dispatched.  The extra event will have "POST-" prepended to the event name passed in, and lambda functions registered for this new event will recieve the same data as the original event.

    (handler-with-post-event "HEALTH" (lambda (data)
      ;; Handle health event
      ))
    (handler "POST-HEALTH" (lambda (data)
      ;; Specialized post-processing code here
      ;; This handler is guaranteed to run only after all handlers for the previous event finish running
      ))


**`handle-event`** `<event name>` `<data frame>`

The `handle-event` primitive explicitly dispatches another event with an arbitrary data frame.  All handlers for this dispatched event will run before this primitive returns.

    (handler "OLD-EVENT" (lambda (data)
      ;; Do some things before dispatching the new event
      (handle-event "NEW-EVENT" {value: 69})
      ;; Do more things after the new event is dispatched and fully handled
      ))

#### Current environment

**`game-sandbox`**

The `game-sandbox` primitive returns the sandboxed environment your handler code is executing in as a first-class environment object.  See the environments section of [the GoLisp language reference][golisp language ref] for information on primitives that operate on environments.

    ;; Print all symbols bound in the sandbox environment
    (map write-line (environment-bound-names (game-sandbox)))

#### Custom zone management ####

_Note:_: Custom zones are only valid on per-key-illuminated devices (e.g. the Apex M800)

**`add-custom-zone`** `<zone definition list>`

The `add-custom-zone` primitive registers a named zone in your sandboxed environment, associating the name with a group of HID key codes.  The zone definition list passed in contains the name of the zone (as a string), followed by each of the HID key codes you want to be present in the zone.  If this primitive is used, all `on-device` calls in your file will be able to use the zone name specified for per-key-illuminated devices.

    (add-custom-zone '("my-name-as-a-zone" 13 18 8 15))

    ...
    ;; Light up my name up in white
    (on-device 'rgb-per-key-zones show-on-zone: white-color "my-name-as-a-zone")


**`define-custom-zones`** `<list of zone definition lists>`

The `define-custom-zones` primitive is an alternate form for the `add-custom-zone` primitive, allowing the registration of multiple named zones in one call.

    (define-custom-zones '(("my-name-as-a-zone" 13 18 8 5) ("another-zone" 29 18 17 8)))

#### Device initialization and deinitialization ####

**`add-event-per-key-zone-use`** `<event name>` `<zone name>`

The `add-event-per-key-zone-use` primitive is required for device initialization purposes when writing handlers that use per-key-illuminated devices.  The system uses these declarations to know what zones are actually in use by handlers - only the zones in use will be initialized for writing when GameSense™ initializes.  The event name and zone name should be passed in as strings.

    (handler "HEALTH" (lambda (data)
      (on-device 'rgb-per-key-zones show-percent-on-zone: red-color (value: data) "function-keys")
      ;; Other on-device calls for other device types, other code, etc.
      ))

    ;; Required to initialize this zone on the M800
    (add-event-per-key-zone-use "HEALTH" "function-keys")


**`all-events-use-this-per-key-zone`** `<zone name>`

The `all-events-use-this-per-key-zone` primitive is a shortcut for the above, and associates a particular zone with all events.  This is particularly useful if you are writing an application or game where all events cause slightly different things to happen within the same defined area on the keyboard.

    ;; This is the verbose version
    (add-event-per-key-zone-use "HEALTH" "function-keys")
    (add-event-per-key-zone-use "AMMO" "function-keys")
    (add-event-per-key-zone-use "ARMOR" "function-keys")

    ;; This is the shortcut
    (all-events-use-this-per-key-zone "function-keys")

**`add-event-zone-use-with-specifier`** `<event name>` `<zone name>` `<specifier name>`

The `add-event-zone-use-with-specifier` primitive is required for SteelSeries Engine configuration purposes when writing custom GoLisp Handlers. The system uses these declarations to know what zones are actually in use by handlers so the user can still configure other zones from Engine.  The event name and zone name should be passed in as strings and the specifier name should be passed in as a string or quoted symbol.

    (add-event-zone-use-with-specifier "HEALTH" "one" "rgb-2-zone")

    ;; This is also acceptable
    (add-event-zone-use-with-specifier "HEALTH" "one" 'rgb-2-zone)

**`event-autoinit-exclusion`** `<list of event names>`

By default, receiving the data for any event will cause devices to initialize in GameSense™ mode.  If there are events that you want to handle to setup state, do pre-processing, or otherwise do anything that is not yet displayed on devices, you can use the `event-autoinit-exclusion` primitive to name the events that should not cause devices to initialize.

    (event-autoinit-exclusion '("START"))


**`deinitialize-timer-length`** `<timer length in ms>`

By default, GameSense™ will deinitialize devices after 15 seconds without receiving any events.  If you need to adjust this timer to a higher value, you can use this primitive to do so.  The maximum value is 60000 (1 minute).

### Device management ###

**`on-device`** `<device type specifier>` `<interface function>` `[argument]...`

All device management is centralized through the single function `on-device`.  Various values can be passed to each parameter to display data in different ways on different devices.

For a list of device types valid for the first parameter, see the [list of standard device types and zones][zones-types]

#### Common device interface functions ####

The device interface functions below are valid values for the second parameter of the `on-device` call.  Each function lists its required arguments that should be passed in.

If a connected device doesn't support the requested fuction, it's simply ignored. For example, the Apex M800 has individually controlled key illumination and supports `show-on-key:` while the Apex does not. This lets us write handlers without having to worry about what devices are actually connected.

A given device command is implemented in device specific ways to get the most out of each device's capabilities. For example, the Apex and Apex M800 keyboards both support `show-percent-on-zone:` (the Apex sets the color of the specified zone, and the M800 also uses the keys in that zone as a bar graph corresponding to the value.

**`show:`** `<color>`
Set the lighting on a single-zone device to the specified color.  For information on colors, see the section "Color Manipulation" below.

**`show-on-zone:`** `<color>` `<zone>`
Set the specified zone to the specied color.  For information on colors, see the section "Color Manipulation" below.  For information on available zones by device type, see the [list of standard device types and zones][zones-types].

**`show-percent-on-zone:`** `<color>` `<percent>` `<zone>`
Set the specified zone to the specified color, providing a percentage value as well.  On some devices, the percentage value will be used to select a subset of the zone to color as a bar graph.

**`show-between-on-zone:`** `<zero color>` `<hundred color>` `<percent>` `<zone>`
As above, but providing the colors for zero and one hundred percent values rather than an explicit color. The color used corresponds to that percentage on the gradient between the two colors specified.

**`show-count-on-zone:`** `<color>` `<count>` `<zone>`
Similar to the percentage displays, but uses the provided count as a measure of how much of the zone to color.  This function is only available on per-key-illuminated devices.

**`show-on-keys:`** `<list of keys>` `<list of colors>`
This is the lowest level way to set key colors on per-key-illuminated devices. `keys` is a list of HID key codes, and `colors` is a list of color tuples. The two lists must be the same length, and corresponding elements from each specify a key and the color to set it to. This gives you ultimate flexibility.

**`vibrate:`** `<vibration event list>`
Plays a series of vibrations on a device with a single vibration motor.

**`vibrate-on-zone:`** `<vibration event list>`
Plays a series of vibrations on a vibration zone on a device.  For information on available zones by device type, see the [list of standard device types and zones][zones-types].

**`show-text:`** `<text string>`
Shows text on the screen of a device with a single embedded OLED or LCD screen.

**`show-text-on-zone:`** `<text string>`
Shows text on a screen of a device with one or more embedded OLED or LCD screens.  For information on available zones by device type, see the [list of standard device types and zones][zones-types].

**`show-text-with-icon-on-zone:`** `<text string>` `<icon id>` `<zone>`
Shows an icon and text on a screen of a device with one or more embedded OLED or LCD screens.  For information on available zones by device type, see the [list of standard device types and zones][zones-types].  For a list of icon ids, see [Event icons][event-icons].

Examples:

    ;; Set all single-zone devices to red
    (on-device 'rgb-1-zone show: red-color)

    ;; Set earcup illumination of all headsets to white
    (on-device 'headset show-on-zone: white-color earcups:)

    ;; Show blue on 37% of per-key-illuminated devices' function key zone
    (on-device 'rgb-per-key-zones show-percent-on-zone: blue-color 37 "function-keys")

    ;; Show a color that is 37% between red and green on 37% of per-key-illuminated devices' function key zone
    (on-device 'rgb-per-key-zones show-between-on-zone: red-color green-color 37 'function-keys)

    ;; Show yellow on the first 3 keys in the QWERTY row on per-key-illuminated devices
    (on-device 'rgb-per-key-zones show-count-on-zone: yellow-color 3 q-row:)

    ;; Show red, green, blue on the keys Y,E,P
    (on-device 'rgb-per-key-zones show-on-keys: '(28 8 19) '(red-color green-color blue-color))

_Note:_: You will notice that the zone values used in these examples were specified in 3 different ways.  As a string ("function-keys"), as a quoted symbol ('function-keys), and as a self-evaluating symbol (function-keys:).  All of these are equally valid ways to reference zones, and merely a stylistic choice on your part when writing your handlers.

#### Zones ####

Many Steelseries products have multiple LEDs that are indpendantly controlled. We call these _illumination zones_. When you are setting colors from a handler, naturally you can specify what zone is to be effected.

Things get complicated when you consider that the Apex keyboard has 5 zones, while the M800 has over one hundred (since a zone can be a single key). In order to be able to work with zones in a device independent way, we refer to them symbolicly and leave the details up to the device.

For full information on available zones by device type, see the [list of standard device types and zones][zones-types].

As much as possible, we tried to degrade gracefully when some devices are less capable than others.  For example, the M800 has a zone called `number-keys:` for the keys `1`-`0` on the main keyboard area. The APEX doesn't have that as a separate zone, so if you use the `number-keys:` zone on an APEX, it will use the main keyboard block of keys.

If your handlers will be making use of predefined zones on the M800 you need to declare them so that they will be automatically initialized for your use. Include something like the following, changing event and zones as appropriate:

    (add-event-per-key-zone-use "HEALTH" "number-keys")
    (add-event-per-key-zone-use "AMMO" "function-keys")

Devices with per-key illumination (e.g. the Apex M800) support custom zones.  See the `add-custom-zone` and `define-custom-zones` primitives above for details. Devices with fixed zones simply ignore anything to do with custom zones.  

### Color manipulation ###

#### Colors ####

There are several color constants predefined for your use:

| symbol        | list          | css       |
|:-------------:|:-------------:|:---------:|
| black-color   | (0 0 0)       | `#000000` |
| white-color   | (255 255 255) | `#ffffff` |
| red-color     | (255 0 0)     | `#ff0000` |
| green-color   | (0 255 0)     | `#00ff00` |
| blue-color    | (0 0 255)     | `#0000ff` |
| yellow-color  | (255 255 0)   | `#ffff00` |
| cyan-color    | (0 255 255)   | `#00ffff` |
| magenta-color | (255 0 255)   | `#ff00ff` |
| orange-color  | (255 127 0)   | `#ff7f00` |


Custom colors can be specified in three ways:

**a list**
: of red, green, and blue byte values. E.g. `'(0 234 56)`

**an integer**
: that is the 24 bit value of the combined RGB values,   typically in hex. E.g. `0x00EA38`

**a string**
: representing the CSS color. E.g. `"#00EA38"`

The SDK uses colors in the `(R G B)` format. The predefined color constants are ready to use, but if you have a color not already in `(R G B)` list format, or you're not sure if it is, use the `color->list` function to convert it before use. This function will convert the three above formats to the canonical list format.

    (color->list "#00EA38") ==> (0 234 56)

The individual red, green, and blue values can be extracted using functions `red`, `green`, and `blue`:

    (red '(1 2 3))   ==> 1
    (green '(1 2 3)) ==> 2
    (blue '(1 2 3))  ==> 3

You can use `with-red`, `with-green`, and `with-blue` to replace the red, green, or blue value of a color with another value:

    (with-red '(1 2 3) 42)   ==> (42 2 3)
    (with-green '(1 2 3) 42) ==> (1 42 3)
    (with-blue '(1 2 3) 42)  ==> (1 2 42)

These `with-` functions return a new color list containing the changed value.

#### Gradients ####

Something that is very useful is to compute a color at some point on the gradient between two others.

**`color-between`** `<zero percent color>` `<hundred percent color>` `<percentage>`

    (color-between '(255 0 0) '(0 255 0) 50) ==> (127 127 0)

    (map (lambda (x)
           (color-between '(255 0 0) '(0 255 0) x))
         (interval 0 100 10))

    ((255 0 0)
     (229 25 0)
     (204 51 0)
     (178 76 0)
     (153 102 0)
     (127 127 0)
     (101 153 0)
     (76 178 0)
     (50 204 0)
     (25 229 0)
     (0 255 0))


#### Blending ####

Sometimes you will want to blend two colors, using some percentage of the second.

**`blend`** `<color 1>` `<color 2>` `<percentage>`

`blend` takes some percentage of color-2 and adds it to color-1, returning the result.

For example, it you wanted to fade to white you would blend white into your color, with increasing values of `percentage`.

    (map (lambda (i)
           (blend red-color white-color i))
         (interval 0 100 10))

    ((255 0 0)
     (255 25 25)
     (255 51 51)
     (255 76 76)
     (255 102 102)
     (255 127 127)
     (255 153 153)
     (255 178 178)
     (255 204 204)
     (255 229 229)
     (255 255 255))

### Visual effects ###

#### Flashing ####

Flashing an indicator is a handy way to draw attention to an extreme condition such as low health.

Create a flasher by making a frame with a proto*: slot set to the global `Flasher` frame.  (You can view flasher.lsp to see how this base flasher is defined.)

There are several function slots you can override to customize the behavior of your flasher:

**`compute-color:`** `<value>`

This function is invoked every time the value of the flasher changes, or when a recompute is forced.  It is a lambda function that takes the new value as a parameter, and returns the appropriate color.  The default for this function treats the value as a percentage and returns the color that is that percentage between the values of the `zero-color:` and `hundred-color:` slots.

**`compute-period:`** `<value>`

This function is invoked every time the value of the flasher changes, or when a recompute is forced.  It is a lambda function that takes the new value as a parameter, and returns the appropriate period value for the flasher (the number of milliseconds between toggles).  The default for this function simply returns the value of the `period:` slot as described below.

**`update-color:`** `<color>` `<value>`

This function is invoked every time the flasher is toggled on or off.  It is a lambda function that takes two parameter, a color and a value.  It is responsible for displaying the information appropriately on all devices.  

_Note:_: When the flasher is toggled on, the color passed to this function will be the value returned by the last call to the function `compute-color:` for the value specified.  When toggled off, the color passed in will be the value of the `off-color:` slot.

**`cleanup-function:`** `<value>`
This function is invoked as the last thing the flasher does as it is stopping. This function is optional and defaults to having no behavior. Takes the current value as its single parameter.


There are several data slots that can be overriden to tweak the behavior of the flasher:

**`off-color:`**
The color to use when the flasher is in the off part of its duty cycle. This defaults to `black-color`.

**`zero-color:`**
The color corresponding to a value of 0. Default is `black-color`.

**`hundred-color:`**
The color corresponding to a value of 100. Default is `white-color`.

**`period:`**
The number of milliseconds between toggles. Default is 250 ms, which corresponds to a 500 ms cycle time, and so a 2Hz flash frequency.

**`auto-enable:`**
Defaults to `#f`. If set to `#t`, the flasher will automatically be enabled if `compute-period` returns a non-zero value, and disabled otherwise.

The function slots you override or add also have access to a handful of useful slots in the `Flasher` prototype frame:

**`is-on:`**
This is the current on/off state of the flasher's cycle (using `#t` and `#f`, respectively). The flasher toggles beteen off and on every `period` milliseconds while it is running. When the `cleanup-function:` slot is invoked `is-on:` will be `#t`.

**`color:`**
This contains the color that was returned by the `compute-color:` function the last time it was invoked.

**`value:`**
: is the value of the flasher, an integer in the range 0-100. This value determines the value of the `color:` slot by using the `compute-color:` function and the flash frequency by using the `compute-period:` function.

You control the flasher with a small number of messages:

**`set-value:`** `<value>`
Takes an integer value that with the default function behavior should be between 0 and 100, inclusive. This is the value that determines the current _on_ color of the flasher as well as its period.

**`force-recompute:`**
Causes the `compute-color:` and `compute-period:` functions to be re-invoked to calculate new values.  If the flasher is auto-enabled, this will cause it to start if the new calculated period is greater than 0.  If the flasher is on (or was just auto-enabled), the update function will run.

**`start:`**
Causes the flasher to begin running and toggling.  No arguments.

**`stop:`**
Causes the flasher to stop running and toggling.  No arguments.

**`enable:`** `<should be enabled>`
Takes a boolean argument which indicates whether the flasher should start or stop. This is an alternative to calling `start:` and `stop:` explicitly. Handy if you have a boolean condition that controls whether the flasher should run.

You can ignore `start:`, `stop:`, and `enable:` if you have set `auto-enable:` to `#t`.

Here's an example:

    (define adventure-health-flasher
      {
        proto*: Flasher
        period: 100
        zero-color: red-color
        hundred-color: green-color
        update-color: (lambda (c health)
                        (on-device 'rgb-per-key-zones show-percent-on-zone: c health function-keys:)
                        (on-device 'rgb-2-zone show-on-zone: c one:)
                        (on-device 'rgb-1-zone show: c))
        cleanup-function: (lambda ()
                            (update-color color value))
        })

And here's how it could be used:

    (handler "ADVENTURE" "HEALTH"
             (lambda (data)
               (let ((health (value: data)))
                 (send adventure-health-flasher set-value: health)
                 (send adventure-health-flasher enable: (< health 20)))))

This will make the health indicators flash when the value of health goes below 20.

The flasher enablement could be moved into the flasher itself, by making use of `compute-period:` and `auto-enable:`:

    (define adventure-health-flasher
      {
        proto*: Flasher
        auto-enable: #t
        zero-color: red-color
        hundred-color: green-color
        compute-period: (lambda (health)
                          (if (< health 20) 100 0))
        update-color: (lambda (c health)
                        (on-device 'rgb-per-key-zones show-percent-on-zone: c health function-keys:)
                        (on-device 'rgb-2-zone show-on-zone: c one:)
                        (on-device 'rgb-1-zone show: c))
        cleanup-function: (lambda ()
                            (update-color color value))
        })


    (handler "ADVENTURE" "HEALTH"
             (lambda (data)
               (send adventure-health-flasher set-value: (value: data))))

## Support Code ##

Anything you need to define in support of your handlers, you can simply define as you normally would. This includes flashers as above, custome colors, or support functions. Each game has it's own sandboxed environment which acts like a namespace. You don't have to worry about names you define getting confused with anything defined for a different game.

## Best Practices ##

### Provide startup and shutdown events ###

Using startup and shutdown events is a handy way to configure the environment for your game. Shutdown events can be used to do any clean up required, or just to deinitialize immediately rather than waiting for the timer to expire:

    (handler "STOP" (lambda (data)
      (send Generic-Initializer deinitialize:)
    ))

### Send absolute values, not deltas ###

It's better to send event data that are absolute values rather than deltas. For example, send the new value of health, rather than an amount of damage or healing. This avoids having to keep track of the underlying value in your handlers and removes the potential for it to get out of sync with the state of your game.

Your game keeps track of its state, there's no reason your event handlers should as well.

### Send single values ###

If possible, each event should have a single value, preferrably an integer, and preferrable as a percentage value. This makes handlers far easier to write.

In this ideal world, the data payload with the event should be a JSON object with a single key of `"value"`. E.g.

    { "value": 42 }

### Specify devices by their illumination type rather than their base type ###

When you write event handler code, you provide a device type when sending illumination data.  It is preferred to use the types `rgb-2-zone`, `rgb-per-key-zones`, etc. when specifying devices rather than `keyboard`, `mouse`, etc.  Different keyboards and mice have different illumination characteristics, but each illumination type has a distinct set of zones and characteristics.


[golisp frames]: http://techblog.steelseries.com/2014/10/15/golisp-frames.html
[golisp documentation]: http://techblog.steelseries.com/golisp/documents.html
[golisp language ref]: http://techblog.steelseries.com/golisp/language-ref.html
[HID reference]: http://www.usb.org/developers/hidpage/Hut1_12v2.pdf
[json api doc]: /doc/api/writing-handlers-in-json.md
[api doc]: /doc/api/sending-game-events.md
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
[event-icons]: /doc/api/event-icons.md
[server discovery]: /doc/api/sending-game-events.md#server-discovery
