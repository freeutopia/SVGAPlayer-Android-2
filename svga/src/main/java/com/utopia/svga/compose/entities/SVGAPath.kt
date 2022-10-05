package com.utopia.svga.compose.entities

import android.graphics.Path
import android.text.TextUtils
import com.utopia.svga.utils.FPoint
import java.util.*

class SVGAPathEntity(originValue: String) {

  private val replacedValue: String = originValue.replace(",", " ")

  private var cachedPath: Path? = null

  fun buildPath(toPath: Path) {
    (cachedPath ?: initPath()).let {
      cachedPath = it
      toPath.set(it)
    }
  }

  /**
   * M =moveto,L = lineto,H = horizontal lineto,V = vertical lineto,C = curveto,S = smooth curveto,Q = quadratic Belzier curve
   * T = smooth quadratic Belzier curveto,A = elliptical Arc,Z = closepath
   */
  private fun initPath(): Path {
    val path = Path()
    val segments = StringTokenizer(this.replacedValue, "MLHVCSQRAZmlhvcsqraz", true)
    var currentMethod = ""
    while (segments.hasMoreTokens()) {
      val segment = segments.nextToken()
      if (TextUtils.isEmpty(segment)) {
        continue
      }
      if (validMethods(segment)) {
        currentMethod = segment
        if (segment == "Z" || segment == "z") {
          operate(path, currentMethod, StringTokenizer("", ""))
        }
      } else {
        operate(path, currentMethod, StringTokenizer(segment, " "))
      }
    }
    return path
  }

  private fun validMethods(seg: String): Boolean {
    return when (seg) {
      "M", "L", "H", "V", "C", "S", "Q", "R", "A", "Z" -> true
      "m", "l", "h", "v", "c", "s", "q", "r", "a", "z" -> true
      else -> false
    }
  }

  private fun operate(finalPath: Path, method: String, args: StringTokenizer) {
    var x0 = 0.0f
    var y0 = 0.0f
    var x1 = 0.0f
    var y1 = 0.0f
    var x2 = 0.0f
    var y2 = 0.0f
    try {
      var index = 0
      while (args.hasMoreTokens()) {
        val s = args.nextToken()
        if (s.isEmpty()) {
          continue
        }
        if (index == 0) {
          x0 = s.toFloat()
        }
        if (index == 1) {
          y0 = s.toFloat()
        }
        if (index == 2) {
          x1 = s.toFloat()
        }
        if (index == 3) {
          y1 = s.toFloat()
        }
        if (index == 4) {
          x2 = s.toFloat()
        }
        if (index == 5) {
          y2 = s.toFloat()
        }
        index++
      }
    } catch (ignore: Exception) {
    }

    val currentPoint = when (method) {
      "M" -> {
        finalPath.moveTo(x0, y0)
        FPoint(x0, y0)
      }
      "m" -> {
        finalPath.rMoveTo(x0, y0)
        FPoint(x0, y0)
      }
      else -> FPoint(0f, 0f)
    }

    if (method == "L") {
      finalPath.lineTo(x0, y0)
    } else if (method == "l") {
      finalPath.rLineTo(x0, y0)
    }
    if (method == "C") {
      finalPath.cubicTo(x0, y0, x1, y1, x2, y2)
    } else if (method == "c") {
      finalPath.rCubicTo(x0, y0, x1, y1, x2, y2)
    }
    if (method == "Q") {
      finalPath.quadTo(x0, y0, x1, y1)
    } else if (method == "q") {
      finalPath.rQuadTo(x0, y0, x1, y1)
    }
    if (method == "H") {
      finalPath.lineTo(x0, currentPoint.y)
    } else if (method == "h") {
      finalPath.rLineTo(x0, 0f)
    }
    if (method == "V") {
      finalPath.lineTo(currentPoint.x, x0)
    } else if (method == "v") {
      finalPath.rLineTo(0f, x0)
    }
    if (method == "Z" || method == "z") {
      finalPath.close()
    }
  }

}
