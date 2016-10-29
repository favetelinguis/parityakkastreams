
/**
  * Created by henke on 25/10/16.
  */
object Types {

  final case class Identifier(id: String)
  final case class Market(id: Int)

  sealed trait PublicFeedIn

  final case class Price(
                        identifier: Identifier,
                        marketId: Market,
                        tradeTimestamp: Option[String] = None,
                        tickTimestamp: Option[String] = None,
                        bestBidPrice: Option[Double] = None,
                        totalBidVolumeAtBestPrice: Option[Long] = None,
                        bestAskPrice: Option[Double] = None,
                        totalAskVolumeAtBestAskPrice: Option[Long] = None,
                        closePriceToday: Option[Double] = None,
                        highPriceToday: Option[Double] = None,
                        lastPriceTraded: Option[Double] = None,
                        lastVolumeTraded: Option[Long] = None,
                        lowPriceToday: Option[Double] = None,
                        openPriceToday: Option[Double] = None,
                        turnoverValue: Option[Double] = None,
                        turnoverVolume: Option[Long] = None,
                        equilibriumPrice: Option[Double] = None,
                        paired: Option[Long] = None,
                        imbalance: Option[Long] = None
                        ) extends PublicFeedIn

  // An event indicating that a trade has taken place.
  final case class Trade(
                          identifier: Identifier,
                          marketId: Market,
                          tradeTimestamp: String,
                          price: Double,
                          volume: Long,
                          brokerBuying: Option[String] = None,
                          brokerSelling: Option[String] = None,
                          tradeTypeId: Option[String] = None,
                          tradeType: Option[String] = None
                        ) extends PublicFeedIn
}
