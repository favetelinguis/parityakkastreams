package org.parity

import java.net.{InetAddress, NetworkInterface}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

/**
  * Created by henke on 23/10/16.
  */
object Main extends App {

  implicit val system = ActorSystem("MAINSYS")
  implicit val materializer = ActorMaterializer()

  val multicastInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"))
  val multicastGroup     = InetAddress.getByName("224.0.0.1")
  val multicastPort      = 4001
  val requestAddress     = InetAddress.getByName("127.0.0.1")
  val requestPort        = 4002

  val s = ParitySourceSettings(
    multicastInterface,
    multicastGroup,
    multicastPort,
    requestAddress,
    requestPort)

  val instruments = List("FOO", "BAR", "BAZ")

  ParityOrderBookReconstructionSource(settings = s, 100).runWith(Sink.foreach(println(_)))
}
