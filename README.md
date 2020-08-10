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

[**How to write full-keyboard lighting effects**](https://github.com/SteelSeries/gamesense-sdk/blob/master/doc/api/json-handlers-full-keyboard-lighting.md)

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

## Submitting a game or app for approval ##
### You actually don't need to submit anything to us.
Feel free to release your own app, and anyone who has your game/app installed should see it appear as a GameSense App inside of SteelSeries Engine.  We want smaller devs to be able to develop their implementation completely on their own if they so choose.

### Want to partner up and have a full curated experence for our mututal users?
Great, we do too.  [Contact us through this form](https://steelseries.com/developer/contact-us) and we'll get back to you right away.  We can add images, text, links, and other info about your project.  We can also help you with implementation, answer questions, and a lot more.

#### Project info we'll ask for:
* **Game/app title**
* **Name of your organization/company**
* **Link text & URL**
* **Short description**: for App tile, cannot exceed 200 characters
* **App tile image**: 330 pixels x 200 pixels as a PNG, containing your logo with a solid background
* **GameSense customization page image**: 200 pixels x 50 pixels as a PNG, containing your logo with a fully transparent background
