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
  val multicastPort      = 5000
  val requestAddress     = InetAddress.getByName("127.0.0.1")
  val requestPort        = 5001

  val s = ParitySourceSettings(
    multicastInterface,
    multicastGroup,
    multicastPort,
    requestAddress,
    requestPort)


  PMDSource(settings = s, bufferSize = 10)
    .via(ParityOrderBookReconstructionFlow())
    .runWith(Sink.foreach(println(_)))
}
