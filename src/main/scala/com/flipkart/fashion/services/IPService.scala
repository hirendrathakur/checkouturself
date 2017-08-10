package com.flipkart.fashion.services

import java.io.File
import java.util

import nu.pattern.OpenCV
import org.opencv.highgui.Highgui
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.core._
// Draw a bounding box around each face.
import scala.collection.JavaConversions._

/**
  * Created by kinshuk.bairagi on 10/08/17.
  */
object IPService {

  OpenCV.loadShared()

  private val username = System.getProperty("user.name")
  private val CASCADE_FILE_FULL_BODY = getClass.getResource("/haarcascade_fullbody.xml").getPath

  def detectBody(file:String): Unit ={

    val image = Highgui.imread(file)
    val bodyDetector = new CascadeClassifier(CASCADE_FILE_FULL_BODY)

    val bodyDetections = new MatOfRect
    bodyDetector.detectMultiScale(image, bodyDetections)

    println("Detected "+bodyDetections.toArray.length+" body")

    for (rect <- bodyDetections.toArray) {
      Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0))
    }

    //Highgui.imwrite(s"/Users/$username/Pictures/result.jpg", image)


    val image_src: Mat = image.clone()
    Imgproc.cvtColor(image, image_src, Imgproc.COLOR_BGR2GRAY)
    Imgproc.blur(image_src, image_src, new Size(3,3))
    val canny_out: Mat = image.clone()
    Imgproc.Canny(image_src,canny_out, 100, 200, 3, false)
    val contours = new util.ArrayList[MatOfPoint]()
    val hierarchy = new Mat()
    Imgproc.findContours(canny_out,contours,hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0))

    /// Draw contours
    val drawing = Mat.zeros( canny_out.size(), CvType.CV_8UC3 )
    for(  i <- 0 until  contours.size()  )
    {
      val color = new Scalar( 125, 200, 225 )
      Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point(0,0) )
    }


    Highgui.imwrite(s"/Users/$username/Pictures/result_gray.jpg", drawing)
    /// Convert image to gray and blur it
//
//    cvtColor(src, src_gray, CV_BGR2GRAY)
//    blur(src_gray, src_gray, Size(3, 3))

  }

  def main(args: Array[String]): Unit = {
    detectBody("/Users/hirendra.thakur/practiceWorkspace/checkouturself/resources/img2.jpg")
  }

}
