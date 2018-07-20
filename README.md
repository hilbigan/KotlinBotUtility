# KotlinBotUtility
Wrapper for java.awt.Robot and [AutoItXJava](https://github.com/accessrichard/autoitx4java) to provide easily accessible functionality to create bots for windows.
See examples for an example application.
You need to either install AutoIt or download it's portable version and register it using
```
regsvr32.exe AutoItX3.dll
```
(or run the script [here](https://github.com/hilbigan/KotlinBotUtility/tree/master/src/install) as an administrator)


**Minimal Example:**
```kotlin
fun main(args: Array<String>){
  bot {
    //Move the mouse to 0,0
    mouseMove(0,0)
  }
}
```

**Interacting with programs:**
```kotlin
bot {
  //Checks if a program with the specified title (regex) is running and otherwise terminates
  assertRunning(".*?Chrome")

  //Focus window with the specified title
  focusWindow(".*?Chrome")
  
  //All coordinates will be relative to the given window's position from here on.
  //Can be disabled with 'coordinateModeAbsolute()'
  coordinateModeWindow(".*?Chrome")
  
  //Move the mouse slooowly towards 0,0. Greater delay => slower movement.
  moveMouse(0,0,delay=1)
}
```

**Other cool stuff:**
```kotlin
bot {
  onKeyPressed(NativeKeyEvent.VC_ENTER){ //Executes when 'Enter' is pressed
    //Left-Clicks on 150,150 every second until the pixel at 100,100 is red.
    untilPixelHasColor(100,100,Color(0xFF0000), threadStop = true){
      while(true){ click(150,150); sleep(1000) }
    }
    
    //...and then types some text
    type("some text")
  }
}
```
