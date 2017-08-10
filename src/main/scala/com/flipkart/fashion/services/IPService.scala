package com.flipkart.fashion.services

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import  org.bytedeco.javacpp.opencv_imgcodecs._

/**
  * Created by kinshuk.bairagi on 10/08/17.
  */
object IPService {

  def smooth(fileName:String, dest:String) ={
    val image = cvLoadImage(fileName)
    if (image != null) {
      cvSmooth(image, image)
      cvSaveImage(dest, image)
      cvReleaseImage(image)
    }
  }

  def main(args: Array[String]): Unit = {
    smooth("/Users/kinshuk.bairagi/Pictures/kat.jpg", "/Users/kinshuk.bairagi/Pictures/kat-smotth.jpg")
  }

}
