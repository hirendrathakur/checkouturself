package com.flipkart.fashion.services

import java.io.File

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.apache.commons.lang.RandomStringUtils
import org.bytedeco.javacpp.opencv_objdetect._
/**
  * Created by kinshuk.bairagi on 10/08/17.
  */
object IPService {

  private val CASCADE_FILE_FULL_BODY = getClass.getResource("/haarcascade_fullbody.xml").getFile

  def detectBody(file:String): Unit ={
//    val tempImageName = "/tmp/" + RandomStringUtils.random(20, true, true)
//    val imageFile = new File(tempImageName)
//    FileUtils.copyURLToFile(imageUrl, imageFile)

    val image = cvLoadImage(file)
//    val cascadeFileNameArrayUpperBody = Array(CASCADE_FILE_UPPER_BODY_1, CASCADE_FILE_UPPER_BODY_2)

    val cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_FULL_BODY))
    val storage = org.bytedeco.javacpp.helper.opencv_core.AbstractCvMemStorage.create()

    val sign = cvHaarDetectObjects(image, cascade, storage, 1.1, 2 , CV_HAAR_FIND_BIGGEST_OBJECT , null, null)
//    val sign = cvHaarDetectObjects(image, cascade, storage,1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH, null, null)
    cvClearMemStorage(storage)

    println(sign.total())
    println(sign.first())

    // We can allocate native arrays using constructors taking an integer as argument.
    val hatPoints = new CvPoint(3)

    for ( i <- 1 to sign.total()) {
      val r = new CvRect(cvGetSeqElem(sign, i))
      val x = r.x
      val y = r.y
      val w = r.width
      val h = r.height
      cvRectangle(image, cvPoint(x, y), cvPoint(x + w, y + h), org.bytedeco.javacpp.helper.opencv_core.CV_RGB(255,0,0), 1, CV_AA, 0)

    }


    cvSaveImage("/Users/kinshuk.bairagi/Pictures/res.jpg", image)


  }

  def smooth(fileName:String, dest:String) ={
    val image = cvLoadImage(fileName)
    if (image != null) {
      cvSmooth(image, image)
      cvSaveImage(dest, image)
      cvReleaseImage(image)
    }
  }

  def main(args: Array[String]): Unit = {
//    smooth("/Users/kinshuk.bairagi/Pictures/kat.jpg", "/Users/kinshuk.bairagi/Pictures/kat-smotth.jpg")
    detectBody("/Users/kinshuk.bairagi/Documents/fashion-hackday/checkouturself/resources/img2.jpg")
  }

}
