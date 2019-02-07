# SteelSeries GameSense™ SDK #

GameSense™ is a framework in SteelSeries Engine 3 that allows games to
send status updates to Engine, which can then drive illumination (and
potentially more) capabilities of SteelSeries devices. One simple
example would be displaying the player's health on the row of
functions keys as a bargraph that gets shorter and changes from green
to red as their health decreases, even flashing when it gets
critically low.

This repository contains documentation, tutorials, and examples for
developers wishing to support GameSense™ in their games or
applications.

## Documentation ##

[**`doc/api/sending-game-events.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/sending-game-events.md)
How a game can register and send events to GameSense™.

[**`doc/api/writing-handlers-in-json.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/writing-handlers-in-json.md)
How to specify event handlers in JSON from a game for an
out-of-the-box, user customizable experience.

[**`doc/api/json-handlers-color.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-color.md)
Specifics on the JSON API for controlling device illumination.

[**`doc/api/json-handlers-screen.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-screen.md)
Specifics on the JSON API for controlling OLED screens
on supported devices.

[**`doc/api/json-handlers-tactile.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-tactile.md)
Specifics on the JSON API for controlling tactile feedback
on supported devices.

[**`doc/api/writing-handlers-in-golisp.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/writing-handlers-in-golisp.md)
How to write handlers in the GoLisp language for the ultimate
flexibility and power.

[**`doc/api/standard-zones.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/standard-zones.md)
A reference document with the list of standard zones that can
be used in handlers.

[**`doc/api/event-icons.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/event-icons.md)
A reference document with the list of event icons that can be
used with OLED screen handlers.

## Tutorials ##

[**`doc/tutorials/audiovisualizer_tutorial.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/audiovisualizer_tutorial.md)
Turn your APEX M800 into an audio spectrum analyzer.

[**`doc/tutorials/minecraft-meet-sse.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/minecraft-meet-sse.md)
Writing a mod for MineCraft to support GameSense™.

[**`doc/tutorials/minecraft-meet-sse-part2.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/minecraft-meet-sse-part2.md)
Writing advanced handlers to work with the events being sent from the mod
presented in part 1.

[**`doc/tutorials/creating-a-minecraft-mod.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/creating-a-minecraft-mod.md)
Support tutorial on modding MineCraft.

[**`doc/api/csgo-customization-with-golisp.md`**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/csgo-customization-with-golisp.md)
How to customize the builtin CS:GO event handling using GoLisp. Read
the `doc/api/writing-handlers-in-golisp.md` first.

## Sample code ##

[**`examples/audiovisualizer`**](https://github.com/SteelSeries/gamesense-sdk/tree/master/examples/audiovisualizer)
Code to go with `doc/tutorials/audiovisualizer_tutorial.md`.

[**`examples/minecraftforge1.8`**](https://github.com/SteelSeries/gamesense-sdk/tree/master/examples/minecraftforge1.8)
Code for the [GameSense™ Minecraft mod](http://www.technicpack.net/modpack/steelseries-gamesensetm.675193)

