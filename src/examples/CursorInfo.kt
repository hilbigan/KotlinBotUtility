package examples

import botutility.*
import org.jnativehook.keyboard.NativeKeyEvent
import java.awt.Point

fun main(args: Array<String>) {
    /**
     * This example is a small tool that can be used to get mouse coordinates
     * or coordinates relative to a window.
     * Controls:
     * X - Switch between relative and absolute coordinate modes.
     * C - Display mouse position and pixel color and specify first point
     * V - Specify second point and calculate pixel checksum between first (C) and second point
     * B - Highlight the previously selected area between the first (C) and second (V) point (always absolute)
     * N - Close highlight window
     */

    var pos1 = Point(0,0)
    var pos2 = Point(0,0)
    var relative = false

    bot {
        onKeyPressed(NativeKeyEvent.VC_X){
            if(!relative) {
                coordinateModeWindow(getActiveWindow())
                relative = true
                println("Now displaying coordinates relative to \"${getActiveWindow()}\".")
            } else {
                coordinateModeAbsolute()
                relative = false
                println("Now displaying coordinates in absolute mode.")
            }
        }
        onKeyPressed(NativeKeyEvent.VC_C){
            pos1 = getMousePosition()
            val col = getPixelColor(pos1)
            println("Mouse Position: " + pos1.str() + " - Color: ${col.red},${col.green},${col.blue}")
        }
        onKeyPressed(NativeKeyEvent.VC_V){
            pos2 = getMousePosition()
            println("Pixel Checksum: " + getPixelChecksum(pos1, pos2.x - pos1.x, pos2.y - pos1.y) + " (Area: ${pos1.str()} -> ${pos2.str()})")
        }
        onKeyPressed(NativeKeyEvent.VC_B){
            closeAllAlphaWindows()
            highlightArea(pos1.x, pos1.y, pos2.x - pos1.x, pos2.y - pos1.y)
            println("Highlighting area: ${pos1.str()} -> ${pos2.str()}; width=${pos2.x - pos1.x}, height=${pos2.y - pos1.y}")
        }
        onKeyPressed(NativeKeyEvent.VC_N){
            closeAllAlphaWindows()
        }
    }
}