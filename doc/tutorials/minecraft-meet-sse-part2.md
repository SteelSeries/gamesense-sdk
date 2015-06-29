## Introduction ##

Welcome to part 2 of the SteelSeries GameSense™ Minecraft modding tutorial! This tutorial builds on the code from the [previous tutorial](minecraft-meet-sse.md) so it is assumed you have done the first part. With GameSense™ you aren't limited to using JSON for handlers. For more advanced functionality you can use GoLisp to write event handlers. Here you will see how to implement the same functionality in the previous tutorial but this time using GoLisp.

## GoLisp. Go what? ##

There is a high chance that you have never heard of GoLisp. What is GoLisp? GoLisp is a version of Scheme written to provide an extension/scripting language to SteelSeries Engine 3. If you have worked with Lisp, or Scheme, it should be familiar. If you have no knowledge of Lisp or Scheme, fear not, it's not all that complicated. There are a number of tutorials and guides for working with Lisp or Scheme online. It is also recommended to read the ["writing handlers in GoLisp" documentation](../api/writing-handlers-in-golisp.md).

## Defining a handler ##

It is recommended to use an editor that can do Lisp or Scheme syntax highlighting as GoLisp has similar syntax. Create a new file, and enter this code for a `HEALTH` handler:

```lisp
;;; Minecraft Meet SSE3 - Part 2. GoLisp handlers

;;;
;;; handlers
;;;
(handler "HEALTH"
  (lambda (data)
    (let ((health (value: data)))
      (on-device 'keyboard show-percent-on-zone: red-color health function-keys:))))

(add-event-per-key-zone-use "HEALTH" "all")
```

Save the file anywhere, and name it `myminecraftmod.lsp`. _NOTE: The name of the .lsp file must match the name of the game we send with the event in our mod. It is not case-sensitive._
That is all that is required to define a handler named "HEALTH" in GoLisp. Handlers are given a `data` frame, that has a slot named `value` that will be used as a percentage. The percentage is used to determine how much red to show on the function-keys on keyboards.
Adding the event to the "all" zone, causes the entire keyboard to go dark, and only the function keys will be used to show the red color for health.

## hax0rBindings ##

Now that you have a custom GoLisp handler file, put it where SteelSeries Engine 3 will find it. Copy the `myminecraftmod.lsp` file to the path appropriate for your OS:

Platform | Path
-------- | ----
Windows  | `%ProgramData%\SteelSeries\SteelSeries Engine 3\hax0rBindings`
Mac      | `/Library/Application Support/SteelSeries Engine 3/hax0rBindings`

In order to reload the handler files, SteelSeries Engine 3 needs to be restarted. To do this, right click on the SteelSeries icon in the system tray/menu and choose exit/quit.

## Modify the Minecraft mod ##

Since the new handler is now in GoLisp, it is no longer necessary to setup the handler using JSON. Modify our mod code so that `SetupHandlers()` isn't called in the `load()` of the mod's main class. You can also completely delete the `SetupHandlers()` method. The event name is the same, so no changes to `MyEventReceiver` class are needed.
```java
@Mod(modid = MyMod.MODID,
	 name = MyMod.NAME,
	 version = MyMod.VERSION)
public class MyMod {
	
	...

	@EventHandler
    public void load(FMLInitializationEvent event) {
    	FindSSE3Port();
    	// --SetupHandlers(); deleted--
    }

    ...

    public void executePost(String extraAddress, String jsonData) {
    ...
	}
	// --SetupHandlers deleted--
}
```

## Test it out ##
Everything is now ready to test again. Build the Minecraft mod, drop the `.jar` in the `/mods` folder, and fire up SteelSeries Engine 3 and Minecraft.
If you don't see the health on the function-keys when entering a world in Minecraft, you can check the `Logs/golisp-log.txt` to make sure there wasn't anything wrong with the GoLisp handler. The error logs are located in:

Platform | Path
-------- | ----
Windows  | `%ProgramData%\SteelSeries\SteelSeries Engine 3\Logs`
Mac      | `/Library/Application Support/SteelSeries Engine 3/Logs`


## Getting fancy ##

Aside from being slightly less code, the handler that you just implemented in GoLisp is practically the same as what was set up using JSON. In this next section, we will begin to explore why you may want to use GoLisp instead of JSON to create handlers. Since GoLisp is a programming language, not just a data-interchange format, it allows for logic and interaction between handlers.

Another status used in Minecraft is hunger. The hunger bar in the HUD has 2 pieces of information. The level of hunger, indicated by how full the bar is, and if your character is hungry at the moment, indicated by a shake of the bar. Add the following code to `myminecraftmod.lsp` for a hunger handler:

```lisp
(define hunger-flasher {
    proto*: Flasher
    auto-enable: #f
    ;; Flash faster when really hungry. 
    compute-period: (lambda (hunger-percent)
                        (if (< hunger-percent 20) 250 100))
    compute-color: (lambda (hunger-percent) brown-color)
    update-color: (lambda (color hunger-percent)
                        (on-device 'keyboard show-percent-on-zone: color hunger-percent number-keys:))
    cleanup-function: (lambda (value)
                        (update-color color value))})

(handler "HUNGERLEVEL"
    (lambda (data)
        (let ((hungerlevel (value: data)))
            (send hunger-flasher set-value: hungerlevel))))

```

If you have read [writing handlers in GoLisp](\doc\api\writing-handlers-in-golisp.md) you will recognize the Flasher prototype. Essentially, this code defines a hunger-flash frame that uses a `Flasher` as its prototype. The `Flasher` prototype has built-in functionality for both displaying a value as a bar across a zone and flashing based on other parameters. For now, just the level of hunger is set on the bar across the number keys. 

In order to see this in action, the mod will also need to send the HUNGERLEVEL event. Add the following to the `onLivingUpdate` method:

```java
public void onLivingUpdate(LivingUpdateEvent event) {
...

    if ((curTick - this.lastTickMS > 2000) || player.getFoodStats().getFoodLevel() != this.lastFoodLevel) {
      this.lastFoodLevel = player.getFoodStats().getFoodLevel();
      int hungerLevelPct = 5 * this.lastFoodLevel;
      
      // Send HUNGERLEVEL event
      JSONObject hungerlevelEvent = new JSONObject();
      hungerlevelEvent.put("game", "MYMINECRAFTMOD");
      hungerlevelEvent.put("event", "HUNGERLEVEL");
      JSONObject hungerlevelEventData = new JSONObject();
      hungerlevelEventData.put("value", hungerLevelPct);
      hungerlevelEvent.put("data", hungerlevelEventData.toString());

      MyMod.instance.executePost("/game_event", hungerlevelEvent.toString());

      // reset lastTickMS whenever we send an update
      this.lastTickMS = curTick;
    }
}
```

You will also need to add `lastFoodLevel` to the `MyEventReceiver` class:

```java
public class MyEventReceiver {
  // MyEventReceiver, the main class of our mod to receive events
  private boolean isStarted = false;
  private Minecraft mcInst;
  private float lastHealth = 0;
  private long lastTickMS;
  private int lastFoodLevel = 0; 
  
  ...
  
  // Call this method to reset state
  public void reset() {
    this.isStarted = false;
    this.lastHealth = 0;
    this.lastTickMS = 0;
    this.lastFoodLevel = 0;
  }
  ...
```

Build the jar (with `$ ./gradlew build jar`) and copy to the mods folder again. Start up a Minecraft world again, and you should now see the hunger level displayed as a percentage on the number keys.

## Make it blink and stuff ##

The secondary piece of information the hunger bar in Minecraft displays is when your character is hungry. It indicates this by shaking the bar in the HUD. This state can be obtained by calling `player.getFoodStats().needFood()`. `needFood()` returns a boolean that can be used to turn on and off the flashing of the `hunger-flasher` in our GoLisp code.
Add the following handler to `myminecraftmod.lsp` that receives the `HUNGRY` state and turns on or off the hunger-flasher:
```lisp
(handler "HUNGRY"
  (lambda (data)
    (let ((is-hungry (value: data)))
      (if is-hungry
        (send hunger-flasher start:)
        (send hunger-flasher stop:)))))
```

Now add the following code to `onLivingUpdate` to send the `HUNGRY` event:
```java
public void onLivingUpdate(LivingUpdateEvent event) {
  ...

  if ((curTick - this.lastTickMS > 2000) || player.getFoodStats().needFood() != this.isHungry) {
    this.isHungry = player.getFoodStats().needFood();
    // Send HUNGERLEVEL event
    JSONObject hungryEvent = new JSONObject();
    hungryEvent.put("game", "MYMINECRAFTMOD");
    hungryEvent.put("event", "HUNGRY");
    JSONObject hungryEventData = new JSONObject();
    hungryEventData.put("value", this.isHungry);
    hungryEvent.put("data", hungryEventData.toString());

    MyMod.instance.executePost("/game_event", hungryEvent.toString());

    // reset lastTickMS whenever we send an update
    this.lastTickMS = curTick;
  }
}

```

Don't forget to add the new state variable `isHungry` to the `reset()` method and `MyEventReceiver`.
Build the jar, make sure your `myminecraftmod.lsp` is updated in `hax0rBindings`, restart SteelSeries Engine 3 and Minecraft, and you should now see the hunger bar on the keyboard flash when the HUD shakes. Run around a bit in Minecraft to get your hunger level down and trigger `needFood()`.

## Hungry for more! ##

Now that you have an understanding of how to do more advanced handlers using GoLisp, it is highly recommended to read the [api docs](/docs/api) if you haven't already done so.
Also, here are some exercises if you're looking for more ways to get familiar:
 * Refactor `onLivingUpdate()` to use a single variable instead of always checking `curTick - this.lastTickMS > 2000`
 * Add a method to `MyMod` for sending an event. _HINT: implement a method with the signature `public void SendGameEvent(String eventName, String jsonData)`_
 * Add a handler for Armor, Air, or any other stats you find the `player` object has in Java.

 
