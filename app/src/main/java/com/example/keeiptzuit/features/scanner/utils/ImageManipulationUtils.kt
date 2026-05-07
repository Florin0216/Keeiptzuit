package com.example.keeiptzuit.features.scanner.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap
import org.opencv.core.CvType
import org.opencv.core.Size
import kotlin.math.hypot

object ImageManipulationUtils {
    fun detectEdges(mat: Mat): List<Point> {

        // Convert to grayscale
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)

        // Blur to reduce noise
        val blurred = Mat()
        Imgproc.bilateralFilter(gray, blurred, 9, 75.0, 75.0)

        // Detect edges using Canny
        val edges = Mat()
        Imgproc.Canny(blurred, edges, 75.0, 200.0)

        // Find contours
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            edges,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // Sort contours by area, descending
        contours.sortByDescending { Imgproc.contourArea(it) }

        var rawPoints: List<Point> = emptyList()

        for (contour in contours) {
            val peri = Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.08 * peri, true)

            // Look for a 4-point contour (document shape)
            if (approx.total() == 4L) {
                rawPoints = approx.toList()
                break
            }
        }

        gray.release()
        edges.release()
        mat.release()

        return rawPoints
    }

    fun warpPerspective(inputBitmap: Bitmap, corners: List<Point>): Bitmap? {
        if (corners.size != 4) return null

        // Sort corners: TL, TR, BR, BL
        val sorted = sortCornersTLTRBRBL(corners)

        // Calculate dimensions of the destination image
        val widthA = distance(sorted[0], sorted[1])
        val widthB = distance(sorted[2], sorted[3])
        val maxWidth = maxOf(widthA, widthB).toInt()

        val heightA = distance(sorted[1], sorted[2])
        val heightB = distance(sorted[0], sorted[3])
        val maxHeight = maxOf(heightA, heightB).toInt()

        val srcMat = MatOfPoint2f(
            sorted[0], sorted[1], sorted[2], sorted[3]
        )

        val dstMat = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(maxWidth.toDouble() - 1, 0.0),
            Point(maxWidth.toDouble() - 1, maxHeight.toDouble() - 1),
            Point(0.0, maxHeight.toDouble() - 1)
        )

        val transform = Imgproc.getPerspectiveTransform(srcMat, dstMat)

        val src = Mat()
        Utils.bitmapToMat(inputBitmap, src)
        // Ensure 3-channel BGR for warp
        val srcBgr = Mat()
        if (src.channels() == 4) {
            Imgproc.cvtColor(src, srcBgr, Imgproc.COLOR_RGBA2BGR)
        } else {
            src.copyTo(srcBgr)
        }

        val dst = Mat(Size(maxWidth.toDouble(), maxHeight.toDouble()), CvType.CV_8UC4)
        Imgproc.warpPerspective(srcBgr, dst, transform, Size(maxWidth.toDouble(), maxHeight.toDouble()))

        val resultBitmap = createBitmap(maxWidth, maxHeight)
        Utils.matToBitmap(dst, resultBitmap)

        src.release()
        srcBgr.release()
        dst.release()
        transform.release()
        srcMat.release()
        dstMat.release()

        return resultBitmap
    }

    private fun distance(p1: Point, p2: Point): Double {
        return hypot(p1.x - p2.x, p1.y - p2.y)
    }

    /**
     * Sort TL, TR, BR, BL using sum/diff heuristic (most robust for rectangles).
     */
    fun sortCornersTLTRBRBL(points: List<Point>): List<Point> {
        require(points.size == 4)
        val sumSorted = points.sortedBy { it.x + it.y }
        val diffSorted = points.sortedBy { it.y - it.x }

        val topLeft = sumSorted.first()
        val bottomRight = sumSorted.last()
        val topRight = diffSorted.first()
        val bottomLeft = diffSorted.last()

        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }
}