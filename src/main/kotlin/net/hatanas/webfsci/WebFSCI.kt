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
import jumpaku.fsc.identify.reference.reparametrize
import jumpaku.fsc.identify.reparametrize
import org.jfree.graphics2d.svg.SVGGraphics2D
import spark.Spark.post
import spark.Spark.staticFileLocation
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Stroke

fun main(vararg args: String) {
    staticFileLocation("/")

    post("/fsci") { request, response ->
        val jsonStr = request.body()
        val json = jsonStr.parseJson().get()
        val (width, height) = Tuple(json["canvas"]["x"].int, json["canvas"]["y"].int)
        val points = Array.ofAll(json["points"].array.flatMap { ParamPoint.fromJson(it) })
        val fsc = reparametrize(FscGenerator().generate(points))
        val result = Primitive7Identifier().identify(fsc)
        println(result.grades)
        SVGGraphics2D(width, height)
                .drawFuzzyCurve(fsc, BasicStroke(1.0f), Color.CYAN)
                .drawPolyline(fsc, BasicStroke(2.0f), Color.CYAN)
                .drawPolyline(when {
                    result.curveClass.isLinear -> result.linear.reparametrized
                    result.curveClass.isCircular -> result.circular.reparametrized
                    result.curveClass.isElliptic -> result.elliptic.reparametrized
                    else -> fsc
                }, BasicStroke(3.0f), Color.RED)
                .svgDocument
    }
}

fun <C: Curve> SVGGraphics2D.drawFuzzyCurve(curve: ReparametrizedCurve<C>, stroke: Stroke = BasicStroke(1.0f), color: Color = Color.BLACK): SVGGraphics2D {
    val ep = curve.evaluateAll(5.0 / curve.chordLength)
    this.stroke = stroke
    this.color = color
    ep.forEach {
        val r = it.r.toInt()
        this.drawArc(it.x.toInt() - r, it.y.toInt() - r, r * 2, r * 2, 0, 360)
    }
    return this
}

fun <C: Curve> SVGGraphics2D.drawPolyline(polyline: ReparametrizedCurve<C>, stroke: Stroke = BasicStroke(1.0f), color: Color = Color.BLACK): SVGGraphics2D {
    val ep = polyline.evaluateAll(5.0 / polyline.chordLength)
    this.stroke = stroke
    this.color = color
    ep.zip(ep.drop(1)).forEach { (b, e) -> this.drawLine(b.x.toInt(), b.y.toInt(), e.x.toInt(), e.y.toInt()) }
    return this
}