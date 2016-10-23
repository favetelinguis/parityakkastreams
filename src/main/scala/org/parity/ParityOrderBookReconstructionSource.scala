package org.parity

import java.io.IOException
import java.nio.channels.{SelectionKey, Selector}

import akka.NotUsed
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler, TimerGraphStageLogic}
import com.paritytrading.nassau.MessageListener
import com.paritytrading.nassau.moldudp64.{MoldUDP64Client, MoldUDP64ClientStatusListener}
import com.paritytrading.parity.net.pmd.{PMD, PMDListener, PMDParser}

import scala.concurrent.duration._
import scala.collection.mutable

// Temporary junk message, not proper for usage
final case class IncomingMessage(s: String)

object ParityOrderBookReconstructionSource {
  /**
    * Scala API:
    */
  def apply(settings: ParitySourceSettings, bufferSize: Int): Source[IncomingMessage, NotUsed] =
    Source.fromGraph(new ParityOrderBookReconstructionSource(settings, bufferSize))

  /**
    * Java API:
    */
  def create(settings: ParitySourceSettings, bufferSize: Int): akka.stream.javadsl.Source[IncomingMessage, NotUsed] =
    akka.stream.javadsl.Source.fromGraph(new ParityOrderBookReconstructionSource(settings, bufferSize))

  private val defaultAttributes = Attributes.name("ParitySource")

}

/**
  * Connects to an parity server upon materialization and consumes messages from it emitting them
  * into the stream. Each materialized stage will create one connection to the server.
  * As soon as an `IncomingMessage` is send downstream.
  *
  * @param bufferSize The max number of elements to buffer before craching if to slow consumption
  */
final class ParityOrderBookReconstructionSource(settings: ParitySourceSettings, bufferSize: Int) extends GraphStage[SourceShape[IncomingMessage]] with ParityConnector { stage =>

  val out = Outlet[IncomingMessage]("ParitySource.out")

  override def shape: SourceShape[IncomingMessage] = SourceShape.of(out)

  override protected def initialAttributes: Attributes = ParityOrderBookReconstructionSource.defaultAttributes

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new TimerGraphStageLogic(shape) with ParityConnectorLogic {

      override val settings = stage.settings
      override def connectionFactoryFrom(settings: ParitySourceSettings, listener: MessageListener, statusListener: MoldUDP64ClientStatusListener): MoldUDP64Client =
        stage.connectionFactoryFrom(settings, listener, statusListener)

      private val queue = mutable.Queue[IncomingMessage]()

      override val listener = new PMDParser(
        new PMDListener {

          override def version(message: PMD.Version) = Unit

          override def seconds(message: PMD.Seconds) = Unit

          override def orderAdded(message: PMD.OrderAdded) {
//            consumerCallback.invoke(IncomingMessage(message.instrument))
            handleDelivery(IncomingMessage(message.instrument.toString))
//            market.add(message.instrument, message.orderNumber, side(message.side), message.price, message.quantity)
          }

          override def orderExecuted(message: PMD.OrderExecuted) {
//            market.execute(message.orderNumber, message.quantity)
            handleDelivery(IncomingMessage(message.orderNumber.toString))
          }

          override def orderCanceled(message: PMD.OrderCanceled) {
            handleDelivery(IncomingMessage(message.orderNumber.toString))
//            market.cancel(message.orderNumber, message.canceledQuantity)
          }

          override def orderDeleted(message: PMD.OrderDeleted) {
            handleDelivery(IncomingMessage(message.orderNumber.toString))
//            market.delete(message.orderNumber)
          }

          override def brokenTrade(message: PMD.BrokenTrade) = Unit

//          def side(side: Byte) = side match {
//            case PMD.BUY  => Side.BUY
//            case PMD.SELL => Side.SELL
//          }
        })

      def handleDelivery(message: IncomingMessage): Unit = {
        if (queue.size + 1 > bufferSize) {
          failStage(new RuntimeException(s"Reached maximum buffer size $bufferSize"))
        } else {
          queue.enqueue(message)
        }
      }

      //When pull is made send out from buffer
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (queue.nonEmpty) {
            queue.dequeue()
          }
        }

      })

      override def onTimer(timerKey: Any) {
        if (!isClosed(out)) {
          doPoll()
        }
        schedulePoll()
      }

  }

}
