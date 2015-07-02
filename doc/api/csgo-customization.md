# Customizing CS:GO Integration #

This document is meant as an add-on to the *Writing Handlers in GoLisp* document. You will need to familiarize your self with that as well as (to a degree based on how fancy you want to get) [the GoLisp documentation][golisp documentation] and [the GoLisp frames documentation][golisp frames].

## CS:GO data frame ##

The data argument to each of these events is a GoLisp frame which contains two slots:

### `value:` ###

This is the numeric value of the event, E.g. the new value of your health.

### `frame:` ###

This is the complete data frame received from the game client.  You can use this if you need to fetch more information. See the example below of a kill count handler that uses the headshot count as well to show both values on one set of keys.

There is a wealth of data in this frame, likely more than you will ever need. There are three parts you might want to consider when handling the CS:GO events. All are part of the player information which you can fetch using: `(player: (frame: data))`.

The most useful data is the player state, available from `(state: (player: (frame: data)))`. This is, itself, a GoLisp frame that contains the following slots:

Slot name | Value range | Value meaning
`health:` | 0-100 | Percent of full health, 0 = dead.
`armor:` | 0-100 | Percent of full armor protection, 0 = unprotected.
`helmet:` | #t/#f | Whether or not you are wearing a helment.
`flashed:` | 0-100 | The strength of the flash. Normally 0, jumps to 100 when you are flashed, then fades back to 0.
`money:` | 0-16000 | How much money you have.
`round_kills:` | 0-? | The number of kills you have made in the current round.
`round_killhs:` | 0-? | The number of headshots you have made in the current round.

You also have access to information on all weapons you are carrying, in `(weapons: (player: (frame: data)))`.  This contains a frame with a slot for each weapon you have. These are named `weapon_N` where `N` has the values 0, 1, 2, etc. The following example shows what can be expected. Name, type, and state are always present but the ammo related slots vary based on the weapon.

Slot name | Value range | Value meaning |
`name:` | a string | The internal name of the weapon.
`type:` | a string | The type of weapon.
`state:` | "holstered"/"reloading"/"active" | The current state of the weapon. Only one weapon will be active at a given time.
`ammo_clip:` | 0-? | How many bullets remain in the weapon's clip. Not present for non-gun weapons.
`ammo_clip_max:` | 0-? | How many bullets the weapon's clip will hold. Not present for non-gun weapons.
`ammo_reserve:` | 0-? |Extra ammo you have that you can use to reload your weapon.

    weapons: {
      weapon_0: {
        name: "weapon_knife_gut"
        type: "Knife"
        ammo_reserve: 0
        state: "holstered"
      }
      weapon_1: {
        name: "weapon_glock"
        type: "Pistol"
        ammo_clip: 20
        ammo_clip_max: 20
        ammo_reserve: 120
        state: "holstered"
      }
      weapon_2: {
        name: "weapon_c4"
        type: "C4"
        ammo_reserve: 0
        state: "active"
      }
    }

100 * `ammo_clip`/`ammo_clip_max` will give you a percentage measure of how full your current clip is. This is where the value passed to `UPDATE-AMMO` comes from.

You can also get statistics for the entire match to the current time at `(match_stats: (player: (frame: data)))`. This is self explanitory:

    match_stats: {
      kills: 0
      assists: 0
      deaths: 1
      mvps: 0
      score: 0
    }


## Events ##

The CS:GO support provides seven events that can be handled. The table below gives the name of each, the range of the `(value: data)` payload, and it's significance.

Event Name | Value Range | Value Meaning
`UPDATE-HEALTH` | 0-100 | Percent of full health, 0 = dead.
`UPDATE-ARMOR` | 0-100 | Percent of full armor protection, 0 = unprotected.
`UPDATE-HELMET` | #t/#f | Whether or not you are wearing a helment.
`UPDATE-FLASHED` | 0-100 | The strength of the flash. Normally 0, jumps to 100 when you are flashed, then fades back to 0.
`UPDATE-MONEY` | 0-16000 | How much money you have.
`UPDATE-ROUND_KILLS` | 0-? | The number of kills you have made in the current round. (note the dash/underscore)
`UPDATE-AMMO` | 1-100 | How full (as a percentage) your clip is. The value is nil if you are not holding a weapon with ammo.

## Example ##

Here is an example of layering headshots and other kills with the kill count update.

    (handler "CS:GO" "UPDATE-ROUND_KILLS"
             (lambda (data)
               (let ((kills (value: data))
                     (headshots (min (list
                                  kills 
                                  (if (round_killhs:? (state: (player: (frame: data))))
                                    (round_killhs: (state: (player: (frame: data))))
                                    0))))
                     (non-headshots (- kills headshots))
                     (no-hits (- 5 kills))
                     (colors (append (make-list headshots red-color)
                                     (make-list non-headshots white-color)
                                     (make-list no-hits black-color))))
                 (write-line (str "Updating kills to " kills " with " headshots " headshots"))
                 (on-device 'rgb-per-key-zones show-on-keys: (interval 0xE9 0xED) colors))))


[golisp documentation]: http://techblog.steelseries.com/golisp/documents.html
[golisp frames]: http://techblog.steelseries.com/2014/10/15/golisp-frames.html
