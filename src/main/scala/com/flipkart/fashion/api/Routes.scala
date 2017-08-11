package com.flipkart.fashion.api

import akka.http.javadsl.model.ContentType
import akka.http.scaladsl.model.{HttpProtocols, HttpResponse, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.flipkart.fashion.directives.FileDirective
import com.flipkart.fashion.services.IPService
import com.flipkart.fashion.wire.{Response, JsonToEntityMarshaller, GenericResponse}
import com.flipkart.fashion.utils.Wrappers._
import com.flipkart.fashion.wire.GenericResponse
import com.flipkart.fashion.wire.Response

import scala.util.{Failure, Success}

/**
  * Created by hirendra.thakur on 10/08/17.
  */
class Routes(implicit mat: Materializer) extends BaseHandler with FileDirective  {

  val route = withoutRequestTimeout {
    pathEndOrSingleSlash {
      get {
        complete(GenericResponse(StatusCodes.OK.intValue, null, Response(s"Hello World", "hello")))
      }
    } ~ path("upload") {
      post {
        extractFormData { postMap =>
          val fileInfo = postMap("file").right.get
          fileInfo.status match {
            case Success(_) =>
              println(s"Upload Complete ${fileInfo.tmpFilePath}")
              val fileId = IPService.saveUserImage(fileInfo.tmpFilePath)
              complete(GenericResponse(StatusCodes.OK.intValue, null, Response(s"Upload Accepted: Tmp File Created", Map("profileId" -> fileId))))
            case Failure(e) =>
              //There was some isse processing the fileupload.
              println("Upload File Error", e)
              complete(GenericResponse(StatusCodes.InternalServerError.intValue, null, Response("There was some error processing your request", Map("debug" -> e.getMessage))))
          }
        }
      }
    } ~ path("getImage") {
      get {
        parameters('profileId)
        { (profileId) => {
          val destination = IPService.getImage(profileId)
          val re = HttpResponse(status = StatusCodes.OK, Nil, destination, HttpProtocols.`HTTP/1.1`)
          complete(re)
        }
        }
      }
    } ~ path("getTheLook") {
      get {
        parameters('profileId, 'productId) { (profileId, productId) => {
          val destination = IPService.getTheLook(profileId, productId)
          val re = HttpResponse(status = StatusCodes.OK, Nil, destination, HttpProtocols.`HTTP/1.1`)
          complete(re)
        }
        }
      }
    }
  }
}
