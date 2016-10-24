package org.parity

import java.io.IOException
import java.net.{InetSocketAddress, StandardProtocolFamily, StandardSocketOptions}
import java.nio.channels.{DatagramChannel, SelectionKey, Selector}

import scala.concurrent.duration._
import akka.stream.stage.{GraphStageLogic, TimerGraphStageLogic}
import com.paritytrading.nassau.MessageListener
import com.paritytrading.nassau.moldudp64.{MoldUDP64Client, MoldUDP64ClientState, MoldUDP64ClientStatusListener}
import com.paritytrading.parity.net.pmd.{PMD, PMDListener, PMDParser}
import com.paritytrading.parity.top.{Market, MarketListener, Side}

/**
  * Created by henke on 20/10/16.
  */
private[parity] trait ParityConnector {

  /**
    * Creates the actual connection
    **/
  def connectionFactoryFrom(settings: ParitySourceSettings, listener: MessageListener, statusListener: MoldUDP64ClientStatusListener): MoldUDP64Client = {
    settings match {
      case ParitySourceSettings(
      multicastInterface,
      multicastIP,
      multicastPort,
      requestIP,
      requestPort) =>
        val multicastGroup = new InetSocketAddress(multicastIP, multicastPort)
        val requestAddress = new InetSocketAddress(requestIP, requestPort)

        val channel: DatagramChannel = DatagramChannel.open(StandardProtocolFamily.INET)
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true: java.lang.Boolean)
        channel.bind(new InetSocketAddress(multicastGroup.getPort))
        channel.join(multicastGroup.getAddress, multicastInterface)
        channel.configureBlocking(false)

        //Request channel not active?
        //Never bound to something since we are not using it in Protocol
        val requestChannel: DatagramChannel = DatagramChannel.open(StandardProtocolFamily.INET)
        requestChannel.configureBlocking(false)

        new MoldUDP64Client(channel, requestChannel, requestAddress, listener, statusListener)
    }
  }

}

/**
  * Internal API
  **/
private[parity] trait ParityConnectorLogic { this: TimerGraphStageLogic =>

  private var client: MoldUDP64Client = _
  private var selector: Selector = _
  private var channelKey: SelectionKey = _
  private var requestChannelKey: SelectionKey = _

  def settings: ParitySourceSettings
  def connectionFactoryFrom(settings: ParitySourceSettings, listener: MessageListener, statusListener: MoldUDP64ClientStatusListener): MoldUDP64Client
  def listener: MessageListener


  val  statusListener = new MoldUDP64ClientStatusListener {
    override def downstream(session: MoldUDP64Client): Unit = Unit
    override def state(session: MoldUDP64Client, next: MoldUDP64ClientState): Unit = Unit
    override def request(session: MoldUDP64Client, sequenceNumber: Long, requestedMessageCount: Int): Unit = Unit
    override def endOfSession(session: MoldUDP64Client): Unit = Unit
  }

  final override def preStart(): Unit = {
    try {
      //Create the client and connect channels to Parity server
      client = connectionFactoryFrom(settings, listener, statusListener)
      //Open a selector so that a single thread can handle multiple channels
      selector = Selector.open()
      //Register both channels to the selector
      channelKey = client.getChannel().register(selector, SelectionKey.OP_READ)
      requestChannelKey = client.getRequestChannel().register(selector, SelectionKey.OP_READ)
      //Get things going
      schedulePoll()
    } catch {
      case ex: Exception => {println(ex.getMessage.toString);failStage(ex)}
    }

  }

  def schedulePoll(): Unit = {
    scheduleOnce("poll", 2 milliseconds)
  }

  def doPoll(): Unit = {
    try {
      //Check it there are any avaliable channels with data
      if(selector.selectNow() != 0) {
        if (selector.selectedKeys().contains(channelKey))
          client.receive()

        if (selector.selectedKeys().contains(requestChannelKey))
          client.receiveResponse()

        selector.selectedKeys().clear()
      }
    } catch {
      case ex: Exception => {println(ex.getMessage.toString);failStage(ex)}
    }
  }

  override def postStop(): Unit = {
    selector.close()
    client.close()
  }
}
