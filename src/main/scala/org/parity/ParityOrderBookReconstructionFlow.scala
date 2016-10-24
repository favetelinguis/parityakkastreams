package org.parity

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.paritytrading.foundation.ASCII
import com.paritytrading.parity.top.{Market, MarketListener, Side}

import scala.collection.mutable

/**
  * Created by henke on 23/10/16.
  */
sealed trait MarketDataEvent

final case class BBO(
                instrument: String,
                bidPrice:   Double,
                bidSize:    Long,
                askPrice:   Double,
                askSize:    Long
              ) extends MarketDataEvent

final case class Trade(
                  instrument: String,
                  price:      Double,
                  size:       Long
                ) extends MarketDataEvent

object ParityOrderBookReconstructionFlow {
  /**
    * Scala API:
    */
  def apply(): Flow[IncomingMessage, MarketDataEvent, NotUsed] =
  Flow.fromGraph(new ParityOrderBookReconstructionFlow)

  /**
    * Java API:
    */
  def create(): akka.stream.javadsl.Flow[IncomingMessage, MarketDataEvent, NotUsed] =
  akka.stream.javadsl.Flow.fromGraph(new ParityOrderBookReconstructionFlow)

  private val defaultAttributes = Attributes.name("ParityFlow")

}

final class ParityOrderBookReconstructionFlow extends GraphStage[FlowShape[IncomingMessage, MarketDataEvent]] {

  val in: Inlet[IncomingMessage] = Inlet("IncomingMessage.in")
  val out: Outlet[MarketDataEvent] = Outlet("MarketDataEvent.out")

  override val shape: FlowShape[IncomingMessage, MarketDataEvent] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler with OutHandler {

      //TODO: Dont really need a queue since we only ever consume one item at a time
      val queue = mutable.Queue[MarketDataEvent]()
      val PriceFactor = 10000.0
      val market = new Market(new MarketListener {

        override def bbo(instrument: Long, bidPrice: Long, bidSize: Long, askPrice: Long, askSize: Long) {
          queue.enqueue(BBO(
            instrument = ASCII.unpackLong(instrument).trim,
            bidPrice   = bidPrice / PriceFactor,
            bidSize    = bidSize,
            askPrice   = askPrice / PriceFactor,
            askSize    = askSize
          ))
        }

        override def trade(instrument: Long, side: Side, price: Long, size: Long) {
          queue.enqueue(Trade(
            instrument = ASCII.unpackLong(instrument).trim,
            price      = price / PriceFactor,
            size       = size
          ))
        }
      })

     List("FOO", "BAR", "BAZ").foreach { instrument =>
       market.open(ASCII.packLong(instrument))
     }

      setHandlers(in, out, this)

      // Output ready to emit, push is ready to be called
      override def onPull(): Unit = pull(in)

      override def onPush(): Unit = {
        val inData: IncomingMessage = grab(in)
        updateMarket(inData)
        push(out, queue.dequeue())
      }

      private def updateMarket(inData: IncomingMessage): Unit = {
        inData match {
          case message: OrderAdded =>
            market.add(message.instrument, message.orderNumber, message.side, message.price, message.quantity)
          case message: OrderExecuted =>
            market.execute(message.orderNumber, message.quantity)
          case message: OrderCanceled =>
            market.cancel(message.orderNumber, message.canceledQuantity)
          case message: OrderDeleted =>
            market.delete(message.orderNumber)
        }
      }

    }

}
