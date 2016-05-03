# Device types #

A category of device is specified using the `"device-type"` key. The value of this key is either the name of a device category, either type or capability based:

## General device types ##

`keyboard`
: Any connected, supported keyboard. Initially the Apex M800, Apex 300, MSI GE62, and MSI GE72.

`mouse`
: Any connected, supported mouse. Initially the Rival, Dota 2 Rival, Sensei Wireless, and Sims 4 Mouse.

`headset`
: Any connected, supported headset. Initially the Siberia Elite line and Siberia v3 Prism.

`indicator`
: Any connected, supported simple indicator device. Initially the Sims4 Plumbob and Valve Dota 2 indicator.

## Device types by number of RGB zones ##

`rgb-1-zone`
: Any connected, supported, single zone RGB device. Initially the Siberia Elite line, Siberia v3 Prism, and Sims 4 line.

`rgb-2-zone`
: Any connected, supported, dual zone RGB device. Initially the Rival mouse.

`rgb-3-zone`
: Any connected, supported, three zone RGB device. Initially the Sensei Wireless mouse, the MSI GE62 keyboard, and the MSI GE72 keyboard.

`rgb-5-zone`
: Any connected, supported, five zone RGB device. Initially the Apex 300 keyboard.

`rgb-per-key-zones`
: Any connected, supported, keyboard with a lighting zone for each key. Initially the APEX M800 keyboard.

## Device types for tactile notifications ##

`tactile`
: Any connected, supported device that supports a single zone for tactile feedback.  Initially the Rival 700.

Note: Engine 3.7.0 and later

## Device types for OLED/LCD screen notifications ##

`screened`
: Any connected, supported device that supports notifications on a single OLED or LCD screen.  Initially the Rival 700.

`screened-WIDTHxHEIGHT`
: Any connected, supported device that supports notifications on a single OLED or LCD screen that has the specified resolution in pixels.  Initially, the type `screened-128x36` matches the Rival 700.

Note: Engine 3.7.0 and later

# Zones by device type #

Each device type has one or more predefined zones that will apply to all or most devices of that type (depending on exact illumination details).  For best results, it is recommended to use the `rgb-x-zone` and `rgb-per-key-zones` specifiers to handle events based on the type of lighting supported.  Using `keyboard`, `mouse`, and `indicator` is also possible but some devices will not support certain zones.

## `keyboard` ##

Note: On the Apex M800, the order of the keys in each zone is left to right and top to bottom unless otherwise specified.

* `function-keys`: F1 to F12 (Excludes MSI GE62 and GE72)
* `main-keyboard`: All keys included in the central area of the keyboard  (Excludes MSI GE62 and GE72)
* `keypad`: All keys in the numpad cluster (Excludes MSI GE62 and GE72)
* `number-keys`: 1-0 on the main keyboard area (Excludes MSI GE62 and GE72)
* `macro-keys`: The macro keys on the left side (Excludes MSI GE62 and GE72)

## `mouse` ##

* `wheel`: The mousewheel LED
* `logo`: The logo LED (excludes Sims 4 Mouse)
* `base`: The base station LED (Sensei Wireless only)

## `headset` ##

* `earcups`: The LEDs on the earcups

## `indicator` ##

* `one`: The main LED

## `rgb-1-zone`, `rgb-2-zone`, `rgb-3-zone`, `rgb-5-zone` ##

Note: Each of these types supports up to the number of zones specified in its name.

* `one`
* `two`
* `three`
* `four`
* `five`

## `rgb-per-key-zones` ##

Note: The Apex M800 is a full-size keyboard.  Future per-key-illuminated devices support only a subset of these zones.

Note: These zones are named based on the keycaps in the US English layout.  If you are developing using a different layout, the zone names you specify will not match your keycaps in all cases.

### Zones specifying various individual keys ###

* `logo`: The logo LED in the upper right
* `a`, `b`, `c`, ... , `z`: Letters in the main keyboard area
* `keyboard-1`, `keyboard-2`, ..., `keyboard-0`: The number keys in the main keyboard area
* `return`, `escape`, `backspace`, `tab`, `spacebar`, `caps`: Various named special keys
* `dash`, `equal`, `l-bracket`, `r-bracket`, `backslash`, `pound` (hashmark, not the currency), `semicolon`, `quote`, `backquote`, `comma`, `period`, `slash`: Various named symbol keys
* `f1`, `f2`, ..., `f12`: Individual function keys
* `printscreen`, `scrolllock`, `pause`, `insert`, `home`, `pageup`, `delete`, `end`, `pagedown`: Named special keys in the cluster above the arrow keys
* `rightarrow`, `leftarrow`, `downarrow`, `uparrow`: Arrow keys
* `keypad-num-lock`, `keypad-divide`, `keypad-times`, `keypad-minus`, `keypad-plus`, `keypad-enter`, `keypad-period`, `keypad-1`, ...,, `keypad-0`: Keypad/Numpad keys
* `l-ctrl`, `l-shift`, `l-alt`, `l-win`, `r-ctrl`, `r-shift`, `r-alt`, `r-win`, `ss-key`, `win-menu`: Modifier keys.  `ss-key` is the SteelSeries key, and `win-menu` refers to the application button.
* `m0`, `m1`, `m2`, `m3`, `m4`, `m5`: Macro keys on the left side

### Zones specifying groups of keys ###

* `function-keys`, `number-keys`, `q-row`, `a-row`, `z-row`: Horizontal rows of keys.  All except the function-keys row are 10 keys wide, starting at the specified key.
* `macro-keys`: Five macro keys, from M1 to M5.
* `all-macro-keys`: Six macro keys, from M0 to M5.
* `main-keyboard`: Everything in the central keyboard area (does not include function keys)
* `nav-cluster`: All nine keys above the arrow keys
* `arrows`: The arrow keys (right, left, down, up)
* `keypad`: The entire keypad/numpad area
* `keypad-nums`: Keypad/Numpad 1-9 (Starting at 1, sequentially to 9)
* `all`: Every key and LED on the keyboard

## `tactile`

Note: Currently the only supported tactile feedback device is the Rival 700, which has a single motor for the purpose.  More zones may be introduced in the future with new devices.

* `one`

## `screened`, `screened-WIDTHxHEIGHT`

Note: Currently the only supported OLED screen device is the Rival 700, which has a single OLED screen for notifications.  More zones may be introduced in the future with new devices.

* `one`
