package com.flipkart.fashion.services

import java.util

import com.flipkart.fashion.BuildInfo
import nu.pattern.OpenCV
import org.opencv.core._
import org.opencv.highgui.Highgui
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.{CascadeClassifier, Objdetect}

// Draw a bounding box around each face.
import scala.collection.JavaConversions._

/**
  * Created by kinshuk.bairagi on 10/08/17.
  */
object IPService {

  OpenCV.loadShared()

  private val username = System.getProperty("user.name")
  private val CASCADE_FILE_FULL_BODY = getClass.getResource("/haarcascade_fullbody.xml").getPath
  private val CASCADE_FILE_HS = getClass.getResource("/HS.xml").getPath
  private val CASCADE_FILE_FACE = getClass.getResource("/haarcascade_frontalface_default.xml").getPath
  val Y_MIN  = 80
  val Y_MAX  = 255
  val Cb_MIN = 85
  val Cb_MAX = 135
  val Cr_MIN = 135
  val Cr_MAX = 180


  def detectBody(file:String): Unit ={

    val image = Highgui.imread(file)
    val bodyDetector = new CascadeClassifier(CASCADE_FILE_FULL_BODY)
    val hsDetector = new CascadeClassifier(CASCADE_FILE_HS)
    val faceDetector = new CascadeClassifier(CASCADE_FILE_FACE)

    val imageHeight = image.height()
    val humanHeight = imageHeight * 0.7

    val bodyDetections = new MatOfRect
    val hsDetections = new MatOfRect
    val faceDetections = new MatOfRect
    hsDetector.detectMultiScale(image, hsDetections)
//    bodyDetector.detectMultiScale(image, bodyDetections, 1.01, 2, Objdetect.CASCADE_SCALE_IMAGE , new Size(200,humanHeight), new Size())
    println("Detected "+hsDetections.toArray.length+" body")



    val image_bw: Mat = image.clone()
    val image_skin: Mat = image.clone()

    Imgproc.cvtColor(image, image_bw, Imgproc.COLOR_BGR2GRAY)
//    Core.inRange(image_bw, new Scalar(Y_MIN, Cr_MIN, Cb_MIN), new Scalar(Y_MAX, Cr_MAX, Cb_MAX), image_skin)

    faceDetector.detectMultiScale(image_bw, faceDetections, 1.01, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT , new Size(), new Size())

    Highgui.imwrite(s"/Users/$username/Pictures/image_bw.jpg", image_bw)
    Imgproc.blur(image_bw, image_bw, new Size(3,3))
    val canny_out: Mat = image.clone()

    Imgproc.Canny(image_bw,canny_out, 100, 200, 3, false)
    val contours = new util.ArrayList[MatOfPoint]()

    val hierarchy = new Mat()
    Imgproc.findContours(canny_out,contours,hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0))

    /// Draw contours
    val drawing = Mat.zeros( canny_out.size(), CvType.CV_8UC3 )
    println(contours(0).toList.head, contours(0).toList.last)
    for(  i <- 0 until  contours.size()  )
    {
      val color = new Scalar( 125, 200, 225 )
      Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point(0,0) )
    }

    for ( rect <- hsDetections.toArray) {
      Core.rectangle(drawing, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255))
    }

    for ( rect <- faceDetections.toArray) {
      Core.rectangle(drawing, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255))
    }

    val distance = hsDetections.toArray.head.y + hsDetections.toArray.head.height - (faceDetections.toArray.head.y + faceDetections.toArray.head.height)
    val point1 = new Point(faceDetections.toArray.head.x + faceDetections.toArray.head.width / 2, faceDetections.toArray.head.y + faceDetections.toArray.head.height )
    val point2 = new Point(faceDetections.toArray.head.x + faceDetections.toArray.head.width / 2, hsDetections.toArray.head.y + hsDetections.toArray.head.height )

    val point3 = new Point(hsDetections.toArray.head.x, faceDetections.toArray.head.y + faceDetections.toArray.head.height + distance / 2)
    val point4 = new Point(hsDetections.toArray.head.x + hsDetections.toArray.head.width, faceDetections.toArray.head.y + faceDetections.toArray.head.height + distance / 2)

    Core.line(drawing, point1, point2, new Scalar(255, 255, 255))
    Core.line(drawing, point3, point4, new Scalar(255, 255, 255))

    Highgui.imwrite(s"/Users/$username/Pictures/result_gray5.jpg", drawing)

//    Core.rectangle(image, new Point(contours(0).toList.head.x, contours(0).toList.head.y), new Point(contours(0).toList.head.x + contours(0).toList.head.width, rect.y + rect.height), new Scalar(0, 255, 0))
    /// Convert image to gray and blur it
//
//    cvtColor(src, src_gray, CV_BGR2GRAY)
//    blur(src_gray, src_gray, Size(3, 3))

  }

  def copyImage(personFile: String, dressFile: String, resultFile: String): Unit = {
    val personImage = Highgui.imread(personFile, Highgui.CV_LOAD_IMAGE_UNCHANGED)
    val dressImage = Highgui.imread(dressFile, Highgui.CV_LOAD_IMAGE_UNCHANGED)

    personImage.copyTo(dressImage)
    Highgui.imwrite(resultFile, dressImage)


  }

  def main(args: Array[String]): Unit = {
    val imageResources = BuildInfo.baseDirectory + "/resources/"
    detectBody(imageResources + "people/img2.jpg")
    copyImage(imageResources + "people/img2.jpg", imageResources + "dresses/dress1.png", imageResources + "result1.png")
    copyImage(imageResources + "dresses/dress1.png", imageResources + "people/img2.jpg", imageResources + "result2.png")

  }

}
