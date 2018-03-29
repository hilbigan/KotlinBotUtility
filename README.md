# KotlinBotUtility
Wrapper for java.awt.Robot and AutoItXJava to provide easily accessible functionality to create bots for windows.
See examples for an example application.

**Minimal Example:**
```kotlin
fun main(args: Array<String>){
  with(Bot){
    //Move the mouse to 0,0
    mouseMove(0,0)
  }
}
```

**Interacting with programs:**
```kotlin
with(Bot){
  //Checks if a program with the specified title (regex) is running and otherwise terminates
  assertRunning(".?*Chrome")

  //Focus window with the specified title
  focusWindow(".?*Chrome")
  
  //All coordinates will be relative to the given window's position from here on.
  //Can be disabled with 'coordinateModeAbsolute()'
  coordinateModeWindow(".?*Chrome")
  
  //Move the mouse slooowly towards 0,0. Greater delay => slower movement.
  moveMouse(0,0,delay=1)
}
```

