## Introduction

In this tutorial we will go over how to use GameSense™ in a webpage environment and how to write a relatively simple GoLisp handler. This tutorial assumes that you know JavaScript and enough Lisp to follow a step-by-step explanation. If you're lacking on that Lisp part, you can look through the [GoLisp Documents](http://techblog.steelseries.com/golisp/documents.html) or, if you have a little more free time, you can read through [SICP](https://mitpress.mit.edu/sicp/). :)

## Talking to GameSense™

GameSense™ works by sending events over HTTP, so doing so from a webpage is easy. This tutorial uses an already existing demo to get started, [HTML5 Audio Visualizer](https://github.com/Wayou/HTML5_Audio_Visualizer). There have been some small edits to the visualizer part of the code, but this tutorial will go over the GameSense™ parts.


The first thing we need to do is to figure out where the GameSense™ port is located. We do that through the coreProps.json file. This file is located in one of two places, depending on what platform you're on.

Platform | Path
-------- | ----
Windows  | `%ProgramData%\SteelSeries\SteelSeries Engine 3\coreProps.json`
Mac      | `/Library/Application Support/SteelSeries Engine 3/coreProps.json`

For GameSense™ version 1, we have to locate and parse this file manually. We can do that through an `<input>` element and some file parsing. The user manually selects the file and then we can automatically parse the correct path out of it:

```html
<label for="uploadedCoreProps">Select coreProps.json:</label>
<input type="file" id="uploadedCoreProps" accept=".json"></input>
```

```javascript
var sseAddress;
var corePropsInput = document.getElementById('uploadedCoreProps');
corePropsInput.onchange = function() {
    if (corePropsInput.files.length !== 0) {
        var corePropsFile = corePropsInput.files[0];
        // sanity check before attempted parsing
        if (corePropsFile.size < 100) {
            var reader = new FileReader();
            reader.onloadend = function() {
                var json = reader.result;
                try {
                    var corePropsObject = JSON.parse(json);
                    sseAddress = corePropsObject.address;
                } catch(e) {
                    console.error(e);
                }
            }
            reader.readAsBinaryString(corePropsFile);
        }
    }
}
```

We also need a function to create and send GameSense™ events:

```javascript
var GAME = "AUDIOVISUALIZER";

var music_event = function(values, cb) {
    var musicEventRequest = new XMLHttpRequest();
    var payload = {
        "game": GAME,
        "event": "AUDIO",
        "data": JSON.stringify({values: values})
    };
    try {
    	var url = "http://" + sseAddress + "/game_event";
        musicEventRequest.open("POST", url, true);
        musicEventRequest.setRequestHeader("Content-Type", "application/json");
        musicEventRequest.send(JSON.stringify(payload));
        musicEventRequest.onreadystatechange = function() {
            if (musicEventRequest.readyState == XMLHttpRequest.DONE) {
                cb();
            }
        };
    } catch (e) {
    	console.error(e);
    }
}
```

Both the game name and event name are all-caps with no spaces. The payload for game events are `POST`ed to `http://<GameSense™ address>/game_event` and follow this JSON payload format:

```json
{
    "game": "<Game Name>",
    "event": "<Event Name>",
    "data": "<Event Data>"
}
```

The event data itself is another JSON object, but serialized into a string. The exact format of this data is different based on the handler, but a common format used for single-value events is this:

```json
{
    "value": 123
}
```

However, since we have more than one number to send we have a custom format.

After this, we can use the demo's existing code for drawing the visualization and put our GameSense™ code in it. Inside `_drawSpectrum`, we can change the `drawMeter` code to this:

```javascript
var drawMeter = function() {
    var array = new Uint8Array(analyser.frequencyBinCount);
    analyser.getByteFrequencyData(array);
    if (that.status === 0) {
        //fix when some sounds end the value still not back to zero
        for (var i = array.length - 1; i >= 0; i--) {
            array[i] = 0;
        };
    };
    //var step = Math.round(array.length / meterNum); //sample limited data from the total array
    ctx.clearRect(0, 0, cwidth, cheight);
    var values = [];
    for (var i = 0; i < meterNum; i++) {
        var value = array[Math.floor(i * STEP + OFFSET)];
        values.push(value);
        ctx.fillStyle = gradient; //set the filllStyle to gradient for a better look
        ctx.fillRect(i * 12, cheight - value, meterWidth, cheight); //the meter
    }
    if (that.status === 1) {
        music_event(values, drawMeter);
    }
}
```

Not only does this code get values for sending to GameSense™, but also changes the `drawMeter` function to update only once the `music_event` function is done and calls a callback function. Since GameSense™ HTTP requests only return once the event is done and sent to the keyboard, we can sync on those events instead of the browser's `requestAnimationFrame`.

## Spooky Scary ~~Skeletons~~ GoLisp

This is where things get interesting. Instead of using simple JSON event handlers, the audio visualizer we're making is going to use fancy [GoLisp handlers](writing-handlers-in-golisp.md) with advanced functionality. [GoLisp](http://techblog.steelseries.com/golisp/) is a variant of Lisp based off of Scheme with some extra additions of its own.

GoLisp handlers have to be placed in a special directory for GameSense™ to use them, and have to be named a certain way. The filename must be `<Game name>.lsp` (case insensitive) and placed in the `hax0rBindings` directory. This directory, like `coreProps.json`, is in a different location depending on your platform:

Platform | Path
-------- | ----
Windows  | `%ProgramData%\SteelSeries\SteelSeries Engine 3\hax0rBindings`
Mac      | `/Library/Application Support/SteelSeries Engine 3/hax0rBindings`

The handler for the audio visualizer is as follows:

```scheme
(define custom-colors (list green-color green-color orange-color orange-color red-color red-color))

(define (percent value keys)
  (let* ((number-of-keys (length keys))
    (percent-per-key (/ 255.0 number-of-keys))
    (num-keys-lit (cond ((== value 0) 0)
                        ((<= value percent-per-key) 1)
                        (else (min (list number-of-keys
                             (integer (* (/ (+ percent-per-key value) 255.0)
                                         number-of-keys)))))))
    (remainder (- value (* percent-per-key (max (list (- num-keys-lit 1) 0)))))
    (remainder-scale (/ (* 255 remainder) percent-per-key))
    (scaled-final-key-color (map (lambda (color-bit)
                              (integer (/ (* remainder-scale color-bit) 255)))   (nth custom-colors num-keys-lit)))
    (key-colors (map (lambda (key-index)
                       (cond ((nil? (nth keys key-index)) '())
                          ((< key-index num-keys-lit) (nth custom-colors key-index))
                          ((== key-index num-keys-lit) scaled-final-key-color)
                          (else black-color)))
                     (interval 1 number-of-keys))))
    key-colors))

(define visualizer-columns '(
  (0xED 0xEC 0xEB 0xEA 0xE9 0xE8)
  (0xE0 0xE1 0x39 0x2B 0x35 0x29)
  (0xE3 ()   0x04 0x14 0x1E ()  )
  (()   0x1D 0x16 0x1A 0x1F 0x3A)
  (0xE2 0x1B 0x07 0x08 0x20 0x3B)
  (()   0x06 0x09 0x15 0x21 0x3C)
  (()   0x19 0x0A 0x17 0x22 0x3D)
  (()   0x05 0x0B 0x1C 0x23 ()  )
  (()   0x11 0x0D 0x18 0x24 0x3E)
  (()   0x10 0x0E 0x0C 0x25 0x3F)
  (()   0x36 0x0F 0x12 0x26 0x40)
  (0xE6 0x37 0x33 0x13 0x27 0x41)
  (0xEF 0x38 0x34 0x2F 0x2D 0x42)
  (0x65 0xE5 ()   0x30 0x2E 0x43)
  (()   ()   ()   ()   ()   0x44)
  (0xE4 ()   0x28 0x31 0x2A 0x45)
  (0x50 ()   ()   0x4C 0x49 0x46)
  (0x51 0x52 ()   0x4D 0x4A 0x47)
  (0x4F ()   ()   0x4E 0x4B 0x48)
  (0x62 0x59 0x5C 0x5F 0x53 ()  )
  (()   0x5A 0x5D 0x60 0x54 ()  )
  (0x63 0x5B 0x5E 0x61 0x55 ()  )
  (()   0x58 ()   0x57 0x56 0x00)
))

(handler "AUDIO"
  (lambda (data)
    (let* ((vals (values: data))
          (colors (map percent vals visualizer-columns))
          (filtered-zones (filter notnil? (reduce append '() visualizer-columns)))
          (filtered-colors (filter notnil? (reduce append '() colors))))
      (on-device "rgb-per-key-zones" show-on-keys: filtered-zones filtered-colors))))

(add-event-per-key-zone-use "AUDIO" "all")
```

Whoa! That's a lot of code! It's okay though, don't let all those parenthesis scare you. Let's make this easier and go through it piece-by-piece:

```scheme
(define custom-colors (list green-color green-color orange-color orange-color red-color red-color))
```

This defines a list of which colors we are going to use in which rows on the keyboard. They are arranged from bottom to top.

```scheme
(define (percent value keys)
```

This defines a function called `percent` with the parameters `value` and `keys`. All symbols in GoLisp are symbolic expressions (S-exprs) and can contain data or code alike. In this case, `value` is a numeric value  and `keys` is the list of keys. What this function will do is return a list of colors, but it will fade out colors depending on what `value` is. Lower `value` means less colors are full and higher `value` means more keys are full color. This is how we will draw the colored bars for our visualizer. Let's look deeper in the function:

```scheme
(let* ((number-of-keys (length keys))
```

`let` is a GoLisp primitive that takes a list of variables then evaluates code with the values of the variables supplied. Each variable can be either a constant or an expression that is evaluated. In this case, each step is a different variable and the code at the end simply returns the final result. The `*` in `let*` lets the interpreter know that each variable calculated takes the environment from the previous variable calculation, so we can use the variables together and don't have to chain `let` expressions to get our result.

The first variable is `number-of-keys` which is the number of keys in this set. The way the rest of the handler is set up this will always be 6, but it's nice to calculate it here in case we want to edit stuff in the future.

```scheme
(percent-per-key (/ 255.0 number-of-keys))
```

The next variable `percent-per-key` is the number of percentage "ticks" for each key.

```scheme
(num-keys-lit (cond ((== value 0) 0)
                    ((<= value percent-per-key) 1)
                    (else (min (list number-of-keys
                         (integer (* (/ (+ percent-per-key value) 255.0)
                                     number-of-keys)))))))
```

`num-keys-lit` is the total number of keys that are illuminated based off the number of keys and `value`.

```scheme
(remainder (- value (* percent-per-key (max (list (- num-keys-lit 1) 0)))))
(remainder-scale (/ (* 255 remainder) percent-per-key))
```

`remainder` is the value left on the last key. `remainder-scale` is the value of that scaled to a percentage between 0 and 255, used to calculate the final color.

```scheme
(scaled-final-key-color (map (lambda (color-bit)
                          (integer (/ (* remainder-scale color-bit) 255)))   (nth custom-colors num-keys-lit)))
```

`scaled-final-key-color` is the color value of the final key will have. The `map` primitive here takes a list (which is the color we want for the last row) and sends each item in the list to a function, which returns a new value. All the return values are combined into a new list. We use this to apply the `remainder-scale` value to the individual red, green, and blue values in the color to fade it out based on what the remainder is.

```scheme
(key-colors (map (lambda (key-index)
                   (cond ((nil? (nth keys key-index)) '())
                      ((< key-index num-keys-lit) (nth custom-colors key-index))
                      ((== key-index num-keys-lit) scaled-final-key-color)
                      (else black-color)))
                 (interval 1 number-of-keys))))
```

`key-colors` is a list containing all the final colors for the keys. Keys 1 to (`<last key>` - 1) are full color, `<last key>` is `scaled-final-key-color`, and keys after the last key are black. There is also a special case for keys that are nil: they have nil as a color as well, so the list of colors and list of keys have nils in the same positions. This will be useful later.

```scheme
key-colors))
```

This is the expression part of the `let` primitive. However, since we already calculated our return value, we can just place it here.

```scheme
(define visualizer-columns '(
  (0xED 0xEC 0xEB 0xEA 0xE9 0xE8)
  (0xE0 0xE1 0x39 0x2B 0x35 0x29)
  (0xE3 ()   0x04 0x14 0x1E ()  )
  (()   0x1D 0x16 0x1A 0x1F 0x3A)
  (0xE2 0x1B 0x07 0x08 0x20 0x3B)
  (()   0x06 0x09 0x15 0x21 0x3C)
  (()   0x19 0x0A 0x17 0x22 0x3D)
  (()   0x05 0x0B 0x1C 0x23 ()  )
  (()   0x11 0x0D 0x18 0x24 0x3E)
  (()   0x10 0x0E 0x0C 0x25 0x3F)
  (()   0x36 0x0F 0x12 0x26 0x40)
  (0xE6 0x37 0x33 0x13 0x27 0x41)
  (0xEF 0x38 0x34 0x2F 0x2D 0x42)
  (0x65 0xE5 ()   0x30 0x2E 0x43)
  (()   ()   ()   ()   ()   0x44)
  (0xE4 ()   0x28 0x31 0x2A 0x45)
  (0x50 ()   ()   0x4C 0x49 0x46)
  (0x51 0x52 ()   0x4D 0x4A 0x47)
  (0x4F ()   ()   0x4E 0x4B 0x48)
  (0x62 0x59 0x5C 0x5F 0x53 ()  )
  (()   0x5A 0x5D 0x60 0x54 ()  )
  (0x63 0x5B 0x5E 0x61 0x55 ()  )
  (()   0x58 ()   0x57 0x56 0x00)
))
```

`visualizer-columns` is a symbol that contains a list of lists! Each list represents a virtual column for our visualizer and contains a list of HID codes for each key in the column, listed from bottom to top. The HID codes are the same as the USB HID spec, with two exeptions: the SteelSeries key is HID code 0xEF and the logo area is HID code 0x00. We use nil lists `()` for padding purposes for when there isn't a key on that row of our column. Note that the whole thing has an apostrophe before it, this is a shortcut for the GoLisp `quote` primitive. This means that nothing inside these lists is evaluated. There is an issue with this though; symbols in the lists are not evaluated. Luckily this is not an issue for this case. If you want to use symbols in `quote`d lists, you have to remember to `eval` those symbols first.

```scheme
(handler "AUDIO"
  (lambda (data)
```

`handler` is a GameSense™ primitive that is used to define event handlers. It takes two parameters: the name of the event, and a function to call when the event is received. The `data` parameter for the function is the `data` payload of the event, converted from the JSON string to GoLisp primitives. Objects become [frames](http://techblog.steelseries.com/golisp/language-ref.html#frames), arrays become lists, strings and numbers remain the same.

```scheme
(let* ((vals (values: data))
```

`vals` is the `values` part of the JSON we sent way back in the JavaScript code. the `(values: data)` portion is GoLisp shorthand for `get-slot`, where `data` is the frame and `values:` is the slot we are getting.

```scheme
(colors (map percent vals visualizer-columns))
```

`colors` contains all the key colors we are going to draw. We use `map` here again but with two differences. First, instead of passing in a GoLisp primitive as its first argument, we supply our own function `percent` from eariler. Second, we have two arguments after the function instead of one. This means each list is treated as one of the parameters for the map function. So each value passed in is paired with it's respective custom zone and both are passed to `percent`. The end result is a list of colors, separated by zones.

```scheme
(filtered-zones (filter notnil? (reduce append '() visualizer-columns)))
(filtered-colors (filter notnil? (reduce append '() colors))))
```

Because `colors` is a list of lists and `visualizer-columns` is also a list of lists, we want to flatten them for the last step of our handler. We can do this using `reduce`. `reduce` is a GoLisp primitive that combines all the items in a list using a given function. Our function is `append`, so we concatenate all the HID-code lists in `visualizer-columns` and `colors` respectively into one big list each. We also filter out all the nil values at this step using `filter`. `filter` returns a copy of the list only with elements that pass a given function, in our case `notnil?`.

```scheme
(on-device "rgb-per-key-zones" show-on-keys: filtered-zones filtered-colors))))
```

This is the part that actually draws to the keyboard! Feels like forever since we started, but we're finally here. `on-device` takes at least three parameters: a string which specifies the device class we want to write to, a message type to write, and one or more arguments for that message type. We are writing to `"rgb-per-key-zone"` devices, which means each individual key on the device can be illuminated individually. The message type is `show-on-keys:` which is a special message just for per-key illumination devices. It has two arguments, a list of HID codes and a list of colors. We have those calculated already with `filtered-zones` and `filtered-colors`.

```scheme
(add-event-per-key-zone-use "AUDIO" "all")
```

This last part tells GameSense™ which zones or keys each specific event uses. We only have one event, and it uses the entire keyboard, so we specify that here.

## End Thoughts

This should be all you need to know to start working on your own handlers for GameSense™. The code for this example is located [here](/examples/audiovisualizer) and free to hack and improve on. One improvement you can do is instead of a bunch of weird not-quite-lines for visuals is to make them more straight. This however is left as an exercise to the reader. Get going! :)
