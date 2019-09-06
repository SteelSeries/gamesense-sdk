# JSON handlers for Full-Keyboard Lighting Effects

Note: This document describes the specific handler format for full-keyboard effect handlers.  See [Writing Handlers in JSON][json-handlers] for a general overview of how to bind handlers.  See [JSON Color Handlers][json-handlers-color] for specifics on color and lighting handlers for specific keys and zones.

Bitmap-type handlers are used to simultaneously control each key on RGB per-key-illuminated keyboards.

## Full schema definition ##

Each portion is described in detail in later sections.

Top-level schema

```
`device-type`: 'rgb-per-key-zones'            mandatory.  This type of lighting is currently only supported for the rgb-per-key-zones device type.
`mode`: `bitmap` | `partial-bitmap`           mandatory
`excluded-events`: [<event name string>...]   optional, only supported for partial-bitmap.
```

## Full keyboard effects ##

The `bitmap` visualization mode is a special case that can be used to individually control an entire keyboard's lighting each update.  To use this mode, your event data must include a context frame data key named `bitmap`.  This key's value must be a 132-length array of colors, each of which should be a 3-length array specifying R, G, and B values.  This array is interpreted as a 22x6 grid that is automatically mapped to the nearest appropriate keys on the user's keyboard.  The first color specified corresponds to the top left of the keyboard, the 23rd color corresponds to the left side of the second row, etc.   Any parts of the array that do not map to a key on the user's keyboard are ignored.

For details on sending context data with events, see [Event context data](/doc/api/sending-game-events.md#event-context-data).

It is also recommended to set the value_optional flag when binding a bitmap type event, so that you can omit the "value" key from the data and only send the relevant bitmap information.  See [Registering an event](/doc/api/sending-game-events.md#registering-an-event).

Example partial event update payload (assuming value_optional):
```
{
  "game": "MY_GAME",
  "event": "BITMAP_EVENT",
  "data": {
    "frame": {
      "bitmap": [
        [255,0,0],   // Color for top left
        [255,255,0], // Color for second part of top row
        ...          // 130 more colors 
      ]
    }
  }
}
```

## Full keyboard background effects ##

The `partial-bitmap` visualization mode is another special case lighting type.  It functions similarly to the `bitmap` mode, but takes a list of events to exclude from what is being overwritten by the bitmap data sent.  This can be used to create full-keyboard effects that function as a background on top of which your other effect updates are overlaid.  The keys that are excluded are the keys that are currently assigned to the events specified, whether those are the defaults specified in your handler definitions or user-customized zones.

For example, the below handler definition will write to all keys except those used by AMMO and HEALTH events (assuming those are also defined): 

```json
{
  "device-type": "rgb-per-key-zones",
  "mode": "partial-bitmap",
  "excluded-events": ["AMMO", "HEALTH"]
}
```

It is also possible to re-use a background effect event with a different set of excluded events, rather than needing to create a new one for each different combination of exclusions.  To do this, in addition to passing a `bitmap` context frame key, you must also include a `excluded-events` key.  This key/value functions exactly like the one on the handler definition itself, taking an array of event names as strings.  If this key is present in the context frame, the value in the frame will override the default provided during handler binding.  If not present, that default will be used.

To send an update to the event above that overwrites AMMO and only leaves HEALTH visible, send the following data
```
{
  "game": "MY_GAME",
  "event": "BITMAP_EVENT",
  "data": {
    "frame": {
      "bitmap": [...], // 132 colors etc.
      "excluded-events": ["HEALTH"]
    }
  }
}
```

[json-handlers]: /doc/api/writing-handlers-in-json.md "Writing Handlers in JSON"
[json-handlers-color]: /doc/api/json-handlers-color.md "JSON Color Handlers"
[api doc]: /doc/api/sending-game-events.md "Event API documentation"
[zones-types]: /doc/api/standard-zones.md "Device types and zones"
