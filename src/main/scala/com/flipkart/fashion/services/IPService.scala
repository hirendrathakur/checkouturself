package com.flipkart.fashion.services

import java.io.File

import nu.pattern.OpenCV
import org.opencv.highgui.Highgui
import org.opencv.objdetect.CascadeClassifier
import org.opencv.core.{Core, MatOfRect, Point, Scalar}
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

    Highgui.imwrite(s"/Users/$username/Pictures/result.jpg", image)


    /// Convert image to gray and blur it
//
//    cvtColor(src, src_gray, CV_BGR2GRAY)
//    blur(src_gray, src_gray, Size(3, 3))

  }

  def main(args: Array[String]): Unit = {
    detectBody("/Users/kinshuk.bairagi/Documents/fashion-hackday/checkouturself/resources/img2.jpg")
  }

}
