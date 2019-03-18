# SteelSeries GameSense™ SDK #
GameSense™ is a framework in SteelSeries Engine that allows games & apps
to send status updates to Engine, which can then drive illumination,
haptic & OLED display capabilities of SteelSeries devices. One simple
example would be displaying the player's health on the row of
functions keys as a bargraph that gets shorter and changes from green
to red as their health decreases -- even flashing when it gets
critically low.

This repository contains documentation, tutorials, and examples for
developers wishing to support GameSense™ in their games or
applications.

## Documentation ##

### Getting Started
[**How a game can register and send events to GameSense™**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/sending-game-events.md).

[**How to specify event handlers in JSON from a game**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/writing-handlers-in-json.md), for an out-of-the-box user customizable experience.

[**How to control device illumination**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-color.md)

[**How to control OLED screens**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-screen.md)

[**How to control tactile feedback**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-tactile.md)


### GoLisp Handlers
[**How to write handlers in GoLisp**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/writing-handlers-in-golisp.md), for the ultimate flexibility and power.

### Reference
[**List of standard zones that can be used in handlers**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/standard-zones.md)

[**List of event icons that can be used with OLED screen handlers**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/event-icons.md)


## Tutorials ##
[**Turn an RGB device into an audio spectrum analyzer**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/audiovisualizer_tutorial.md), using the GoLisp handlers

[**Writing a mod for MineCraft to support GameSense™**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/minecraft-meet-sse.md)

[**Writing a mod for MineCraft to support GameSense™, Part 2**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/minecraft-meet-sse-part2.md), writing advanced handlers to work with the events being sent from the mod presented in part 1.

[**Support tutorial on modding MineCraft**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/tutorials/creating-a-minecraft-mod.md)

[**Customizing built-in CS:GO event handling with GoLisp**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/csgo-customization-with-golisp.md), read (https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/writing-handlers-in-golisp.md) first.

## Sample Code ##

[**`examples/audiovisualizer`**](https://github.com/SteelSeries/gamesense-sdk/tree/master/examples/audiovisualizer)
Code to go with `doc/tutorials/audiovisualizer_tutorial.md`.

[**`examples/minecraftforge1.8`**](https://github.com/SteelSeries/gamesense-sdk/tree/master/examples/minecraftforge1.8)
Code for the [GameSense™ Minecraft mod](http://www.technicpack.net/modpack/steelseries-gamesensetm.675193)

