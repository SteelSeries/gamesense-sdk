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

`rgb-zoned-device`
: A generic specifier that applies to any connected, supported RGB device that has a static number of lighting zones.  This can be used to apply settings to a certain zone on all of the types of devices in the list below at once.  When using this type, a handler will be created for each type below that has the sepcified zone. 

<details>
  <summary>Static RGB zone device types</summary>

  1. `rgb-1-zone`: Any connected, supported, single zone RGB device.  Covers the Siberia Elite line of headsets, Siberia v3 Prism, Sims 4 line of products, Rival 100, and Rival 110.

  2. `rgb-2-zone`: Any connected, supported, dual zone RGB device.  Covers the Rival, Rival 300, Rival 500, and Rival 700 mice lines, Arctis 5, Arctis Pro, QCK Prism Cloth line of mousepads.

  3. `rgb-3-zone`: Any connected, supported, three zone RGB device. Covers the Sensei Wireless mouse and MSI 3 Zone RGB Keyboard.

  4. `rgb-5-zone`: Any connected, supported, five zone RGB device. Covers the Apex 150, Apex 300, and MSI GT72 keyboards.

  5. `rgb-8-zone`: Any connected, supported, six zone RGB device.  Covers the Rival 600 and Rival 650 mice.

  6. `rgb-12-zone`: Any connected, supported, twelve zone RGB device.  Covers the QCK Prism mousepad.

  7. `rgb-17-zone`: Any connected, supported, seventeen zone RGB device.  Covers the MSI Z270 Gaming Pro Carbon motherboard.

  8. `rgb-24-zone`: Any connected, supported, twenty-four zone RGB device.  Covers the MSI Mystic Light.

  9. `rgb-103-zone`: Any connected, supported, one hundred three zone RGB device.  Covers the MSI MPG27C and MPG27CQ monitors.
</details>

`rgb-per-key-zones`
: Any connected, supported, keyboard with a lighting zone for each key.  Covers the Apex M800, Apex M750 and 750 TKL, and MSI RGB Per Key keyboards.

## Device types for tactile notifications ##

`tactile`
: Any connected, supported device that supports a single zone for tactile feedback.  Covers the Rival 500, Rival 700, and Rival 710.

Note: Engine 3.7.0 and later

## Device types for OLED/LCD screen notifications ##

`screened`
: Any connected, supported device that supports notifications on a single OLED or LCD screen.  Covers the Rival 700, Rival 710, Arctis Pro Wireless, and GameDAC.

Note: Engine 3.7.0 and later

`screened-WIDTHxHEIGHT`
: Any connected, supported device that supports notifications on a single OLED or LCD screen that has the specified resolution in pixels.  Initially, the type `screened-128x36` matches the Rival 700.

Note: Engine 3.8.0 and later

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

## Static RGB zoned devices (`rgb-zoned-device` and each individual type e.g. `rgb-1-zone`) ##

Note: Each of these types supports up to the number of zones specified in its name, except `rgb-zoned-device` which can be used with any of them.

<details>
	<summary>
* `one`
* `two`
Click to expand the full list
</summary>
* `three`
* `four`
* `five`
* `six`
* `seven`
* `eight`
* `nine`
* `ten`
* `eleven`
* `twelve`
* `thirteen`
* `fourteen`
* `fifteen`
* `sixteen`
* `seventeen`
* `eighteen`
* `nineteen`
* `twenty`
* `twenty-one`
* `twenty-two`
* `twenty-three`
* `twenty-four`
* `twenty-five`
* `twenty-six`
* `twenty-seven`
* `twenty-eight`
* `twenty-nine`
* `thirty`
* `thirty-one`
* `thirty-two`
* `thirty-three`
* `thirty-four`
* `thirty-five`
* `thirty-six`
* `thirty-seven`
* `thirty-eight`
* `thirty-nine`
* `forty`
* `forty-one`
* `forty-two`
* `forty-three`
* `forty-four`
* `forty-five`
* `forty-six`
* `forty-seven`
* `forty-eight`
* `forty-nine`
* `fifty`
* `fifty-one`
* `fifty-two`
* `fifty-three`
* `fifty-four`
* `fifty-five`
* `fifty-six`
* `fifty-seven`
* `fifty-eight`
* `fifty-nine`
* `sixty`
* `sixty-one`
* `sixty-two`
* `sixty-three`
* `sixty-four`
* `sixty-five`
* `sixty-six`
* `sixty-seven`
* `sixty-eight`
* `sixty-nine`
* `seventy`
* `seventy-one`
* `seventy-two`
* `seventy-three`
* `seventy-four`
* `seventy-five`
* `seventy-six`
* `seventy-seven`
* `seventy-eight`
* `seventy-nine`
* `eighty`
* `eighty-one`
* `eighty-two`
* `eighty-three`
* `eighty-four`
* `eighty-five`
* `eighty-six`
* `eighty-seven`
* `eighty-eight`
* `eighty-nine`
* `ninety`
* `ninety-one`
* `ninety-two`
* `ninety-three`
* `ninety-four`
* `ninety-five`
* `ninety-six`
* `ninety-seven`
* `ninety-eight`
* `ninety-nine`
* `one-hundred`
* `one-hundred-one`
* `one-hundred-two`
* `one-hundred-three`
</details>

## `rgb-per-key-zones` ##

Note: The Apex M800 and M750 are full-size keyboards.  The Apex M750 TKL is a tenkeyless keyboard, and does not include any zone referencing the keypad.

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

Note: All current OLED devices have a single screen.  This may change in the future, introducing new zones.

* `one`

Available WIDTHxHEIGHT device type specifiers:

* `screened-128x36`: Rival 700, Rival 710
* `screened-128x48`: Arctis Pro Wireless
* `screened-128x52`: GameDAC / Arctis Pro + GameDAC
