package net.hatanas.webfsci

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import io.vavr.API.Tuple
import io.vavr.collection.Array
import jumpaku.core.curve.Curve
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.identify.Primitive7Identifier
import jumpaku.fsc.identify.reparametrize
import org.jfree.graphics2d.svg.SVGGraphics2D
import spark.Spark.post
import spark.Spark.staticFileLocation
import java.awt.BasicStroke
import java.awt.Color

fun main(vararg args: String) {
    staticFileLocation("/")

    post("/fsci") { request, response ->
        val jsonStr = request.body()
        val json = jsonStr.parseJson().get()
        val (width, height) = Tuple(json["canvas"]["x"].int, json["canvas"]["y"].int)
        val points = Array.ofAll(json["points"].array.flatMap { ParamPoint.fromJson(it) })
        val fsc = reparametrize(FscGenerator().generate(points))
        val result = Primitive7Identifier().identify(fsc)
        val g2 = fscToSVG(SVGGraphics2D(width, height), fsc)
        g2.color = Color.RED
        g2.stroke = BasicStroke(3.0f)
        when {
            result.curveClass.isFreeCurve -> {
                val ep = fsc.evaluateAll(5.0 / fsc.chordLength)
                ep.zip(ep.drop(1)).forEach {(b, e) -> g2.drawLine(b.x.toInt(), b.y.toInt(), e.x.toInt(), e.y.toInt())}
            }
            result.curveClass.isLinear -> {
                val l = result.linear.base
                g2.drawLine(l.begin.x.toInt(), l.begin.y.toInt(), l.end.x.toInt(), l.end.y.toInt())
            }
            result.curveClass.isCircular -> {
                val c = result.circular.reparametrized
                val ep = c.evaluateAll(5.0 / c.chordLength)
                ep.zip(ep.drop(1)).forEach {(b, e) -> g2.drawLine(b.x.toInt(), b.y.toInt(), e.x.toInt(), e.y.toInt())}
            }
            result.curveClass.isElliptic -> {
                val c = result.elliptic.reparametrized
                val ep = c.evaluateAll(5.0 / c.chordLength)
                ep.zip(ep.drop(1)).forEach {(b, e) -> g2.drawLine(b.x.toInt(), b.y.toInt(), e.x.toInt(), e.y.toInt())}
            }
        }
        g2.svgDocument
    }
}

fun <C: Curve> fscToSVG(g2: SVGGraphics2D, fsc: ReparametrizedCurve<C>): SVGGraphics2D {
    val ep = fsc.evaluateAll(5.0 / fsc.chordLength)
    g2.stroke = BasicStroke(2.0f)
    g2.color = Color.CYAN
    ep.zip(ep.drop(1)).forEach {(b, e) -> g2.drawLine(b.x.toInt(), b.y.toInt(), e.x.toInt(), e.y.toInt())}
    g2.stroke = BasicStroke(1.0f)
    ep.forEach {
        val r = it.r.toInt()
        g2.drawArc(it.x.toInt() - r, it.y.toInt() - r, r * 2, r * 2, 0, 360) }
    return g2
}