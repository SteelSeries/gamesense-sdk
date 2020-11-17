# SteelSeries Moments Event Support #

Moments is a new service from SteelSeries GG that allows users to capture gameplay clips, edit them, and share them with the community. When Moments game capture is enabled users can manually clip the game they're playing via a shortcut. Devs can utilize timeline events to mark relevant game events on the clip editing timeline. This makes it easy for users to find the exact moment cool things happen in your game, so they can more easily edit and share the clip.

![timeline events](/images/timelineevents/timeline_events.png)

**This feature requires SteelSeries GG which can be downloaded [here](https://steelseries.com/gg/moments)**

Moments APIs utilize the same server as GameSense events, so adding support for Moments in your existing GameSense game is very simple. You can learn more about server discovery and GameSense events [here](sending-game-events)

## Timeline Events ##

Timeline events work by registering a list of existing GameSense events that you want to show on the timeline. This data should be sent via HTTP POST to `http://127.0.0.1:<port>/register_timeline_events`. The event list keys correspond to normal GameSense events you have registered via `register_game_event` or `bind_game_event`. 

The `previewable` param corresponds to showing a preview of the event icon in the clip gallery view:
![icon_preview](/images/timelineevents/thumbnail_icons.png)

```
`game`: <string>                        registered GameSense game name        mandatory
`events`: <timeline-events-definition>  list of events to show on timeline    mandatory
```

_timeline-events-definition_
```
`icon_id`: <timeline-event-icon>  see list of available icons     optional - defaults to 'DEFAULT'
`previewable`: <int> (0-1)        previewable in clip thumbnail   optional - defaults to 0
`localized_text_key`: <string>    localization key                optional*
``` 
* localization is not currently available to 3rd party developers.

### Example Timeline JSON ###

Here is an example of a timeline registration payload:
```json
{
  "game": "MY_GAME",
  "events": [
    {
      "event": "MY_EVENT_1",
      "icon_id": "KILL",
      "previewable": 1
    },
    {
      "event": "MY_EVENT_2",
      "icon_id": "DEATH",
      "previewable": 0
    }
  ]
}
```

### Timeline Icons ###
![DEATH](/images/timelineevents/icons/Death.png) DEATH 

![KILL](/images/timelineevents/icons/Kill.png) KILL

![HEADSHOT](/images/timelineevents/icons/Headshot.png) HEADSHOT

![STAR](/images/timelineevents/icons/Star.png) STAR

![WIN](/images/timelineevents/icons/Win.png) WIN

![DEFAULT](/images/timelineevents/icons/Default.png) DEFAULT
