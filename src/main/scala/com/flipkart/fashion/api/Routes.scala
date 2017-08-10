package com.flipkart.fashion.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.flipkart.fashion.wire.{Response, JsonToEntityMarshaller, GenericResponse}

/**
  * Created by hirendra.thakur on 10/08/17.
  */
class Routes(implicit mat: Materializer) extends JsonToEntityMarshaller {

  val route = withoutRequestTimeout {
    pathSingleSlash {
      get {
        complete {
          complete(GenericResponse(StatusCodes.OK.intValue, null, Response(s"Hello World", "hello")))
        }
      }

    }
  }
}
