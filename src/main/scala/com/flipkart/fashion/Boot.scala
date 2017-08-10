package com.flipkart.fashion

import _root_.akka.actor.ActorSystem
import _root_.akka.http.scaladsl.Http
import _root_.akka.stream.ActorMaterializer
import com.flipkart.fashion.api.Routes
import com.flipkart.fashion.directives.CORSDirectives

object Boot extends App with CORSDirectives{

	implicit val actorSystem = ActorSystem("system")
	implicit val actorMaterializer = ActorMaterializer()

	println("Hello World")

	Http().bindAndHandle(cors(new Routes().route), "0.0.0.0", 9090)
}