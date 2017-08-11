package com.flipkart.fashion.services

import java.io.{File, FileInputStream}
import java.util

import akka.http.scaladsl.model.{ResponseEntity, MediaTypes, HttpEntity}
import akka.stream.scaladsl.FileIO
import com.flipkart.fashion.BuildInfo
import com.flipkart.fashion.services.IPService.testGrid
import com.flipkart.fashion.utils.StringUtils
import nu.pattern.OpenCV
import org.apache.commons.io.IOUtils
import org.opencv.core._
import org.opencv.highgui.Highgui
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.{CascadeClassifier, Objdetect}
import scala.sys.process._

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
  private var personShoulderPoints:(Point, Point) = (new Point(613,313), new Point(855,313))
  val Y_MIN  = 80
  val Y_MAX  = 255
  val Cb_MIN = 85
  val Cb_MAX = 135
  val Cr_MIN = 135
  val Cr_MAX = 180

  def getContourImage(file: String) = {
    val image = Highgui.imread(file)

    val image_bw: Mat = image.clone()

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

    drawing
  }

  def leftShoulder(dressImage: Mat): Point = {

    for (i <- 0 until dressImage.height()) {
      for (j <- 0 until dressImage.width()) {

        if (!dressImage.get(i, j).contains(0.0) && j < dressImage.width() / 2 ) {
          return new Point(j, i)
        }

      }
    }

    null
  }


  def rightShoulder(dressImage: Mat): Point = {

    for (i <- 0 until dressImage.height()) {
      for (j <- dressImage.width() - 1 to 0 by -1) {
        if (!dressImage.get(i, j).contains(0.0) && j > dressImage.width() / 2) {
          return new Point(j, i)
        }
      }
    }

    null
  }


  def shoulders(image: Mat): (Point, Point) = {
    (leftShoulder(image), rightShoulder(image))
  }

  def shoulders(file: String): (Point, Point) = {
    shoulders(getContourImage(file))
  }


  def neckMidPoint(file: String ) {
    val image = Highgui.imread(file)
    neckMidPoint(image)
  }

  def neckMidPoint(image:Mat): Point = {

    val hsDetector = new CascadeClassifier(CASCADE_FILE_HS)
    val faceDetector = new CascadeClassifier(CASCADE_FILE_FACE)

    var hsDetections = new MatOfRect
    val faceDetections = new MatOfRect
    hsDetector.detectMultiScale(image, hsDetections)

    val image_bw: Mat = image.clone()

    Imgproc.cvtColor(image, image_bw, Imgproc.COLOR_BGR2GRAY)

    faceDetector.detectMultiScale(image_bw, faceDetections, 1.01, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT , new Size(), new Size())

    Highgui.imwrite(s"/Users/$username/Pictures/image_bw.jpg", image_bw)
    Imgproc.blur(image_bw, image_bw, new Size(3,3))
    val canny_out: Mat = image.clone()
    hsDetections = new MatOfRect(hsDetections.toArray.filter(hs => hs.contains(faceDetections.toArray.head.tl())): _*)

    val distance = hsDetections.toArray.head.y + hsDetections.toArray.head.height - (faceDetections.toArray.head.y + faceDetections.toArray.head.height)

    new Point(faceDetections.toArray.head.x + faceDetections.toArray.head.width / 2, faceDetections.toArray.head.y + faceDetections.toArray.head.height + distance / 4)

  }

  def getShoulderStartingX(drawing:Mat, start:Int,  end:Int, intersectY:Int): Int = {
    for (i <- start until end) {
      if (drawing.get(intersectY,i).toList != List(0.0, 0.0, 0.0)) {
        return i
      }
    }
    0
  }

  def getShoulderEndingX(drawing:Mat, start:Int,  end:Int, intersectY:Int): Int = {
    for (i <- end to start by -1) {
      if (drawing.get(intersectY,i).toList != List(0.0, 0.0, 0.0)) {
        return i
      }
    }
    0
  }

  def detectBody(file:String): Unit ={

    val image = Highgui.imread(file)
    val hsDetector = new CascadeClassifier(CASCADE_FILE_HS)
    val faceDetector = new CascadeClassifier(CASCADE_FILE_FACE)

    val imageHeight = image.height()

    var hsDetections = new MatOfRect
    val faceDetections = new MatOfRect
    hsDetector.detectMultiScale(image, hsDetections)
//    bodyDetector.detectMultiScale(image, bodyDetections, 1.01, 2, Objdetect.CASCADE_SCALE_IMAGE , new Size(200,humanHeight), new Size())
    println("Detected "+hsDetections.toArray.length+" body")

    val image_bw: Mat = image.clone()

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

    val drawingShoulder = drawing.clone()
    for ( rect <- hsDetections.toArray) {
      Core.rectangle(drawing, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255))
    }
    hsDetections = new MatOfRect(hsDetections.toArray.filter(hs => hs.contains(faceDetections.toArray.head.tl())): _*)
    println("head and should size = " + hsDetections.toArray.size)
    for ( rect <- faceDetections.toArray) {
      Core.rectangle(drawing, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255))
    }
    println("face size = " + faceDetections.toArray.size)

    val distance = hsDetections.toArray.head.y + hsDetections.toArray.head.height - (faceDetections.toArray.head.y + faceDetections.toArray.head.height)
    val point1 = new Point(faceDetections.toArray.head.x + faceDetections.toArray.head.width / 2, faceDetections.toArray.head.y + faceDetections.toArray.head.height )
    val point2 = new Point(faceDetections.toArray.head.x + faceDetections.toArray.head.width / 2, hsDetections.toArray.head.y + hsDetections.toArray.head.height )
    val point3 = new Point(hsDetections.toArray.head.x, faceDetections.toArray.head.y + faceDetections.toArray.head.height + distance / 4)
    val point4 = new Point(hsDetections.toArray.head.x + hsDetections.toArray.head.width, faceDetections.toArray.head.y + faceDetections.toArray.head.height + distance / 4)

//    Core.line(drawing, point1, point2, new Scalar(255, 255, 255))
//    Core.line(drawing, point3, point4, new Scalar(255, 255, 255))

    val intersectX = point3.x + (point4.x-point3.x)/2
    val intersectY = point1.y + (point2.y-point1.y)/2
    val shoulderStartX = getShoulderStartingX(drawingShoulder,  point3.x.toInt, intersectX.toInt, intersectY.toInt)
    val shoulderEndX = getShoulderEndingX(drawingShoulder,intersectX.toInt ,  point4.x.toInt , intersectY.toInt)

    val point5 = new Point(shoulderStartX, intersectY)
    val point6 = new Point(shoulderEndX, intersectY)

    Core.line(drawing, point5, point6, new Scalar(255, 255, 255))


    Core.line(drawingShoulder, point5, point6, new Scalar(255, 255, 255))
    Highgui.imwrite(s"/Users/$username/Pictures/result_gray5.jpg", drawing)
    Highgui.imwrite(s"/Users/$username/Pictures/person_shoulder.jpg", drawingShoulder)
    personShoulderPoints = (point5, point6)
    println(personShoulderPoints)

  }



  def distance(point1:Point, point2 : Point): Double = {
    val x = Math.pow(point2.x - point1.x, 2)
    val y = Math.pow(point2.y - point1.y, 2)
    Math.sqrt(x + y)
  }


  def shouldMid(dressImage: Mat): Point = {
    val l = leftShoulder(dressImage)
    val r = rightShoulder(dressImage)
    new Point((l.x + r.x) / 2, (l.y + r.y) / 2)

  }

  def putOnDress(person:Mat, dress:Mat, shoulder:(Point, Point)): Mat ={
    //shoulder point left, right
    //dress left, dress right

    val shoulderDistance = distance( shoulder._1, shoulder._2)
    val strap = shoulders( dress)
    val strapDistace = distance(strap._1, strap._2)
    println("shoulderDistance",shoulderDistance)
    println("strapDistance",strapDistace)
    val (newDHeight, newDWdith) = {
      val d = if(strapDistace> shoulderDistance) strapDistace/ shoulderDistance else shoulderDistance/strapDistace
      val targetWidth = shoulderDistance + (dress.width()-strapDistace)/d
      val ratio = targetWidth / dress.width()
      val targetHeight = dress.height().toDouble * ratio
      println(ratio)
      targetHeight -> targetWidth
    }

    println("actual dress size",dress.height(),dress.width())
    println("new dress size", newDHeight,newDWdith)

    val resizeimage = new Mat
    Imgproc.resize(dress, resizeimage, new Size(newDWdith, newDHeight))
    println("resizeImage", resizeimage.size())
    Imgproc.cvtColor(person,person, Imgproc.COLOR_RGB2RGBA)

    val finalImage = person.clone()
    println("shoulderPOint, Overlay", shoulder._1)
    println("finalImage", finalImage.size())
    val newStaps = shoulders(resizeimage)
    println("newStaps", newStaps)

    //
    val neckPoint = neckMidPoint(person)
    println("neckmidpoint", neckPoint)
    val shoulderMidpoint =shouldMid(resizeimage)

    println("resizeImagewidth", resizeimage.width())

    val startPOint = new Point( neckPoint.x - shoulderMidpoint.x ,neckPoint.y  - shoulderMidpoint.y)
//    val startPOint =  new Point(shoulder._1.x - newStaps._1.x + 25 ,shoulder._1.y - newStaps._1.y - 20)  //new Point(0,0)

    for(i <- startPOint.x.toInt to startPOint.x.toInt + resizeimage.width()){
      for (j <- startPOint.y.toInt to startPOint.y.toInt + resizeimage.height()){
        val x = i - startPOint.x.toInt
        val y = j - startPOint.y.toInt
        val overayPoint = resizeimage.get(y,x )
        if(overayPoint != null && overayPoint(3) > 0)
          finalImage.put(j,i, overayPoint:_ *)
      }
    }

    println("resizeimage", resizeimage.size())

    finalImage
  }

  def getWaist(drawing: Mat): Unit = {
    val upperBodyDetections = new MatOfRect
    val lowerBodyDetections = new MatOfRect

    // Middle of upperBody part and lowerBody part
    val midUpper = new Point(upperBodyDetections.toArray.head.x + upperBodyDetections.toArray.head.width / 2, (upperBodyDetections.toArray.head.y + upperBodyDetections.toArray.head.height))
    val midLower = new Point(lowerBodyDetections.toArray.head.x + lowerBodyDetections.toArray.head.width / 2, (lowerBodyDetections.toArray.head.y))
    Core.line(drawing, midUpper, midLower, new Scalar(255, 255, 255))

    val waistMiddleY = (midLower.y + midUpper.y) / 2
    val waistMiddlePoint = new Point(midUpper.x, waistMiddleY)

    Core.circle(drawing, waistMiddlePoint, 50, new Scalar(255, 255, 255))

    for (i <- waistMiddlePoint.x.toInt to upperBodyDetections.toArray.head.x by -1) {
      val tempPoint = new Point(i, waistMiddleY)
      if(testGrid(tempPoint, drawing)){
        Core.circle(drawing, tempPoint, 50, new Scalar(255, 255, 255))
      }
    }
  }

  def testGrid(point: Point, mat: Mat) : Boolean = {
    for (i <- point.x.toInt - 3  to point.y.toInt + 3 ){
      for (j <- point.y.toInt -3 to point.y.toInt + 3) {
        return true
      }
    }
    false
  }

  def main(args: Array[String]): Unit = {
    val imageResources = BuildInfo.baseDirectory + "/resources/"
    val person = imageResources + "people/img2.jpg"
    detectBody(person)
    val src = Highgui.imread(person, Highgui.CV_LOAD_IMAGE_UNCHANGED)
    println("personShoulderPoints",personShoulderPoints)
    val dress = Highgui.imread(imageResources + "dresses/dress2.png", Highgui.CV_LOAD_IMAGE_UNCHANGED)
    val res = putOnDress(src, dress,personShoulderPoints)
    Highgui.imwrite(s"/Users/$username/Pictures/showoff.jpg", res)

  }

  def getTheLook(profileId:String, productId: String):ResponseEntity = {
    val imageResources = BuildInfo.baseDirectory + "/resources/"
    val person = imageResources + s"people/$profileId.jpg"
    detectBody(person)
    val src = Highgui.imread(person, Highgui.CV_LOAD_IMAGE_UNCHANGED)
    println("personShoulderPoints",personShoulderPoints)
    val dress = Highgui.imread(imageResources + s"dresses/$productId.png", Highgui.CV_LOAD_IMAGE_UNCHANGED)
    val res = putOnDress(src, dress,personShoulderPoints)
    HttpEntity(MediaTypes.`image/jpeg`, toByte(res))
  }

  def toByte(image:Mat):Array[Byte] = {
    val b = new MatOfByte
    Highgui.imencode(".jpg",image, b)
    b.toArray
  }

  def saveUserImage(file:String): String = {
    val imageResources = BuildInfo.baseDirectory + "/resources/"
    val fileId = StringUtils.generateRandomStr(6)
    val copyCmd = s"cp $file $imageResources/people/$fileId.jpg"
    val cmdOutput = copyCmd.!!
    fileId
  }
}
