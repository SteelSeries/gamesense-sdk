## Introduction ##

Welcome to the first SteelSeries GameSense™ tutorial. In this tutorial, you will learn how to get started using GameSense™ in your own Minecraft mod. You will see how to create handlers for Minecraft events like those that already exist in the [SteelSeries GameSense™ Mod](http://www.technicpack.net/modpack/steelseries-gamesensetm.675193). Then, you will get a walkthrough for sending events, and how to get more advanced with handling events. It's quite possible you will learn everything you need to know in this first part. If you don't know how to make a Minecraft mod, you can check out this [tutorial](creating-a-minecraft-mod.md) or search online for the many tutorials regarding Minecraft Mods. This tutorial assumes you own a copy of Minecraft, are familiar with Java, comfortable using a command prompt, and have some basic knowledge of Eclipse. The code examples were written using the MinecraftForge SDK, so if you are using another SDK, consult the documentation of the SDK you are using for equivalent events and methods.

## Connect to SteelSeries Engine 3 ##

To start, you need to know how to talk to SteelSeries Engine 3. SteelSeries Engine 3 listens on a random, unused port when it starts. Since SteelSeries Engine 3 doesn't use a static port, it creates a `coreProps.json` file when it runs that can be parsed to find out what port it is listening on. Here is a method to load and parse `coreProps.json`: 

_NOTE: Location of the `coreProps.json` file is in the code below._
```java

private void FindSSE3Port() {

  // Open coreProps.json to parse what port SteelSeries Engine 3 is listening on.
  String jsonAddressStr = "";
  String corePropsFileName;
  // Check if we should be using the Windows path to coreProps.json
  if(System.getProperty("os.name").startsWith("Windows")) {
    corePropsFileName = System.getenv("PROGRAMDATA") +
                        "\\SteelSeries\\SteelSeries Engine 3\\coreProps.json";
  } else {
    // Mac path to coreProps.json
    corePropsFileName = "/Library/Application Support/" + 
                        "SteelSeries Engine 3/coreProps.json";
  }

  try {
    BufferedReader coreProps = new BufferedReader(new FileReader(corePropsFileName));
    jsonAddressStr = coreProps.readLine();
    System.out.println("Opened coreProps.json and read: " + jsonAddressStr);
    coreProps.close();
  } catch (FileNotFoundException e) {
    System.out.println("coreProps.json not found");
  } catch (IOException e) {
    e.printStackTrace();
    System.out.println("Unhandled exception.");
  }

  try {
    // Save the address to SteelSeries Engine 3 for game events.
    if(!jsonAddressStr.equals("")) {
      JSONObject obj = new JSONObject(jsonAddressStr);
      sse3Address = "http://" + obj.getString("address");
    }
  } catch (JSONException e) {
    e.printStackTrace();
    System.out.println("Exception creating JSONObject from coreProps.json.");
  }
}

```

This method should be called in the `load` of your mod's main class. You will also want to add `String sse3Address;` to save the parsed values for use later.

```java
// Tell Forge what instance to use.
@Instance(value = MyMod.MODID)
public static MyMod instance;
private String sse3Address;
  
@EventHandler
public void preInit(FMLPreInitializationEvent event) {
}
    
@EventHandler
public void load(FMLInitializationEvent event) {
  FindSSE3Port();
}
```

## Create your first handler ##

GameSense™ uses JSON as the data format for creating handlers, and sending game events. If you already know what JSON is, great. If not, you can read about it at [JSON.org](http://www.json.org). Knowing JSON fully isn't a requirement, but will help you understand what we're going to do next.

First, make a method that can be used to post any JSON string to an URL on the port for SteelSeries Engine 3. It is recommended to make it public in your mod's main class so other classes can call it.

```java
public void executePost(String extraAddress, String jsonData) {
  try {
    URL url = new URL(sse3Address + extraAddress);
    // Create an HTTP connection to core
    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    // 1ms read timeout, as we don't care to read the result, just send & forget
    connection.setReadTimeout(1);
    connection.setUseCaches(false);
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    
    // Send the json data
    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
    byte[] data = jsonData.getBytes(Charset.forName("UTF-8"));
    wr.write(data);
    wr.flush();
    wr.close();
    // The following triggers the request to actually send. Just one of the quirks of HttpURLConnection.
    connection.getInputStream();
    // Done, make sure we disconnect
    connection.disconnect();
    
  } catch (Exception e) {
    // e.printStackTrace();
  }
}
```

The first argument to `executePost` is appended to `sse3Address` so that you can use this method for creating handlers with `"/bind_game_event"` and sending events with `"/game_event"`.

For handlers, the structure required is documented in [writing handlers in JSON](../api/writing-handlers-in-json.md). An event handler looks like this in JSON:

```JSON
{
  "game": "MYMINECRAFTMOD",
  "event": "HEALTH", 
  "handlers": [
    "{
      \"device-type\": \"keyboard\",
      \"zone\": \"function-keys\",
      \"color\": {
        \"gradient\": {
          \"zero\": {
            \"red\": 0,
            \"green\": 0, 
            \"blue\": 0
          },
          \"hundred\": {
            \"red\": 0, 
            \"green\": 255, 
            \"blue\": 0
          }
        }
      },
      \"mode\": \"percent\"
    }"
  ]
}  
```

It is recommended to use the JSON.org .jar for building JSON strings. See [creating a minecraft mod](creating-a-minecraft-mod.md) for more information on where to get the JSON.jar and how to link it in your MinecraftForge mod.
Here is a method that uses JSON classes to build a handler string:

```java
private void SetupHandlers() {
  System.out.println("Setting up JSON handlers");
  JSONObject healthBinding = new JSONObject();
  healthBinding.put("game", "MYMINECRAFTMOD");
  healthBinding.put("event", "HEALTH");
  // Add an array of strings for the "handlers" object
  JSONArray handlersArray = new JSONArray();
  
  JSONObject keyboardHandler = new JSONObject();
  keyboardHandler.put("device-type", "keyboard");
  keyboardHandler.put("zone", "function-keys");
  keyboardHandler.put("mode", "percent");
  
  JSONObject colorObj = new JSONObject();
  JSONObject gradientObj = new JSONObject();
  
  JSONObject zeroColor = new JSONObject();
  zeroColor.put("red", 255);
  zeroColor.put("green", 0);
  zeroColor.put("blue", 0);
  gradientObj.put("zero", zeroColor);
  
  JSONObject hundoColor = new JSONObject();
  hundoColor.put("red", 0);
  hundoColor.put("green", 255);
  hundoColor.put("blue", 0);
  gradientObj.put("hundred", hundoColor);
  colorObj.put("gradient", gradientObj);
  keyboardHandler.put("color", colorObj);
  // NOTE: handlersArray is an array of strings, not objects
  handlersArray.put(keyboardHandler.toString());
  healthBinding.put("handlers", handlersArray);
  executePost("/bind_game_event", healthBinding.toString());
}
```

Add `SetupHandlers()` to your mod's main class init method.

```java
@EventHandler
public void load(FMLInitializationEvent event) {
  FindSSE3Port();
  SetupHandlers();
}
```

Take a moment to digest setting up the handler you just added.

SteelSeries Engine 3 takes JSON data to define what happens when it receives an event. The handler definition is sent to the `/bind_game_event` path. The handler definition requires certain parts to be defined, so GameSense™ knows what to do when it gets that event. _Refer to the [writing handlers in JSON](../api/writing-handlers-in-json.md) documentation for more information_.

## Hooking up an event ##

Now that you know how to communicate with SteelSeries Engine 3 and have an event handler set up for HEALTH, it's time now to hook up an event that sends the player's health.

For this, it is recommended to create a new class that receives the events via Forge for player related state.
Here is an example class that can be used to get player events:

```java
package com.mymod.tutorial;

import net.minecraft.client.Minecraft;

public class MyEventReceiver {
  // MyEventReceiver, the main class of our mod to receive events
  private boolean isStarted = false;
  private Minecraft mcInst;
  private float lastHealth = 0;
  private long lastTickMS;
  
  public MyEventReceiver(Minecraft inst) {
    // Cache the instance to Minecraft in case we need to get world state.
    this.mcInst = inst;
    // Save the time in milliseconds as the first time this updates.
    lastTickMS = System.currentTimeMillis();
    
    this.reset();
  }
  
  // Call this method to reset state
  public void reset() {
    this.isStarted = false;
    this.lastHealth = 0;
    this.lastTickMS = 0;
  }
}

```

Make sure you register the class you just created with MinecraftForge's `EVENT_BUS`. Here is an example of registering a class in the `postInit` method:

```java
@EventHandler
public void postInit(FMLPostInitializationEvent event) {
  MinecraftForge.EVENT_BUS.register(new MyEventReceiver(Minecraft.getMinecraft()));
}
```

Now you are ready to get and send the player's health. Health should be sent as a percentage, with the event name "HEALTH" since that is how the handler was set up. In MinecraftForge's documentation, you can find events about health in the `onLivingUpdate` event. Here is an example of implementing an `onLivingUpdate` method. Put it in the class created to receive events.

```java
@SubscribeEvent(priority = EventPriority.NORMAL)
public void onLivingUpdate(LivingUpdateEvent event) {
}
```

This method is going to get called many times, since it is a generic event for every living thing in the game. If a world hasn't loaded yet, there is no need to check for events. Add events so that the event receiver starts/stops on map load/unload:

```java
@SubscribeEvent(priority = EventPriority.NORMAL)
public void onWorldLoad(WorldEvent.Load event) {
  this.isStarted = true;
}

@SubscribeEvent(priority = EventPriority.NORMAL)
public void onWorldUnload(WorldEvent.Unload event) {
  // Good time to reset, too
  this.reset();
}
```

In `onLivingUpdate` the only thing you need to check is the player's health, and when it changes. Here is code to check the event's entity and handle the player's health:

```java
@SubscribeEvent(priority = EventPriority.NORMAL)
public void onLivingUpdate(LivingUpdateEvent event) {
  if (!this.isStarted)
    return;
  
  if (event.entity instanceof EntityPlayerSP) {
    EntityPlayer player = (EntityPlayer) event.entity;
  
    if (player.getHealth() != this.lastHealth) {
      this.lastHealth = player.getHealth();
      // Calculate health as a percentage using an integer from 0-100.
      int healthPercent = 100 * ((int) this.lastHealth) / ((int)player.getMaxHealth());
      // Send HEALTH event
  }
}
```

Now that health is being tracked, it's time to create the `HEALTH` event and send it to `/game_event`.
Just like the handler, the event requires JSON structured data. The `HEALTH` event in JSON would look like this:
```JSON
{
  "game": "MYMINECRAFTMOD",
  "event": "HEALTH",
  "data": "{\"value\": 100}"
}
```

Add this after the `//Send HEALTH event` comment:

```java
JSONObject healthEvent = new JSONObject();
healthEvent.put("game", "MYMINECRAFTMOD");
healthEvent.put("event", "HEALTH");
JSONObject healthEventData = new JSONObject();
healthEventData.put("value", healthPercent);
healthEvent.put("data", healthEventData.toString());
MyMod.instance.executePost("/game_event", healthEvent.toString());
```

Note that events are being sent to `/game_event`. Any event that has been bound using `/bind_game_event` will be handled accordingly. For information regarding the structure of events refer to the [writing handlers in JSON](../api/writing-handlers-in-json.md) documentation.

Game event handlers, by default, will disable themselves after a duration of inactivity. If you want events to stay active, you will need to periodically send updates or a heartbeat event. One way to do this is to send the `HEALTH` event every 2 seconds or when it changes, whichever is sooner. Make `onLivingUpdate` now look like this:

```java
@SubscribeEvent(priority = EventPriority.NORMAL)
public void onLivingUpdate(LivingUpdateEvent event) {
  if (!this.isStarted)
    return;

  long curTick = System.currentTimeMillis();

  if (event.entity instanceof EntityPlayerSP) {
    EntityPlayer player = (EntityPlayer) event.entity;
  
    // Send health update every 2 seconds or when health changes, whichever comes first.
    if ((curTick - this.lastTickMS > 2000) || player.getHealth() != this.lastHealth) {
      this.lastHealth = player.getHealth();
      int healthPercent = 100 * ((int) this.lastHealth) / ((int)player.getMaxHealth());
      // Send HEALTH event
      JSONObject healthEvent = new JSONObject();
      healthEvent.put("game", "MYMINECRAFTMOD");
      healthEvent.put("event", "HEALTH");
      JSONObject healthEventData = new JSONObject();
      healthEventData.put("value", healthPercent);
      healthEvent.put("data", healthEventData.toString());

      MyMod.instance.executePost("/game_event", healthEvent.toString());

      // reset lastTickMS whenever we send a health update
      this.lastTickMS = curTick;
    }
  }
}
```

Now you should have everything set to test out sending the player's health to SteelSeries Engine 3 GameSense™. As your health goes down the color should go from green to red. On the M800, the number of keys lit will also decrease.

Congratulations! You now know everything needed to hook up game events in a Minecraft mod. If you are interested in more advanced handlers, and the term `hax0rBindings` doesn't scare you, check out [writing handlers in GoLisp](../api/writing-handlers-in-golisp.md).







