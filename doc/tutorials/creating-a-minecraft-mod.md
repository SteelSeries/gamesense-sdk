# Creating a Minecraft Mod Using MinecraftForge #

Welcome to Creating a Minecraft Mod Using MinecraftForge. The purpose of this tutorial is to show you what is involved in setting up the environment and building a mod.

## Put on our toolbelt ##

Creating a Minecraft mod requires the JDK, an SDK and/or a mod loader. There are a handful of options to create a mod for Minecraft, but for this tutorial, MinecraftForge and Eclipse will be used. One of the nice things about Java is that it's available on many platforms, so this tutorial is applicable to the platform you are working in. Windows 7+ or Mac OS X 10.8+ are recommended.

Install these first:

* [Latest Minecraft launcher](https://minecraft.net) *(Use the .jar under "Minecraft for Linux/Other" not .app on Mac to be able to use with JDK 1.8)*
* [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) *(8u45 at time of writing)*
* [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/) *(Luna version at time of writing)*

## Installing Forge ##

Download the recommended installer and src for [MinecraftForge for Minecraft 1.8](http://files.minecraftforge.net) *(11.14.1.1334 at time of writing)*

![](/images/minecraftmod/minecraftforge-files.png)

Prior to installing Forge, you will need to run Minecraft 1.8 at least once. Forge will also remind you of this if you forget.

Run the Minecraft launcher (NOTE: as a reminder, if you are on Mac OS X, it is recommended to use the minecraft.jar for this tutorial, which is the Linux/Other download of Minceraft. Not the .dmg and .app.)

![](/images/minecraftmod/newprofile.png)

![](/images/minecraftmod/select1.8.png)

Save Profile then click Play. Once Minecraft has fully launched, you can close it.

Now install Forge. Running the Forge installer, you will be prompted to install client, server, or extract. For the intents of this tutorial, only the client is needed.

![](/images/minecraftmod/forgeinstaller.png)

## Setting Up A Forge Project ##

Now that Forge is installed, extract the forge source zip file (not to be confused with the Extract option from the installer) to a desired location. This is where the majority of work on the mod will be done. Also feel free to rename the folder to the name of your mod. For this tutorial, *forge-mod* will be used.

![](/images/minecraftmod/extract.png)

![](/images/minecraftmod/rename.png)

The Forge SDK uses an open source build automation tool called *gradle*. If you are already familiar with gradle, great. If not, do not worry, it is fairly simple to use as long as you are comfortable using the command line.

Now on to generating the Eclipse workspace.

Open a command prompt (Cmd.exe or Terminal.app) and `cd` to the forge source folder from earlier.

`$ cd forge-mod`

To generate the workspace, use the `gradlew` build script (`gradlew.bat` on Windows):

`$ ./gradlew setupDecompWorkspace eclipse`

This command will link with Minecraft and create the appropriate Eclipse workspace files. You are now ready to begin work on your mod in Eclipse.

## Creating MyMod With Eclipse ##

Launch Eclipse, and if this is the first time launching it, you will be prompted to select a workspace when it starts. If you don't get the workspace prompt, select File -> Switch Workspace -> Other... to bring up the dialog.

The Eclipse workspace is in the `forge-mod/eclipse` folder. You can either type in the path to it, or click `Browse...` and navigate to choose the `eclipse` folder that is in the root of `forge-mod`.

![](/images/minecraftmod/selectworkspace.png)

You should now see a window that looks something like this (expand the Minecraft node in Package Explorer):

![](/images/minecraftmod/eclipseworkspace.png)

Forge includes an `ExampleMod` class you may use as reference. It is not used in this tutorial, so you may remove it if you like. 

Right-click on src/main/java in Package Explorer and choose New -> Package:

![](/images/minecraftmod/newpackage.png)

Java recommends using the reverse-domain naming convention, so call the package `com.mymod.tutorial` then click Finish.

![](/images/minecraftmod/packagename.png)

You now have an empty package. This is where all the source code for the mod will go.
Create the main class for MyMod. Right-click on `com.mymod.tutorial` in the Package Explorer and choose New -> Class:

![](/images/minecraftmod/newclass.png)

Java recommends camel-case naming for classes, so let's name the class `MyMod` then click Finish.

![](/images/minecraftmod/classname.png)

Your workspace should now look like this:

![](/images/minecraftmod/mymodclass.png)

Now it's time to add strings to the `MyMod` class that will be used to register the mod with Forge. Add these to the `MyMod` class:

```java
public class MyMod {
  public static final String MODID = "mymod";
  public static final String NAME = "My Minecraft mod";
  public static final String VERSION = "1.0";
```

An annotation to the `MyMod` class is needed to tell Forge this is the base mod class:

```java
@Mod(modid = MyMod.MODID,
     name = MyMod.NAME,
     version = MyMod.VERSION)
public class MyMod {
```

At this point, you'll probably notice errors building or if you have _Build Automatically_ enabled by default in Eclipse. You can have Eclipse fix this by clicking on the icon in the margin and selecting the _import 'Mod'_ option:

![](/images/minecraftmod/fixerror.png)

Next, add the rest of the boilerplate code for a mod:

```java
  // Tell Forge what instance to use.
  @Instance(value = MyMod.MODID)
  public static MyMod instance;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
  }
  
  @EventHandler
  public void load(FMLInitializationEvent event) {
  }
  
  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
  }
```

Your `MyMod.java` file should now look like this:

```java
package com.mymod.tutorial;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MyMod.MODID,
  name = MyMod.NAME,
  version = MyMod.VERSION)
public class MyMod {
  public static final String MODID = "mymod";
  public static final String NAME = "My Minecraft mod";
  public static final String VERSION = "1.0";
  
  // Tell Forge what instance to use.
  @Instance(value = MyMod.MODID)
  public static MyMod instance;
  
  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
  }
  
  @EventHandler
  public void load(FMLInitializationEvent event) {
  }
  
  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
  }
}
```

## Build and test MyMod ##

Now that the main class is all set, modify the `build.gradle` file to match the main class's information.

Open up `build.gradle` and you should see these lines near the top:

```java
version = "1.0"
group= "com.yourname.modid" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
archivesBaseName = "modid"
```

Change version, group, and archivesBaseName to match VERSION, package name, and MODID from before. So your `build.gradle` should now contain:

```java
version = "1.0"
group= "com.mymod.tutorial"
archivesBaseName = "mymod"
```

Now you can build the jar file and test it out.
In a cmd prompt, run the `gradlew` script with `build` and `jar` as arguments:

`$ ./gradlew build jar` 

If there were no errors, you should now have a .jar file named `mymod-1.0.jar` in `forge-mod/build/libs`. In order for Forge to load the mod, it needs to be copied to the `mods` folder Forge creates in the .minecraft folder. The .minecraft folder location depends on your OS:
<table class="wikitable">
<tbody><tr>
<th>OS</th>
<th>Path</th>
</tr>
<tr>
<td>Windows</td>
<td><code>%appdata%\.minecraft</code></td>
</tr>
<tr>
<td>Mac&nbsp;OS&nbsp;X</td>
<td><code>~/Library/Application&nbsp;Support/minecraft</code></td>
</tr>
</tbody></table><BR>

![](/images/minecraftmod/copiedtomods.png)

(If there is no `mods` folder, create one.)

Once your .jar has been copied, launch Minecraft. Make sure you have the Forge profile selected before you click Play.

![](/images/minecraftmod/forgeprofile.png)

After you click Play and Minecraft launches, there should be a `Mods` button in the main menu. Clicking that will list the mods Forge has loaded. You should now see MyMod in that list:

![](/images/minecraftmod/mymodloaded.png)

Congratulations! You just created and ran a Forge mod.

## EXTRA: Add JSON dependency ##

SteelSeries Engine 3 GameSense™ events are formatted using JSON. So it is recommended to use the latest JSON library for Java. You can get the latest JSON.jar from the [mvnrepository](http://mvnrepository.com/artifact/org.json/json/20140107).

Put the JSON .jar file in a `jars` folder in the root of forge-mod:

![](/images/minecraftmod/jsoninjars.png)

Next, add the dependency to build.gradle and regenerate the Eclipse workspace.

Open up the `build.gradle` file again, and find the top-level section for `dependencies` (NOT `dependencies` under `buildscript`) :

```java
dependencies {
    // you may put jars on which you depend on in ./libs
    // or you may define them like so..
    //compile "some.group:artifact:version:classifier"
    //compile "some.group:artifact:version"
      ...
}
```

Add ```compile files('jars/json-20140107.jar')``` to the contents of `dependencies`:

```java
dependencies {
    // you may put jars on which you depend on in ./libs
    // or you may define them like so..
    //compile "some.group:artifact:version:classifier"
    //compile "some.group:artifact:version"
    compile files('jars/json-20140107.jar')
      ...
}
```

Exit Eclipse if it's still running and regenerate the Eclipse workspace and build.

`$ ./gradlew eclipse build`

You will also need to copy the JSON jar to the `mods` folder in `.minecraft`. You will only have to do that once, unless you use a newer version of the JSON jar.

![](/images/minecraftmod/jsoncopied.png)

## Connect to SteelSeries Engine 3 GameSense™ ##

Now that you have a development environment set up, check out the [Minecraft Meet SSE tutorial](minecraft-meet-sse.md) to learn how to create handlers and events for GameSense™. 
