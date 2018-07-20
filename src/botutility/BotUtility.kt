package botutility

import autoitx4java.AutoItX
import com.jacob.com.LibraryLoader
import helper.Hotkey
import helper.ScreenSearcher
import java.awt.*
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import java.lang.Math.abs
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.NoSuchElementException

/**
 * Provides various functionality to simulate or recieve user input.
 * @author Aaron Hilbig
 */
object Bot {

    private val robot = Robot()
    private val ait: AutoItX by lazy {
        val files = listOf(File("src/jacob-1.18-x64.dll"), File("lib/jacob-1.18-x64.dll"), File("jacob-1.18-x64.dll"))
        if (!files.any { it.exists() }) {
            throw IllegalAccessException("Cannot access autoit, jacob-1.18-x64.dll not found!")
        } else {
            System.setProperty(LibraryLoader.JACOB_DLL_PATH, files.first { it.exists() }.absolutePath)
            AutoItX()
        }
    }
    private var coordinateModeWindow = false
    private var offsetWindowTitle: String? = null
    private var offset = Point(0, 0)

    init {
        robot.isAutoWaitForIdle = true
        robot.autoDelay = 10
    }

    /**
     * Sets the coordinate mode to "windowed".<br>
     * Every coordinate point will be offset relative to this window's position from this point on.
     */
    fun coordinateModeWindow(titleRegex: String, offsetFromWindow: Point = Point(0,0)) {
        offsetWindowTitle = titleRegex
        offset = offsetFromWindow
        coordinateModeWindow = true
    }

    /**
     * Sets the coordinate mode to "absolute". This is the default mode.
     */
    fun coordinateModeAbsolute() {
        coordinateModeWindow = false
    }

    private fun Point.coord(): Point {
        if(coordinateModeWindow) {
            val pt = getPositionOfWindow(offsetWindowTitle!!)
            return Point(pt.x + offset.x + this.x, pt.y + offset.y + this.y)
        } else {
            return this
        }
    }

    private fun Point.dcoord(): Point {
        if(coordinateModeWindow) {
            val pt = getPositionOfWindow(offsetWindowTitle!!)
            return Point(- pt.x - offset.x + this.x, - pt.y - offset.y + this.y)
        } else {
            return this
        }
    }

    /**
     * Wait for the java.awt.Robot to finish it's last action.
     */
    fun waitFor() = robot.waitForIdle()

    /**
     * Returns the current mouse position.
     */
    fun getMousePosition(): Point = MouseInfo.getPointerInfo().location.dcoord()

    /**
     * Simulates a left click with the mouse.
     */
    fun leftClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_MASK)
    }

    /**
     * Presses and holds the left mouse button.
     */
    fun leftClickHold() {
        robot.mousePress(InputEvent.BUTTON1_MASK)
    }

    /**
     * Releases the left mouse button.
     */
    fun leftClickRelease() {
        robot.mouseRelease(InputEvent.BUTTON1_MASK)
    }

    /**
     * Simulates a right click with the mouse.
     */
    fun rightClick() {
        robot.mousePress(InputEvent.BUTTON3_MASK)
        robot.mouseRelease(InputEvent.BUTTON3_MASK)
    }

    /**
     * Moves the mouse to the specified point and left-clicks.
     */
    fun click(p: Point){
        moveMouse(p.x, p.y)
        leftClick()
    }

    /**
     * Moves the mouse to the specified point and left-clicks.
     */
    fun click(x: Int, y: Int){
        moveMouse(x, y)
        leftClick()
    }

    /**
     * Moves the mouse to the specified point. If the delay is greater than zero, the mouse will move smoothly along a
     * direct path to the given point.
     */
    fun moveMouse(p: Point, delay: Int = 0) {
        moveMouse(p.x, p.y, delay)
    }

    /**
     * Moves the mouse to the specified point. If the delay is greater than zero, the mouse will move smoothly along a
     * direct path to the given point.
     */
    fun moveMouse(x: Int, y: Int, delay: Int = 0) {
        val (xx, yy) = Point(x,y).coord()

        if (delay == 0) {
            robot.mouseMove(xx, yy)
            return
        }

        robot.autoDelay = 0
        for (p in line(getMousePosition().x, getMousePosition().y, xx, yy)) {
            robot.mouseMove(p.x, p.y)
            robot.delay(delay)
            if(Thread.currentThread().isInterrupted){
                moveMouse(xx, yy)
                robot.autoDelay = 10
                return
            }
        }
        robot.autoDelay = 10
    }

    /**
     * Moves the mouse to the specified point relative to the current mouse position. If the delay is greater than zero, the mouse will move smoothly along a
     * direct path to the given point.
     */
    fun moveMouseRelative(p: Point, delay: Int = 0) {
        moveMouseRelative(p.x, p.y, delay)
    }

    /**
     * Moves the mouse to the specified point relative to the current mouse position. If the delay is greater than zero, the mouse will move smoothly along a
     * direct path to the given point.
     */
    fun moveMouseRelative(x: Int, y: Int, delay: Int = 0) {
        val mousePos = getMousePosition()

        if (delay == 0) {
            robot.mouseMove(mousePos.x + x, mousePos.y + y)
            return
        }

        robot.autoDelay = 0
        for (p in line(mousePos.x, mousePos.y, mousePos.x + x, mousePos.y + y)) {
            robot.mouseMove(p.x, p.y)
            robot.delay(delay)
        }
        robot.autoDelay = 10
    }

    /**
     * Uses AutoIT's "controlClick" function to simulate a click on something inside a window
     * without actually moving or controlling the mouse.
     */
    fun controlClick(x: Int, y: Int, titleRegex: String = "") {
        val (xx, yy) = Point(x,y).coord()
        ait.controlClick(titleRegex.toRegexTitle(), "", "", "Left", 1, xx, yy)
    }

    fun controlType(text: String, titleRegex: String = "") {
        ait.controlSend(titleRegex.toRegexTitle(), "", "", text)
    }

    /**
     * Uses AutoIT's "controlClick" function to simulate a click on something inside a window
     * without actually moving or controlling the mouse.
     */
    fun controlClick(p: Point, titleRegex: String = "") {
        controlClick(p.x, p.y, titleRegex)
    }

    /**
     * Types the given text.
     */
    fun type(text: String) {
        ait.send(text, true)
    }

    /**
     * Presses all given key(s) at the same time and then releases them all at the same time.<br>
     * Useful for single keystrokes, e.g.: <br><code>typeKey(KeyEvent.VK_ENTER)</code> but especially for<br>
     * combinations, e.g.: <br><code>typeKey(KeyEvent.VK_WINDOWS, KeyEvent.VK_R)</code>
     * @see java.awt.event.KeyEvent
     */
    fun typeKey(vararg keyCodes: Int){
        for(k in keyCodes)
            pressKey(k)
        for(k in keyCodes)
            releaseKey(k)
    }

    /**
     * Presses and holds the given key.
     * @see java.awt.event.KeyEvent
     */
    fun pressKey(keyCode: Int) {
        robot.keyPress(keyCode)
    }

    /**
     * Releases the given key.
     * @see java.awt.event.KeyEvent
     */
    fun releaseKey(keyCode: Int) {
        robot.keyRelease(keyCode)
    }

    /**
     * Checks and returns wether a program with the given title (regex) is running.
     */
    fun isProgramRunning(titleRegex: String): Boolean {
        val hndl = ait.winGetHandle("[REGEXPTITLE:$titleRegex]")
        return Integer.parseInt(hndl.substring(2), 16) != 0
    }

    /**
     * Returns wether a process with the given name exists.
     */
    fun isProcessExists(procName: String): Boolean {
        return ait.processExists(procName) != 0
    }

    /**
     * Focuses the window with the given title (regex).
     */
    fun focusWindow(titleRegex: String) {
        ait.winActivate("[REGEXPTITLE:$titleRegex]")
    }

    /**
     * Returns the calculated pixel checksum of the given area.
     */
    fun getPixelChecksum(x: Int, y: Int, w: Int, h: Int): Double {
        val (xx, yy) = Point(x,y).coord()
        return ait.pixelChecksum(xx, yy, xx + w, yy + h)
    }

    /**
     * Returns the calculated pixel checksum of the given area.
     */
    fun getPixelChecksum(p: Point, w: Int, h: Int): Double {
        val (xx, yy) = Point(p.x,p.y).coord()
        return ait.pixelChecksum(xx, yy, xx + w, yy + h)
    }

    /**
     * Returns the specified pixels color.
     */
    fun getPixelColor(x: Int, y: Int): Color {
        val (xx, yy) = Point(x,y).coord()
        return robot.getPixelColor(xx, yy)
    }

    /**
     * Returns the specified pixels color.
     */
    fun getPixelColor(p: Point): Color {
        val (xx, yy) = Point(p.x,p.y).coord()
        return robot.getPixelColor(xx, yy)
    }

    /**
     * Finds the first pixel with the given color and given shade variation (default: 5)
     * in the specified area (default: full screen)
     */
    fun findPixelColor(color: Int, shadeVariation: Int = 5, x: Int = 0, y: Int = 0, w: Int = 1920, h: Int = 1080): Point {
        val (xx, yy) = Point(x,y).coord()

        val coord = ait.pixelSearch(xx, yy, xx + w, yy + h, color, shadeVariation, 1)
        //println(Arrays.toString(coord))
        return Point(coord[0].toInt(), coord[1].toInt()).dcoord()
    }

    /**
     * Executes the given block of code in a new thread. If any of the pixels in the specified area change, the thread
     * will be joined or stopped (depending on wether threadStop is true).
     * updateDelayMs determines the delay between checks and thereby the accuracy and reaction time.
     */
    fun untilPixelsChanged(x: Int, y: Int, w: Int = 1, h: Int = 1, updateDelayMs: Int = 5, threadStop: Boolean = false, block: Bot.() -> Unit) {
        val startPixels = getPixelChecksum(x, y, w, h)
        val thread = Thread {
            block.invoke(this)
        }.apply { isDaemon = true; start() }
        while (getPixelChecksum(x, y, w, h) == startPixels) {
            Thread.sleep(updateDelayMs.toLong())
        }
        if (threadStop)
            thread.stop()
        else thread.join()
    }

    /**
     * Executes the given block of code in a new thread. If the given pixel changes to the given color, the thread
     * will be joined or stopped (depending on wether threadStop is true).
     * updateDelayMs determines the delay between checks and thereby the accuracy and reaction time.
     */
    fun untilPixelHasColor(x: Int, y: Int, color: Color, updateDelayMs: Int = 5, threadStop: Boolean = false, block: Bot.() -> Unit) {
        val thread = Thread {
            block.invoke(this)
        }.apply { isDaemon = true; start() }
        while (getPixelColor(x, y) != color) {
            Thread.sleep(updateDelayMs.toLong())
        }
        if (threadStop)
            thread.stop()
        else thread.join()
    }

    /**
     * Executes the given block of code in a new thread. If the given pixel changes to the given color, the thread
     * will be joined or stopped (depending on wether threadStop is true).
     * updateDelayMs determines the delay between checks and thereby the accuracy and reaction time.
     */
    fun untilPixelHasColor(p: Point, color: Color, updateDelayMs: Int = 5, threadStop: Boolean = false, block: Bot.() -> Unit) {
        untilPixelHasColor(p.x, p.y, color, updateDelayMs, threadStop, block)
    }

    /**
     * Executes the given block of code in a new thread. If the given key is pressed, the thread
     * will be joined or stopped (depending on wether threadStop is true).
     * @see org.jnativehook.keyboard.NativeKeyEvent
     */
    fun untilKeyPressed(nativeKeyCode: Int, updateDelayMs: Int = 5, block: Bot.() -> Unit) {
        Hotkey.instance.activate()
        var pressed = false
        val thread = Thread {
            block.invoke(this)
        }.apply { start() }
        Hotkey.instance.addHotkey({ e ->
            thread.stop()
            pressed = true
        }, nativeKeyCode)
        while (!pressed) {
            Thread.sleep(updateDelayMs.toLong())
        }
    }

    /**
     * Executes the given block of code in a new thread every time the given key is pressed.
     * @see org.jnativehook.keyboard.NativeKeyEvent
     */
    fun onKeyPressed(nativeKeyCode: Int, block: () -> Unit) {
        Hotkey.instance.activate()
        Hotkey.instance.addHotkey(Hotkey.TRIGGER_TYPE.TYPED, { e ->
            block.invoke()
        }, intArrayOf(nativeKeyCode))
    }

    /**
     * Executes the given block of code in a new thread. If the given image found in the search space, the thread
     * will be joined or stopped (depending on wether threadStop is true).
     * updateDelayMs determines the delay between checks and thereby the accuracy and reaction time.
     * Use longer updateDelayMs (e.g. 100) to avoid image detection in transition animations etc.
     */
    fun untilImageFound(pathToImage: File, x: Int = 0, y: Int = 0, w: Int = 1920, h: Int = 1080, updateDelayMs: Int = 5, threadStop: Boolean = false, block: Bot.() -> Unit): Point {
        val (xx, yy) = Point(x,y).coord()
        var startPixels = getPixelChecksum(xx, yy, w, h)
        var point = Point(-1, -1)
        val NOT_FOUND = Point(-1, -1)
        val thread = Thread {
            block.invoke(this)
        }.apply { isDaemon = true; start() }
        do {
            /*val st = System.currentTimeMillis()
            while (getPixelChecksum(x, y, w, h) == startPixels && st - System.currentTimeMillis() < 100) {
                Thread.sleep(updateDelayMs.toLong())
            }*/
            Thread.sleep(updateDelayMs.toLong())
            point = searchImage(pathToImage, x, y, w, h)
            startPixels = getPixelChecksum(x, y, w, h)
        } while (point == NOT_FOUND)
        if (threadStop)
            thread.stop()
        else thread.join()
        return point
    }

    /**
     * Returns the top left position of a window with the given title (regex)
     */
    fun getPositionOfWindow(titleRegex: String): Point {
        return Point(ait.winGetPosX(titleRegex.toRegexTitle()), ait.winGetPosY(titleRegex.toRegexTitle()))
    }

    /**
     * Returns the dimensions (w,h) of a window with the given title (regex)
     */
    fun getDimensionOfWindow(titleRegex: String): Point {
        return Point(ait.winGetPosWidth(titleRegex.toRegexTitle()), ait.winGetPosHeight(titleRegex.toRegexTitle()))
    }

    /**
     * Returns the title of the currently active (focused) window.
     */
    fun getActiveWindow(): String {
        return ait.winGetTitle("[ACTIVE]")
    }

    /**
     * Returns the title of the given process' window, if it has one.<br>
     * Useful for programs that change their title constantly, but an expensive operation.<br>
     * @throws NoSuchElementException if the process or no window was found
     */
    fun getTitleByProcess(procName: String): String {
        ait.winList(".*?".toRegexTitle()).forEach {
            return it.filter { !it.isNullOrEmpty() }.first {
                try {
                    ait.winGetProcess(it).toInt() == ait.processExists(procName)
                } catch(e: Exception){ false }
            }
        }
        throw NoSuchElementException("Unknown process / process not found: $procName")
    }

    /**
     * Checks if a program with the specified title (regex) is running and otherwise terminates
     */
    fun assertRunning(titleRegex: String) {
        if (!isProgramRunning(titleRegex)) {
            println("Window with title '$titleRegex' not found, please start it first!")
            System.exit(1)
        }
    }

    /**
     * Checks if a process with the specified name exists and otherwise terminates
     */
    fun assertProcessPresent(procName: String){
        if(ait.processExists(procName) == 0){
            println("Process '$procName' not found, please start it first!")
            System.exit(1)
        }
    }

    /**
     * Searches for the given image in the given search space (default: full screen) and returns it's coordinates
     * if found, otherwise (-1,-1)
     */
    fun searchImage(pathToImage: File, x: Int = -1, y: Int = -1, w: Int = -1, h: Int = -1): Point {
        if (x != -1 && y != -1 && w != -1 && h != -1) {
            val (xx, yy) = Point(x, y).coord()
            return ScreenSearcher().search(pathToImage, ScreenSearcher.rectangleToRegion(Rectangle(xx, yy, w, h)))
        } else
            return ScreenSearcher().search(pathToImage)
    }

    /**
     * Opens the given filename/executable and optionally waits.
     */
    fun run(filename: String, wait: Boolean = false, workDir: String? = null){
        if(!wait){
            if(workDir == null) ait.run(filename) else ait.run(filename, workDir)
        } else {
            if(workDir == null) ait.runWait(filename) else ait.runWait(filename, workDir)
        }
    }
}

fun bot(block: Bot.() -> Unit){
    block.invoke(Bot)
}

/*
 * Extension functions for java.awt.Point
 */

operator fun Point.component1() = this.x
operator fun Point.component2() = this.y
fun Point.str(): String {
    return "($x,$y)"
}

private fun String.toRegexTitle(): String = "[REGEXPTITLE:$this]"

/**
 * https://de.wikipedia.org/wiki/Bresenham-Algorithmus
 */
@Suppress("NAME_SHADOWING")
private fun line(x0: Int, y0: Int, x1: Int, y1: Int): List<Point> {
    var x0 = x0
    var y0 = y0
    val ret = ArrayList<Point>()
    val dx = abs(x1 - x0)
    val sx = if (x0 < x1) 1 else -1
    val dy = -abs(y1 - y0)
    val sy = if (y0 < y1) 1 else -1
    var err = dx + dy
    var e2: Int

    while (true) {
        ret.add(Point(x0, y0))
        if (x0 == x1 && y0 == y1) break
        e2 = 2 * err
        if (e2 > dy) {
            err += dy
            x0 += sx
        }
        if (e2 < dx) {
            err += dx
            y0 += sy
        }
    }
    return ret
}

/**
 * Highlights the specified area in red.
 */
fun highlightArea(x: Int, y: Int, w: Int, h: Int) {
    drawAlphaWindow(x = x, y = y, w = w, h = h)
}

private var drawnAlphaWindows: MutableList<JFrame> = mutableListOf()
private val array = Array(1920 * 1080) { i -> 0xFF0000 }

/**
 * Display a transparent window with the given bounds. The colArray represents a lookup table for every pixel color in
 * the highlighted area.
 */
fun drawAlphaWindow(colArray: Array<Int> = array, x: Int = 0, y: Int = 0, w: Int = 1920, h: Int = 1080, colalpha: Int = 128) {

    assert(colArray.size == w * h)

    try {
        val c = Class.forName("com.sun.awt.AWTUtilities");
    } catch (e: Exception) {
        error("AlphaWindow not supported!")
    }

    @Throws(Exception::class)
    fun setOpaque(window: Window, opaque: Boolean) {
        try {
            val awtUtilsClass = Class.forName("com.sun.awt.AWTUtilities")
            if (awtUtilsClass != null) {
                val method = awtUtilsClass.getMethod("setWindowOpaque", Window::class.java, Boolean::class.javaPrimitiveType)
                method.invoke(null, window, opaque)
            }
        } catch (exp: Exception) {
            error("AlphaWindow not supported!")
        }
    }

    val frame = JFrame("Highlighted Area")
    frame.isUndecorated = true
    setOpaque(frame, false)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    frame.setBounds(x, y, w, h)
    val panel = (object : JPanel() {
        init {
            isOpaque = false
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(w, h)
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.color = Color(255, 0, 0, 128)
            for (xx in 0 until w)
                for (yy in 0 until h)
                    if (colArray[xx + yy * h] != 0) {
                        val col = colArray[xx + yy * h]
                        g2d.color = Color(col and 0xFF0000 shr 16, col and 0x00FF00 shr 8, col and 0x0000FF, colalpha)
                        g2d.fillRect(xx, yy, 1, 1)
                    }
            g2d.dispose()
        }
    })

    fun wndclose() {
        frame.dispose()
    }
    panel.addMouseListener(object : MouseListener {
        override fun mouseReleased(e: MouseEvent?) {
            wndclose()
        }

        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseClicked(e: MouseEvent?) {
            wndclose()
        }

        override fun mouseExited(e: MouseEvent?) {}
        override fun mousePressed(e: MouseEvent?) {}
    })
    frame.add(panel)
    frame.pack()
    frame.isVisible = true
    drawnAlphaWindows.add(frame)
}

fun closeAllAlphaWindows() {
    drawnAlphaWindows.forEach { it.dispose() }
}