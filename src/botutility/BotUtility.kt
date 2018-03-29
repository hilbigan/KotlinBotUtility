package botutility

import autoitx4java.AutoItX
import com.jacob.com.LibraryLoader
import helper.Hotkey
import helper.ScreenSearcher
import java.awt.*
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import java.lang.Math.abs
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel

/**
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
     * Every coordinate point will be offset relative to this window's position from this point on
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

    fun waitFor() = robot.waitForIdle()

    fun getMousePosition(): Point = MouseInfo.getPointerInfo().location.dcoord()

    fun leftClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_MASK)
    }

    fun leftClickHold() {
        robot.mousePress(InputEvent.BUTTON1_MASK)
    }

    fun leftClickRelease() {
        robot.mouseRelease(InputEvent.BUTTON1_MASK)
    }

    fun rightClick() {
        robot.mousePress(InputEvent.BUTTON3_MASK)
        robot.mouseRelease(InputEvent.BUTTON3_MASK)
    }

    fun click(p: Point){
        moveMouse(p.x, p.y)
        leftClick()
    }

    fun click(x: Int, y: Int){
        moveMouse(x, y)
        leftClick()
    }

    fun moveMouse(p: Point, delay: Int = 0) {
        moveMouse(p.x, p.y, delay)
    }

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
        }
        robot.autoDelay = 10
    }

    fun moveMouseRelative(p: Point, delay: Int = 0) {
        moveMouseRelative(p.x, p.y, delay)
    }

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

    fun controlClick(x: Int, y: Int, titleRegex: String = "") {
        val (xx, yy) = Point(x,y).coord()
        ait.controlClick("[REGEXPTITLE:$titleRegex]", "", "", "Left", 1, xx, yy)
    }

    fun controlClick(p: Point, titleRegex: String = "") {
        controlClick(p.x, p.y, titleRegex)
    }

    fun type(text: String) {
        ait.send(text, true)
    }

    fun pressKey(e: KeyEvent) {
        robot.keyPress(e.keyCode)
    }

    fun releaseKey(e: KeyEvent) {
        robot.keyRelease(e.keyCode)
    }

    fun isProgramRunning(titleRegex: String): Boolean {
        val gameHandle = ait.winGetHandle("[REGEXPTITLE:$titleRegex]")
        return Integer.parseInt(gameHandle.substring(2), 16) != 0
    }

    fun focusWindow(titleRegex: String) {
        ait.winActivate("[REGEXPTITLE:$titleRegex]")
    }

    fun getPixelChecksum(x: Int, y: Int, w: Int, h: Int): Double {
        val (xx, yy) = Point(x,y).coord()
        return ait.pixelChecksum(xx, yy, xx + w, yy + h)
    }

    fun getPixelChecksum(p: Point, w: Int, h: Int): Double {
        val (xx, yy) = Point(p.x,p.y).coord()
        return ait.pixelChecksum(xx, yy, xx + w, yy + h)
    }

    fun getPixelColor(x: Int, y: Int): Color {
        val (xx, yy) = Point(x,y).coord()
        return robot.getPixelColor(xx, yy)
    }

    fun getPixelColor(p: Point): Color {
        val (xx, yy) = Point(p.x,p.y).coord()
        return robot.getPixelColor(xx, yy)
    }

    fun findPixelColor(color: Int, shadeVariation: Int = 5, x: Int = 0, y: Int = 0, w: Int = 1920, h: Int = 1080): Point {
        val (xx, yy) = Point(x,y).coord()

        val coord = ait.pixelSearch(xx, yy, xx + w, yy + h, color, shadeVariation, 1)
        //println(Arrays.toString(coord))
        return Point(coord[0].toInt(), coord[1].toInt()).dcoord()
    }

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

    fun untilPixelHasColor(p: Point, color: Color, updateDelayMs: Int = 5, threadStop: Boolean = false, block: Bot.() -> Unit) {
        untilPixelHasColor(p.x, p.y, color, updateDelayMs, threadStop, block)
    }

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

    fun onKeyPressed(nativeKeyCode: Int, block: () -> Unit) {
        Hotkey.instance.activate()
        Hotkey.instance.addHotkey(Hotkey.TRIGGER_TYPE.TYPED, { e ->
            block.invoke()
        }, intArrayOf(nativeKeyCode))
    }

    /**
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

    fun getPositionOfWindow(titleRegex: String): Point {
        return Point(ait.winGetPosX(titleRegex.regexTitle()), ait.winGetPosY(titleRegex.regexTitle()))
    }

    fun getDimensionOfWindow(titleRegex: String): Point {
        return Point(ait.winGetPosWidth(titleRegex.regexTitle()), ait.winGetPosHeight(titleRegex.regexTitle()))
    }

    fun getActiveWindow(): String {
        return ait.winGetTitle("[ACTIVE]")
    }

    fun assertRunning(titleRegex: String) {
        if (!isProgramRunning(titleRegex)) {
            println("Process: $titleRegex not found, please start it first!")
            System.exit(1)
        }
    }

    fun searchImage(pathToImage: File, x: Int = -1, y: Int = -1, w: Int = -1, h: Int = -1): Point {
        if (x != -1 && y != -1 && w != -1 && h != -1) {
            val (xx, yy) = Point(x, y).coord()
            return ScreenSearcher().search(pathToImage, ScreenSearcher.rectangleToRegion(Rectangle(xx, yy, w, h)))
        } else
            return ScreenSearcher().search(pathToImage)
    }

    /*fun Point.toPointInWindow(titleRegex: String, xOffset: Int = 0, yOffset: Int = 0): Point {
        val posOfWin = getPositionOfWindow(titleRegex)
        return Point(this.x + posOfWin.x + xOffset, this.y + posOfWin.y + yOffset)
    }*/
}

operator fun Point.component1() = this.x
operator fun Point.component2() = this.y
fun Point.str(): String {
    return "($x,$y)"
}

fun String.regexTitle(): String = "[REGEXPTITLE:$this]"

@Suppress("NAME_SHADOWING")
private  fun line(x0: Int, y0: Int, x1: Int, y1: Int): List<Point> {
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

fun highlightArea(x: Int, y: Int, w: Int, h: Int) {
    drawAlphaWindow(x = x, y = y, w = w, h = h)
}

private var drawnAlphaWindows: MutableList<JFrame> = mutableListOf()
private val array = Array(1920 * 1080) { i -> 0xFF0000 }
fun drawAlphaWindow(bitArray: Array<Int> = array, x: Int = 0, y: Int = 0, w: Int = 1920, h: Int = 1080, colalpha: Int = 128) {

    assert(bitArray.size == w * h)

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
            //g2d.fillRect(0, 0, width, height)
            //g2d.drawString("HALLO",0,0)
            for (xx in 0 until w)
                for (yy in 0 until h)
                    if (bitArray[xx + yy * h] != 0) {
                        val col = bitArray[xx + yy * h]
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