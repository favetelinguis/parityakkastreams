package org.parity

import java.net.{NetworkInterface, InetAddress}

/**
  * Internal API
  */

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
