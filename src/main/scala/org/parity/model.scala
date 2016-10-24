package org.parity

import java.net.{InetAddress, NetworkInterface}

import com.paritytrading.parity.top.Side

/**
  * Internal API
  */

sealed trait IncomingMessage
final case class OrderDeleted(orderNumber: Long) extends IncomingMessage
final case class OrderAdded(instrument: Long, orderNumber: Long, side: Side, price: Long, quantity: Long) extends IncomingMessage
final case class OrderExecuted(orderNumber: Long, quantity: Long) extends IncomingMessage
final case class OrderCanceled(orderNumber: Long, canceledQuantity: Long) extends IncomingMessage
/**
  * Only for internal implementations
  */
final case class ParitySourceSettings(
                                        multicastInterface: NetworkInterface,
                                        multicastGroup: InetAddress,
                                        multicastPort: Int,
                                        requestAddress: InetAddress,
                                        requestPort: Int
                                        )
