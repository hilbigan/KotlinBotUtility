package helper

import org.sikuli.api.DesktopScreenRegion
import org.sikuli.api.ImageTarget
import org.sikuli.api.ScreenRegion
import java.awt.Point
import java.awt.Rectangle
import java.io.File
import java.util.*

class ScreenSearcher(resolutionWidth: Int = 1920, resolutionHeight: Int = 1080) {

    companion object {
        fun rectangleToRegion(rect: Rectangle): ScreenRegion {
            return DesktopScreenRegion(rect.x, rect.y, rect.width, rect.height)
        }
    }

    private val sr: ScreenRegion = DesktopScreenRegion(0, 0, resolutionWidth, resolutionHeight)

    fun search(image: File, region: ScreenRegion = sr): Point {
        val t = ImageTarget(image)
        val result = region.find(t) ?: return Point(-1, -1)
        return Point(result.center.x, result.center.y)
    }

    fun searchAll(image: File, region: ScreenRegion = sr): List<Point> {
        val t = ImageTarget(image)
        val result = region.findAll(t)
        val list = ArrayList<Point>()

        for (s in result) {
            list.add(Point(s.center.x, s.center.y))
        }

        return list
    }

}