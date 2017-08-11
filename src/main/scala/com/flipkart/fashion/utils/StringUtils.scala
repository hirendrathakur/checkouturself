/*
 *         -╥⌐⌐⌐⌐            -⌐⌐⌐⌐-
 *      ≡╢░░░░⌐\░░░φ     ╓╝░░░░⌐░░░░╪╕
 *     ╣╬░░`    `░░░╢┘ φ▒╣╬╝╜     ░░╢╣Q
 *    ║╣╬░⌐        ` ╤▒▒▒Å`        ║╢╬╣
 *    ╚╣╬░⌐        ╔▒▒▒▒`«╕        ╢╢╣▒
 *     ╫╬░░╖    .░ ╙╨╨  ╣╣╬░φ    ╓φ░╢╢Å
 *      ╙╢░░░░⌐"░░░╜     ╙Å░░░░⌐░░░░╝`
 *        ``˚¬ ⌐              ˚˚⌐´
 *
 *      Copyright © 2016 Flipkart.com
 */
package com.flipkart.fashion.utils

import java.io.InputStream
import java.lang.reflect.{ParameterizedType, Type => JType}
import java.math.BigInteger
import java.security.SecureRandom
import java.util.UUID

import akka.http.scaladsl.model.HttpEntity
import akka.stream.Materializer
import akka.util.ByteString
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.commons.lang.CharEncoding

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, _}


object StringUtils {

  val currentMirror = runtimeMirror(getClass.getClassLoader)


  implicit def enum2String(enumValue: Enumeration#Value): String = enumValue.toString

  implicit def generateUUID: String = UUID.randomUUID().toString

  implicit class StringHandyFunctions(val s: String) {
    def getUtf8Bytes = s.getBytes(CharEncoding.UTF_8)


    def isDefined = null != s && s.nonEmpty


    def stripNewLines = s.replaceAll("\n", "").replaceAll("\r", "")

  }


  implicit class InputStreamHandyFunctions(val is: InputStream) {
    def getString = scala.io.Source.fromInputStream(is).mkString
  }

  implicit class StringOptionHandyFunctions(val obj: Option[String]) {
    def orEmpty = obj.getOrElse("")
  }

  implicit class OptionHandyFunctions(val obj: Option[Any]) {
    def getString = obj.map(_.toString).get
  }


  implicit class ObjectHandyFunction(val obj: AnyRef) {
    def asMap: Map[String, Any] = {
      val fieldsAsPairs = for (field <- obj.getClass.getDeclaredFields) yield {
        field.setAccessible(true)
        (field.getName, field.get(obj))
      }
      Map(fieldsAsPairs: _*)
    }
  }

  val objMapper = new ObjectMapper() with ScalaObjectMapper
  objMapper.registerModules(Seq(DefaultScalaModule): _*)
  objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  implicit class JSONMarshallFunctions(val o: AnyRef) {
    def getJson = objMapper.writeValueAsString(o)

    def getJsonNode = objMapper.convertValue(o, classOf[ObjectNode])
  }

  implicit class ByteArrayHandyFunctions(val b: Array[Byte]) {
    def getString = new String(b, CharEncoding.UTF_8)

    def getObj[T: ClassTag] = objMapper.readValue(b, classTag[T].runtimeClass).asInstanceOf[T]


  }

  implicit class JSONUnMarshallFunctions(val s: String) {

    def getObj[T: ClassTag] = objMapper.readValue(s, classTag[T].runtimeClass).asInstanceOf[T]

    def getObj(implicit cType: Class[_]) = objMapper.readValue(s, cType)

    def getObj[T](tTag: TypeTag[T]): T = objMapper.readValue(s, typeReference[T](tTag))

    private def typeReference[T](tag: TypeTag[T]): TypeReference[_] = new TypeReference[T] {
      override val getType = jTypeFromType(tag.tpe)
    }

    private def jTypeFromType(tpe: Type): JType = {
      val typeArgs = tpe match {
        case TypeRef(_, _, args) => args
      }
      val runtimeClass = currentMirror.runtimeClass(tpe)
      if (typeArgs.isEmpty) {
        runtimeClass
      }
      else new ParameterizedType {
        def getRawType = runtimeClass

        def getActualTypeArguments = typeArgs.map(jTypeFromType).toArray

        def getOwnerType = runtimeClass.getEnclosingClass
      }
    }
  }

  implicit class HttpEntity2String(val entity: HttpEntity) {
    def getString(implicit mat: Materializer): String = {
      import akka.http.scaladsl.unmarshalling._
      implicit val ec = mat.executionContext
      val futureString = Unmarshal(entity).to[String]
      Await.result(futureString, 60.seconds)
    }
  }

  def isNullOrEmpty(o: Any): Boolean = o match {
    case m: Map[_, _] => m.isEmpty
    case i: Iterable[Any] => i.isEmpty
    case null | None | "" => true
    case Some(x) => isNullOrEmpty(x)
    case _ => false
  }

  def getObjectNode = objMapper.createObjectNode()

  def getArrayNode = objMapper.createArrayNode()

  def generateRandomStr(len: Int): String = {
    val ZERO = Character.valueOf('0')
    val A = Character.valueOf('A')
    val sb = new StringBuffer()
    for (i <- 1 to len) {
      var n = (36.0 * Math.random).asInstanceOf[Int]
      if (n < 10) {
        n = ZERO + n
      }
      else {
        n -= 10
        n = A + n
      }
      sb.append(new Character(n.asInstanceOf[Char]))
    }
    new String(sb)
  }

  def generateSecureRandom: String = {
    val random: SecureRandom = new SecureRandom()
    new BigInteger(130, random).toString(32)
  }

}
